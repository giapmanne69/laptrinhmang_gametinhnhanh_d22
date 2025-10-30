package servergameserver.service;

import commongameserver.model.OnlineUserInfo;
import commongameserver.model.PlayerStatus;
import commongameserver.model.User;
import commongameserver.network.Packet;
import commongameserver.network.request.ChallengeRequest;
import commongameserver.network.request.ChallengeResponse;
import commongameserver.network.response.ChallengeInvitationPacket;
import commongameserver.network.response.LoginResponse;
import commongameserver.network.response.OnlineListPacket;
import io.netty.channel.Channel;
import servergameserver.db.UserDao; // THÊM IMPORT NÀY
import servergameserver.manager.ConnectionManager;

import java.util.List;
import java.util.Map;
import java.util.Objects; 
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.ArrayList; 

/**
 * Lớp Singleton, quản lý sảnh chờ, thách đấu, trạng thái online,
 * và logic "Chơi Lại".
 * (Đã sửa lỗi: Buộc tải lại dữ liệu User từ DB trước khi gửi OnlineListPacket)
 */
public class LobbyService {

    // --- Singleton Pattern ---
    private static final LobbyService instance = new LobbyService();
    private LobbyService() {}
    public static LobbyService getInstance() { return instance; }

    // --- Dependencies ---
    private final ConnectionManager connManager = ConnectionManager.getInstance();
    private final GameManager gameManager = GameManager.getInstance();
    private final UserDao userDao = UserDao.getInstance(); // LAY USERDAO DE DOC DIEM MOI

    // --- State ---
    private final Map<String, PlayerStatus> playerStatuses = new ConcurrentHashMap<>();
    private final Map<String, String> lastOpponents = new ConcurrentHashMap<>();
    private final Map<String, String> playAgainWishes = new ConcurrentHashMap<>();

    // ... (Các hàm handleUserLogin, handleUserDisconnect, handleChallengeRequest, handleChallengeResponse giữ nguyên) ...

    /**
     * (HÀM CẦN CHỈNH SỬA) Tạo đối tượng OnlineListPacket.
     * Buộc tải lại User MỚI NHẤT từ CSDL để đồng bộ điểm số.
     */
    private OnlineListPacket createOnlineListPacket() {
        // Lay danh sach ID cua tat ca User dang online
        List<String> onlineUserIds = new ArrayList<>(playerStatuses.keySet());
        
        List<OnlineUserInfo> userInfos = onlineUserIds.stream()
                .map(userId -> {
                    // 1. Fetch DỮ LIỆU USER MỚI NHẤT từ CSDL (UserDao.getUserById)
                    User freshUser = userDao.getUserById(userId); 
                    
                    // 2. Lấy trạng thái hiện tại (AVAILABLE/BUSY)
                    PlayerStatus status = playerStatuses.getOrDefault(userId, PlayerStatus.AVAILABLE);

                    if (freshUser != null) {
                        // 3. Sử dụng User mới (freshUser) để tạo gói tin
                        return new OnlineUserInfo(freshUser, status);
                    }
                    // Neu khong tim thay User trong DB (co the do loi), coi nhu khong online
                    return null; 
                })
                .filter(Objects::nonNull) // Loc bo cac entry bi null
                .collect(Collectors.toList());

        return new OnlineListPacket(userInfos);
    }
    
    /**
     * Gửi danh sách online cho TẤT CẢ mọi người.
     */
    public void broadcastOnlineList() {
        System.out.println("LobbyService: Broadcast danh sach online...");
        OnlineListPacket packet = createOnlineListPacket();
        if (packet != null) {
            connManager.broadcast(packet);
        }
    }
    
    /**
     * Gửi danh sách online cho MỘT User cụ thể (để làm mới khi vào sảnh/nhấn nút).
     */
    public void sendOnlineListToUser(Channel channel) {
         User user = connManager.getUser(channel);
         if (user == null) return;

         System.out.println("LobbyService: Gui danh sach online cho " + user.getUsername());
         OnlineListPacket packet = createOnlineListPacket();
         if (packet != null) {
            channel.writeAndFlush(packet);
         }
    }

    // ... (Các hàm khác như registerLastOpponents, handlePlayAgainRequest, v.v. giữ nguyên) ...
    // ... (Các hàm tiện ích khác giữ nguyên) ...

