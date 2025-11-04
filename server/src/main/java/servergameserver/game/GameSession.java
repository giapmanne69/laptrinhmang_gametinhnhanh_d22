package servergameserver.game;

import commongameserver.model.GameResult;
import commongameserver.model.PlayerStatus;
import commongameserver.model.User;
import commongameserver.network.response.GameOverPacket;
import commongameserver.network.response.GameStartPacket;
import commongameserver.network.response.GameStateUpdatePacket;
import commongameserver.network.response.GameTimerPacket;
import commongameserver.network.response.OpponentExitPacket;
import commongameserver.network.response.ScoreUpdatePacket; 
import commongameserver.network.response.UserUpdatePacket;
import io.netty.channel.Channel;
import servergameserver.db.GameResultDao;
import servergameserver.db.UserDao;
import servergameserver.manager.ConnectionManager;
import servergameserver.service.GameManager;
import servergameserver.service.LobbyService;

import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
// Them import cho Regex
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Quan ly logic cho MOT van dau (doc lap) giua 2 nguoi choi.
 * (DA CAP NHAT: Them logic Dieu Kien (Constraint))
 */
public class GameSession implements Runnable {

    // Hang so
    private static final int TOTAL_NUMBERS = 30;
    private static final int GAME_DURATION_SECONDS = 120; // 2 phut

    // --- DINH NGHIA DIEU KIEN ---
    
    /**
     * Enum cho cac loai dieu kien
     */
    private enum ConstraintType {
        OPERAND_COUNT,      // So toan hang (Vi du: 3 so)
        OPERATOR_COUNT,     // So toan tu (Vi du: 2 toan tu)
        OPERATOR_REQUIRED   // Yeu cau toan tu cu the (Vi du: Phai co dau '+')
    }

    /**
     * Lop (record) de luu tru mot dieu kien
     */
    private record Constraint(
        ConstraintType type,       // Loai dieu kien
        String description,    // Mo ta cho Client (Vi du: "Phai dung 3 toan hang")
        int value,             // Gia tri so (Vi du: 3)
        String operator         // Gia tri toan tu (Vi du: "+")
    ) implements java.io.Serializable {} // Can Serializable de gui qua mang (neu can)

    // --- KET THUC DINH NGHIA DIEU KIEN ---


    // Thong tin nguoi choi
    private final User player1;
    private final User player2;
    private final Channel channel1;
    private final Channel channel2;

    // Trang thai doc lap cua moi nguoi choi
    private int player1Score = 0;
    private int player2Score = 0;
    private int player1CurrentIndex = 0;
    private int player2CurrentIndex = 0;
    private boolean player1Finished = false;
    private boolean player2Finished = false;

    // Trang thai chung
    private final String sessionId;
    private final int[] targetNumbers = new int[TOTAL_NUMBERS];
    
    // --- THEM MANG DIEU KIEN ---
    private final Constraint[] constraints = new Constraint[TOTAL_NUMBERS];
    
    private volatile boolean isGameRunning = false;
    private int gameTimeRemaining = GAME_DURATION_SECONDS;

    // Dependencies (Dich vu)
    private final ExpressionEvaluator evaluator;
    private final ScheduledExecutorService timerService;
    private ScheduledFuture<?> gameTimerFuture;
    private ScheduledFuture<?> gameEndFuture;
    
    // DAO va Services
    private final ConnectionManager connManager;
    private final GameManager gameManager;
    private final LobbyService lobbyService;
    private final GameResultDao gameResultDao;
    private final UserDao userDao;
    
    // Them Random de tao dieu kien
    private final Random random = new Random();

    public GameSession(User player1, Channel channel1, User player2, Channel channel2) {
        this.sessionId = UUID.randomUUID().toString();
        this.player1 = player1;
        this.player2 = player2;
        this.channel1 = channel1;
        this.channel2 = channel2;

        this.evaluator = new ExpressionEvaluator();
        this.timerService = Executors.newScheduledThreadPool(2);
        
        // Lay cac Singleton
        this.connManager = ConnectionManager.getInstance();
        this.gameManager = GameManager.getInstance();
        this.lobbyService = LobbyService.getInstance();
        this.gameResultDao = GameResultDao.getInstance();
        this.userDao = UserDao.getInstance();
        
        System.out.println("GS [" + sessionId + "]: Khoi tao thanh cong cho " + player1.getUsername() + " va " + player2.getUsername());
    }

