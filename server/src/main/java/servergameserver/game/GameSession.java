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
import commongameserver.network.response.UserUpdatePacket; // THÊM IMPORT NÀY
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

/**
 * Quan ly logic cho MOT van dau (doc lap) giua 2 nguoi choi.
 * Phien ban nay ho tro viec tra loi doc lap.
 */
public class GameSession implements Runnable {

    // Hằng số
    private static final int TOTAL_NUMBERS = 30;
    private static final int GAME_DURATION_SECONDS = 120; // 2 phut

    // Thông tin người chơi
    private final User player1;
    private final User player2;
    private final Channel channel1;
    private final Channel channel2;

    // Trạng thái độc lập của mỗi người chơi
    private int player1Score = 0;
    private int player2Score = 0;
    private int player1CurrentIndex = 0;
    private int player2CurrentIndex = 0;
    private boolean player1Finished = false;
    private boolean player2Finished = false;

    // Trạng thái chung
    private final String sessionId;
    private final int[] targetNumbers = new int[TOTAL_NUMBERS];
    private volatile boolean isGameRunning = false;
    private int gameTimeRemaining = GAME_DURATION_SECONDS;

    // Dependencies (Dịch vụ)
    private final ExpressionEvaluator evaluator;
    private final ScheduledExecutorService timerService;
    private ScheduledFuture<?> gameTimerFuture;
    private ScheduledFuture<?> gameEndFuture;
    
    // DAO và Services
    private final ConnectionManager connManager;
    private final GameManager gameManager;
    private final LobbyService lobbyService;
    private final GameResultDao gameResultDao;
    private final UserDao userDao;

    public GameSession(User player1, Channel channel1, User player2, Channel channel2) {
        this.sessionId = UUID.randomUUID().toString();
        this.player1 = player1;
        this.player2 = player2;
        this.channel1 = channel1;
        this.channel2 = channel2;

        this.evaluator = new ExpressionEvaluator();
        this.timerService = Executors.newScheduledThreadPool(2);
        
        // Lấy các Singleton
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

    @Override
    public void run() {
        if (isGameRunning) return;
        isGameRunning = true;

        System.out.println("GS [" + sessionId + "]: Bat dau!");

        // 1. Tao bo so
        generateNumbers();

        // 2. Bat dau Timer
        startTimer();

        // 3. Gui goi tin GameStart cho ca 2
        int firstTarget = targetNumbers[0];
        GameStartPacket packet1 = new GameStartPacket(player2, firstTarget);
        GameStartPacket packet2 = new GameStartPacket(player1, firstTarget);

        channel1.writeAndFlush(packet1);
        channel2.writeAndFlush(packet2);
        
        System.out.println("GS [" + sessionId + "]: Gui GameStartPacket voi Target: " + firstTarget);
    }

    private void generateNumbers() {
        Random rand = new Random();
        for (int i = 0; i < TOTAL_NUMBERS; i++) {
            targetNumbers[i] = rand.nextInt(291) + 10;
        }
        System.out.println("GS [" + sessionId + "]: Da tao " + TOTAL_NUMBERS + " so.");
    }

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
     * Xu ly khi mot nguoi choi nop dap an.
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
        
        // LOG BUOC 2: TRUOC KHI DANH GIA
        System.out.println("GS [" + sessionId + "]: Nhan SUBMIT_ANSWER tu " + answeringPlayer.getUsername() + 
                           ". Cau " + (currentIndex + 1) + ". Bieu thuc: [" + expression + "]");


        // --- 3. Danh gia phep tinh (Co try-catch an toan) ---
        int target = targetNumbers[currentIndex];
        double res = Double.NaN;
        try {
            Double resultObject = evaluator.evaluate(expression);
            if (resultObject != null) {
                res = resultObject.doubleValue();
            }
        } catch (Exception e) {
            System.err.println("GS [" + sessionId + "]: LOI DANH GIA (Exception) cho '" + expression + "': " + e.getMessage());
        }

        // --- 4. Xử lý kết quả (Đúng hoặc Sai) ---
        long result;
        if (Double.isNaN(res) || Double.isInfinite(res)) {
            result = Long.MIN_VALUE; 
            System.out.println("GS [" + sessionId + "]: " + answeringPlayer.getUsername() + " tra loi SAI (Bieu thuc khong hop le).");
        } else {
            result = Math.round(res);
            if (result == target) {
                currentScore++;
                System.out.println("GS [" + sessionId + "]: " + answeringPlayer.getUsername() + " tra loi DUNG. Diem cu: " + (currentScore - 1) + ", Diem moi: " + currentScore);
            } else {
                System.out.println("GS [" + sessionId + "]: " + answeringPlayer.getUsername() + " tra loi SAI. KQ: " + result + ", MT: " + target);
            }
        }
        
        // --- 5. Cap nhat Index va Diem ---
        currentIndex++;
        int nextTargetNumber = -1;

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
        }