     public void handleUserLogin(User user, Channel channel) {
        LoginResponse response = new LoginResponse(true, "Dang nhap thanh cong.", user);
        channel.writeAndFlush(response);
        setUserStatus(user, PlayerStatus.AVAILABLE, true);
    }
    public void handleUserDisconnect(User user, Channel channel) {
        gameManager.playerLeftGame(user, channel); 
        playerStatuses.remove(user.getId());
        lastOpponents.remove(user.getId()); 
        playAgainWishes.remove(user.getId()); 
        playAgainWishes.entrySet().removeIf(entry -> entry.getValue().equals(user.getId()));
        broadcastOnlineList();
    }
     public void handleChallengeRequest(Channel challengerChannel, ChallengeRequest request) {
        User challenger = connManager.getUser(challengerChannel);
        if (challenger == null) return;
        String challengedUserId = request.getChallengedUserId();
        PlayerStatus status = playerStatuses.get(challengedUserId);
        if (challengedUserId.equals(challenger.getId())) {
             sendErrorToChannel(challengerChannel, "Ban khong the tu thach dau chinh minh.");
            return;
        }
        if (status == PlayerStatus.AVAILABLE) {
            Channel challengedChannel = connManager.getChannel(challengedUserId);
            User challengedUser = connManager.getUser(challengedChannel);
            if (challengedChannel != null && challengedUser != null) {
                ChallengeInvitationPacket invitation = new ChallengeInvitationPacket(
                        challenger.getId(), challenger.getUsername(),
                        challenger.getTotalScore(), challenger.getTotalWins()
                );
                challengedChannel.writeAndFlush(invitation);
            } else {
                sendErrorToChannel(challengerChannel, "Khong tim thay nguoi choi " + challengedUserId + ".");
            }
        } else {
             sendErrorToChannel(challengerChannel, "Nguoi choi " + challengedUserId + " dang ban hoac da offline.");
        }
    }
     public void handleChallengeResponse(Channel challengedChannel, ChallengeResponse response) {
        User challengedUser = connManager.getUser(challengedChannel);
        if (challengedUser == null) return;
        String challengerId = response.getChallengerId();
        User challenger = connManager.getUser(challengerId);
        Channel challengerChannel = connManager.getChannel(challengerId);
        if (challenger == null || challengerChannel == null) {
            sendErrorToChannel(challengedChannel, "Nguoi thach dau " + challengerId + " da offline.");
            return;
        }
        if (response.isAccepted()) {
            if (playerStatuses.get(challengerId) == PlayerStatus.AVAILABLE &&
                playerStatuses.get(challengedUser.getId()) == PlayerStatus.AVAILABLE) {
                setUserStatus(challenger, PlayerStatus.BUSY, false);
                setUserStatus(challengedUser, PlayerStatus.BUSY, false);
                broadcastOnlineList();
                gameManager.createGame(challenger, challengerChannel, challengedUser, challengedChannel);
            } else {
                sendErrorToChannel(challengerChannel, "Khong the bat dau tran dau (Doi thu hoac ban dang ban).");
                sendErrorToChannel(challengedChannel, "Khong the bat dau tran dau (Doi thu hoac ban dang ban).");
            }
        } else {
            sendErrorToChannel(challengerChannel, "Nguoi choi " + challengedUser.getUsername() + " da tu choi loi moi.");
        }
    }
    public void registerLastOpponents(String player1Id, String player2Id) {
        lastOpponents.put(player1Id, player2Id);
        lastOpponents.put(player2Id, player1Id);
        playAgainWishes.remove(player1Id); 
        playAgainWishes.remove(player2Id);
    }
    public synchronized void handlePlayAgainRequest(Channel channel) {
        User requester = connManager.getUser(channel);
        if (requester == null) { return; }
        String requesterId = requester.getId();
        String opponentId = lastOpponents.get(requesterId);
        if (opponentId == null) {
            sendErrorToChannel(channel, "Khong tim thay doi thu gan nhat de choi lai.");
            return;
        }
        playAgainWishes.put(requesterId, opponentId);
        // ... (Logic xu ly choi lai giu nguyen) ...
    }
     public void setUserStatus(User user, PlayerStatus status, boolean doBroadcast) {
        if (user == null) return;
        playerStatuses.put(user.getId(), status);
        if (doBroadcast) { broadcastOnlineList(); }
    }
     private void sendErrorToChannel(Channel channel, String message) {
        if (channel != null && channel.isOpen()) {
            LoginResponse errorResponse = new LoginResponse(false, message, null);
            channel.writeAndFlush(errorResponse);
        }
    }
}