    public String getSessionId(){ return sessionId; }
    public User getPlayer1() { return this.player1; }
    public User getPlayer2() { return this.player2; }
    public Channel getChannel1() { return channel1; }
    public Channel getChannel2() { return channel2; }
    public boolean isPlayerInSession(Channel channel) { return channel.equals(channel1) || channel.equals(channel2); }

    /**
     * (DA CAP NHAT: Them generateConstraints() va gui dieu kien)
     */
    @Override
    public void run() {
        if (isGameRunning) return;
        isGameRunning = true;

        System.out.println("GS [" + sessionId + "]: Bat dau!");

        // 1. Tao bo so va bo dieu kien
        generateNumbers();
        generateConstraints(); // <-- TAO MOI DIEU KIEN

        // 2. Bat dau Timer
        startTimer();

        // 3. Gui goi tin GameStart cho ca 2 (DA THEM DIEU KIEN)
        int firstTarget = targetNumbers[0];
        Constraint firstConstraint = constraints[0];
        
        GameStartPacket packet1 = new GameStartPacket(player2, firstTarget, firstConstraint.description());
        GameStartPacket packet2 = new GameStartPacket(player1, firstTarget, firstConstraint.description());

        channel1.writeAndFlush(packet1);
        channel2.writeAndFlush(packet2);
        
        System.out.println("GS [" + sessionId + "]: Gui GameStartPacket. Target: " + firstTarget + ", Dieu kien: " + firstConstraint.description());
    }

    private void generateNumbers() {
        Random rand = new Random();
        for (int i = 0; i < TOTAL_NUMBERS; i++) {
            targetNumbers[i] = rand.nextInt(291) + 10;
        }
        System.out.println("GS [" + sessionId + "]: Da tao " + TOTAL_NUMBERS + " so.");
    }
    
    // --- HAM TAO DIEU KIEN MOI ---
    
    /**
     * Tao 30 dieu kien ngau nhien
     */
    private void generateConstraints() {
        for (int i = 0; i < TOTAL_NUMBERS; i++) {
            constraints[i] = generateRandomConstraint();
        }
        System.out.println("GS [" + sessionId + "]: Da tao " + TOTAL_NUMBERS + " dieu kien.");
    }
    
    /**
     * Tao mot dieu kien ngau nhien (1 trong 3 loai)
     */
    private Constraint generateRandomConstraint() {
        int type = random.nextInt(3); // Chon 0, 1, hoac 2
        
        switch (type) {
            case 0: // So toan hang
                int operandCount = random.nextInt(6) + 1; // 1 den 6
                return new Constraint(ConstraintType.OPERAND_COUNT, 
                                      "Dieu kien: Phai dung " + operandCount + " toan hang", 
                                      operandCount, null);
            
            case 1: // So toan tu
                int operatorCount = random.nextInt(6); // 0 den 5
                return new Constraint(ConstraintType.OPERATOR_COUNT, 
                                      "Dieu kien: Phai dung " + operatorCount + " toan tu", 
                                      operatorCount, null);

            case 2: // Yeu cau toan tu
            default:
                String[] ops = {"+", "-", "*", "/"};
                String requiredOp = ops[random.nextInt(4)];
                return new Constraint(ConstraintType.OPERATOR_REQUIRED, 
                                      "Dieu kien: Phai chua toan tu '" + requiredOp + "'", 
                                      0, requiredOp);
        }
    }
    