        // --- 6. GUI GÓI TIN CẬP NHẬT (Packet Sending) ---
        int yourScore = isPlayer1 ? player1Score : player2Score;
        int opponentScore = isPlayer1 ? player2Score : player1Score;

        // 6a. Gui cho Nguoi vua tra loi (GameStateUpdatePacket)
        GameStateUpdatePacket updatePacket = new GameStateUpdatePacket(
                yourScore, opponentScore, nextTargetNumber
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

        // Gan diem (Player 1 thoat thi Player 2 thang, va nguoc lai)
        // Set diem cua nguoi thoat la -1 de phan biet
        if (isPlayer1) {
            player1Score = -1;
            player2Score = Math.max(player2Score, player1Score + 1); // Dam bao nguoi o lai luon thang
        } else {
            player2Score = -1;
            player1Score = Math.max(player1Score, player2Score + 1);
        }

        // Ket thuc game, co 'forcedExit' = true
        endGame(true);

        // Gui thong bao cho nguoi o lai
        if (stayingChannel.isOpen()) {
            stayingChannel.writeAndFlush(new OpponentExitPacket());
        }
    }

    private synchronized void endGame(boolean forcedExit) {
        if (!isGameRunning) return; // Đảm bảo chỉ chạy 1 lần
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
        // LOG BUOC 1: Xac nhan nguoi thang
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

        // Cap nhat Stats (Logic da sua o luot truoc)
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

        // --- BƯỚC MỚI: TẢI LẠI VÀ GỬI USER CẬP NHẬT ---
        // Tải lại thông tin User mới nhất từ CSDL (đã có điểm và trận thắng mới)
        User updatedPlayer1 = userDao.getUserById(player1.getId());
        User updatedPlayer2 = userDao.getUserById(player2.getId());
        
        // 4. Gửi GameOverPacket (FIX LỖI: Gửi đúng channel2)
        GameOverPacket gameOverPacket = new GameOverPacket(result);
        if (channel1.isOpen()) channel1.writeAndFlush(gameOverPacket);
        if (channel2.isOpen()) channel2.writeAndFlush(gameOverPacket); // Đã sửa lỗi ở đây
        System.out.println("GS [" + sessionId + "]: Gui GameOverPacket. WinnerID: " + (winnerId != null ? winnerId : "Hoa"));

        // 5. GỬI GÓI TIN CẬP NHẬT USER
        if (updatedPlayer1 != null && channel1.isOpen()) {
            channel1.writeAndFlush(new UserUpdatePacket(updatedPlayer1));
            System.out.println("GS [" + sessionId + "]: Gui UserUpdatePacket cho " + updatedPlayer1.getUsername());
        }
        if (updatedPlayer2 != null && channel2.isOpen()) {
            channel2.writeAndFlush(new UserUpdatePacket(updatedPlayer2));
            System.out.println("GS [" + sessionId + "]: Gui UserUpdatePacket cho " + updatedPlayer2.getUsername());
        }
        
        // 6. Cập nhật trạng thái Lobby VÀ ĐĂNG KÝ "CHOI LAI"
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