    /**
     * Kiem tra xem bieu thuc co thoa man dieu kien khong
     */
    private boolean checkConstraint(String expression, Constraint constraint) {
        if (constraint == null) return true; // Neu khong co dieu kien, luon dung

        try {
            switch (constraint.type()) {
                case OPERAND_COUNT:
                    // Dem so toan hang (cac so)
                    // Split theo cac toan tu va khoang trang
                    String[] operands = expression.split("[+\\-*/()\\s]+");
                    int count = 0;
                    for (String op : operands) {
                        if (!op.trim().isEmpty()) { // Bo qua cac chuoi rong
                            count++;
                        }
                    }
                    boolean match = (count == constraint.value());
                    if (!match) System.out.println("GS [Constraint Check]: That bai OPERAND_COUNT. Yeu cau: " + constraint.value() + ", Thuc te: " + count);
                    return match;

                case OPERATOR_COUNT:
                    // Dem so toan tu
                    // Loai bo tat ca ky tu khong phai la toan tu
                    int opCount = expression.replaceAll("[^+\\-*/]", "").length();
                    match = (opCount == constraint.value());
                    if (!match) System.out.println("GS [Constraint Check]: That bai OPERATOR_COUNT. Yeu cau: " + constraint.value() + ", Thuc te: " + opCount);
                    return match;

                case OPERATOR_REQUIRED:
                    // Kiem tra co chua toan tu yeu cau khong
                    match = expression.contains(constraint.operator());
                    if (!match) System.out.println("GS [Constraint Check]: That bai OPERATOR_REQUIRED. Yeu cau: '" + constraint.operator() + "'");
                    return match;
            }
        } catch (Exception e) {
            System.err.println("GS [Constraint Check]: Loi khi kiem tra dieu kien: " + e.getMessage());
            return false; // Loi xay ra, coi nhu that bai
        }
        return false;
    }

    // --- KET THUC HAM TAO DIEU KIEN ---

    private void startTimer() {
        gameTimerFuture = timerService.scheduleAtFixedRate(this::tick, 1, 1, TimeUnit.SECONDS);
        gameEndFuture = timerService.schedule(() -> {
            System.out.println("GS [" + sessionId + "]: Timer 2 phut da het. Goi endGame(false).");
            endGame(false);
        }, GAME_DURATION_SECONDS, TimeUnit.SECONDS);
    }

    private void tick() {
        if (!isGameRunning) return;
        gameTimeRemaining--;
        GameTimerPacket timerPacket = new GameTimerPacket(gameTimeRemaining);
        if (channel1.isOpen()) channel1.writeAndFlush(timerPacket);
        if (channel2.isOpen()) channel2.writeAndFlush(timerPacket);
    }

    /**
     * (DA CAP NHAT: Kiem tra them dieu kien)
     */
    public synchronized void handlePlayerAnswer(Channel channel, String expression) {
        if (!isGameRunning) {
            System.out.println("GS [" + sessionId + "]: Nhan duoc cau tra loi sau khi game ket thuc.");
            return;
        }

        // --- 2. Xac dinh nguoi choi va kenh doi thu ---
        boolean isPlayer1 = channel.equals(channel1);
        User answeringPlayer = isPlayer1 ? player1 : player2;
        Channel opponentChannel = isPlayer1 ? channel2 : channel1; 
        
        int currentScore, currentIndex;
        
        if (isPlayer1) {
            currentScore = player1Score;
            currentIndex = player1CurrentIndex;
            if (player1Finished) return; 
        } else {
            currentScore = player2Score;
            currentIndex = player2CurrentIndex;
            if (player2Finished) return; 
        }
        
        System.out.println("GS [" + sessionId + "]: Nhan SUBMIT_ANSWER tu " + answeringPlayer.getUsername() + 
                           ". Cau " + (currentIndex + 1) + ". Bieu thuc: [" + expression + "]");

        // --- 3. Lay so muc tieu va dieu kien hien tai ---
        int target = targetNumbers[currentIndex];
        Constraint constraint = constraints[currentIndex]; // <-- LAY DIEU KIEN HIEN TAI
        
        double res = Double.NaN;
        try {
            Double resultObject = evaluator.evaluate(expression);
            if (resultObject != null) res = resultObject.doubleValue();
        } catch (Exception e) {
            System.err.println("GS [" + sessionId + "]: LOI DANH GIA (Exception) cho '" + expression + "': " + e.getMessage());
        }

        // --- 4. Xử lý kết quả (Dúng & Thoa man dieu kien) ---
        long result;
        if (Double.isNaN(res) || Double.isInfinite(res)) {
            result = Long.MIN_VALUE; 
            System.out.println("GS [" + sessionId + "]: " + answeringPlayer.getUsername() + " tra loi SAI (Bieu thuc khong hop le).");
        } else {
            result = Math.round(res);
            
            // --- LOGIC MOI: Kiem tra ca ket qua VA dieu kien ---
            boolean constraintMet = checkConstraint(expression, constraint);
            
            if (result == target && constraintMet) { // PHAI DUNG CA HAI
                currentScore++;
                System.out.println("GS [" + sessionId + "]: " + answeringPlayer.getUsername() + " tra loi DUNG. Diem cu: " + (currentScore - 1) + ", Diem moi: " + currentScore);
            } else {
                System.out.println("GS [" + sessionId + "]: " + answeringPlayer.getUsername() + " tra loi SAI. (KQ: " + result + " vs " + target + " | DieuKienOk: " + constraintMet + ")");
            }
        }
        
        // --- 5. Cap nhat Index va Diem ---
        currentIndex++;
        int nextTargetNumber = -1;
        String nextConstraintDesc = ""; // <-- Dieu kien tiep theo

        if (isPlayer1) {
            player1Score = currentScore;
            player1CurrentIndex = currentIndex;
            if (currentIndex >= TOTAL_NUMBERS) player1Finished = true;
        } else {
            player2Score = currentScore;
            player2CurrentIndex = currentIndex;
            if (currentIndex >= TOTAL_NUMBERS) player2Finished = true;
        }
        
        if (currentIndex < TOTAL_NUMBERS) {
            nextTargetNumber = targetNumbers[currentIndex];
            nextConstraintDesc = constraints[currentIndex].description(); // <-- Lay dieu kien tiep theo
        }

        // --- 6. GUI GÓI TIN CẬP NHẬT (DA THEM DIEU KIEN) ---
        int yourScore = isPlayer1 ? player1Score : player2Score;
        int opponentScore = isPlayer1 ? player2Score : player1Score;

        // 6a. Gui cho Nguoi vua tra loi (GameStateUpdatePacket)
        GameStateUpdatePacket updatePacket = new GameStateUpdatePacket(
                yourScore, opponentScore, nextTargetNumber, nextConstraintDesc // <-- Gui dieu kien moi
        );
        channel.writeAndFlush(updatePacket);
        System.out.println("GS [" + sessionId + "]: Gui GameStateUpdate cho " + answeringPlayer.getUsername() + " (Diem ban: " + yourScore + ", Diem dich: " + opponentScore + ", NextTarget: " + nextTargetNumber + ")");

        // 6b. Gui cho DOI THU (ScoreUpdatePacket)
        if (opponentChannel != null && opponentChannel.isOpen()) {
            ScoreUpdatePacket scorePacket = new ScoreUpdatePacket(opponentScore, yourScore); 
            opponentChannel.writeAndFlush(scorePacket);
            System.out.println("GS [" + sessionId + "]: Gui ScoreUpdatePacket cho DOI THU (MyScore: " + opponentScore + ", OpponentScore: " + yourScore + ")");
        } else {
             System.out.println("GS [" + sessionId + "]: Khong gui ScoreUpdatePacket vi doi thu da ngat ket noi.");
        }
        
        // --- 7. Kiem tra Ket thuc Game ---
        if (player1Finished && player2Finished) {
            System.out.println("GS [" + sessionId + "]: Ca hai da xong. Ket thuc game som.");
            endGame(false); 
        }
    }

    /**
     * Xu ly khi mot nguoi choi thoat (bi ngat ket noi hoac bam nut Thoat).
     */
    public synchronized void playerLeftGame(Channel channel) {
        if (!isGameRunning) return;

        boolean isPlayer1 = channel.equals(channel1);
        User leavingPlayer = isPlayer1 ? player1 : player2;
        Channel stayingChannel = isPlayer1 ? channel2 : channel1;

        System.out.println("GS [" + sessionId + "]: " + leavingPlayer.getUsername() + " da thoat.");

        if (isPlayer1) {
            player1Score = -1;
            player2Score = Math.max(player2Score, player1Score + 1); 
        } else {
            player2Score = -1;
            player1Score = Math.max(player1Score, player2Score + 1);
        }

        endGame(true);

        if (stayingChannel.isOpen()) {
            stayingChannel.writeAndFlush(new OpponentExitPacket());
        }
    }

    private synchronized void endGame(boolean forcedExit) {
        if (!isGameRunning) return; 
        isGameRunning = false;

        System.out.println("GS [" + sessionId + "]: Dang ket thuc game... (forced=" + forcedExit + ")");

        // Dung timer
        if (gameTimerFuture != null) gameTimerFuture.cancel(true);
        if (gameEndFuture != null) gameEndFuture.cancel(true);
        timerService.shutdown();

        // 1. Xac dinh nguoi thang
        String winnerId = null;
        if (player1Score > player2Score) {
            winnerId = player1.getId();
        } else if (player2Score > player1Score) {
            winnerId = player2.getId();
        }
        System.out.println("GS [" + sessionId + "]: Xac nhan nguoi thang. Diem P1: " + player1Score + ", Diem P2: " + player2Score + ". WinnerID: " + (winnerId != null ? winnerId : "Hoa"));

        // 2. Tao doi tuong GameResult
        GameResult result = new GameResult();
        result.setGameId(sessionId);
        result.setPlayer1Id(player1.getId());
        result.setPlayer2Id(player2.getId());
        result.setPlayer1Username(player1.getUsername());
        result.setPlayer2Username(player2.getUsername());
        result.setPlayer1Score(player1Score);
        result.setPlayer2Score(player2Score);
        result.setWinnerId(winnerId);
        result.setTimestamp(new Date());

        // 3. Luu CSDL
        gameResultDao.saveGameResult(result);
        System.out.println("GS [" + sessionId + "]: Da luu ket qua CSDL.");

        // Cap nhat Stats
        if (!forcedExit) {
            boolean player1Won = (winnerId != null && winnerId.equals(player1.getId()));
            boolean player2Won = (winnerId != null && winnerId.equals(player2.getId()));
            userDao.updateUserStats(player1.getId(), player1Score, player1Won);
            userDao.updateUserStats(player2.getId(), player2Score, player2Won);
        } else if (winnerId != null) {
            User winner = winnerId.equals(player1.getId()) ? player1 : player2;
            int winnerScore = winnerId.equals(player1.getId()) ? player1Score : player2Score;
            userDao.updateUserStats(winner.getId(), winnerScore, true);
        }

        // Tai lai thong tin User moi nhat
        User updatedPlayer1 = userDao.getUserById(player1.getId());
        User updatedPlayer2 = userDao.getUserById(player2.getId());
        
        // 4. Gui GameOverPacket
        GameOverPacket gameOverPacket = new GameOverPacket(result);
        if (channel1.isOpen()) channel1.writeAndFlush(gameOverPacket);
        if (channel2.isOpen()) channel2.writeAndFlush(gameOverPacket); 
        System.out.println("GS [" + sessionId + "]: Gui GameOverPacket. WinnerID: " + (winnerId != null ? winnerId : "Hoa"));

        // 5. GUI GOI TIN CAP NHAT USER
        if (updatedPlayer1 != null && channel1.isOpen()) {
            channel1.writeAndFlush(new UserUpdatePacket(updatedPlayer1));
            System.out.println("GS [" + sessionId + "]: Gui UserUpdatePacket cho " + updatedPlayer1.getUsername());
        }
        if (updatedPlayer2 != null && channel2.isOpen()) {
            channel2.writeAndFlush(new UserUpdatePacket(updatedPlayer2));
            System.out.println("GS [" + sessionId + "]: Gui UserUpdatePacket cho " + updatedPlayer2.getUsername());
        }
        
        // 6. Cap nhat trang thai Lobby VA DANG KY "CHOI LAI"
        if (channel1.isOpen()) {
            lobbyService.setUserStatus(player1, PlayerStatus.AVAILABLE, false);
        }
        if (channel2.isOpen()) {
            lobbyService.setUserStatus(player2, PlayerStatus.AVAILABLE, false);
        }
        
        if (!forcedExit) {
            lobbyService.registerLastOpponents(player1.getId(), player2.getId());
        }

        lobbyService.broadcastOnlineList(); 
        
        // 7. Xoa session nay khoi GameManager
        gameManager.removeGame(this);
        System.out.println("GS [" + sessionId + "]: Da don dep xong.");
    }
}