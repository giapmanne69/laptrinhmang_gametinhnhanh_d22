package servergameserver.service;

import commongameserver.model.User;
import io.netty.channel.Channel;
import servergameserver.game.GameSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lớp Singleton, quản lý việc tạo, theo dõi và hủy các GameSession.
 * Đây là "trung tâm điều phối" cho tất cả các ván đấu đang diễn ra.
 */
public class GameManager {

    // --- Singleton Pattern ---
    private static final GameManager instance = new GameManager();
    private final Map<String, GameSession> userIdToSessionMap = new ConcurrentHashMap<>();

    private GameManager() {
        // Sử dụng một Thread Pool để chạy các GameSession
        // Mỗi GameSession là một Runnable
        this.gameExecutor = Executors.newCachedThreadPool();
    }

    public static GameManager getInstance() {
        return instance;
    }

    // --- State ---

    // Chứa tất cả các session đang hoạt động,
    // key là sessionId (từ GameSession)
    private final Map<String, GameSession> activeSessions = new ConcurrentHashMap<>();

    // Ánh xạ Kênh (Channel) tới GameSession
    // để tìm kiếm nhanh khi có packet đến
    private final Map<Channel, GameSession> channelToSessionMap = new ConcurrentHashMap<>();

    // Dùng để chạy các GameSession (mỗi session là một Runnable)
    private final ExecutorService gameExecutor;

    /**
     * Tạo một GameSession mới và bắt đầu chạy nó.
     * Được gọi bởi LobbyService khi 2 người chơi đồng ý.
     */
    public void createGame(User player1, Channel channel1, User player2, Channel channel2) {
        System.out.println("GameManager: Đang tạo game cho " + player1.getUsername() + " và " + player2.getUsername());

        // 1. Tạo mới GameSession
        GameSession newSession = new GameSession(player1, channel1, player2, channel2);

        // 2. Lưu trữ và ánh xạ
        // (Lấy sessionId từ bên trong session,
        // nhưng chúng ta có thể dùng channel làm key chính)
        activeSessions.put(newSession.getSessionId(), newSession);
        channelToSessionMap.put(channel1, newSession);
        channelToSessionMap.put(channel2, newSession);

        userIdToSessionMap.put(player1.getId(), newSession);
        userIdToSessionMap.put(player2.getId(), newSession);

        // 3. Khởi chạy GameSession trên một luồng riêng
        // (Vì GameSession implement Runnable)
        gameExecutor.execute(newSession);
    }

    /**
     * Dọn dẹp một GameSession sau khi nó kết thúc.
     * Được gọi từ bên trong GameSession.endGame().
     */
    public synchronized void removeGame(GameSession session) {
        if (session == null) return;

        System.out.println("GameManager: Đang xóa game " + session.getSessionId());

        activeSessions.remove(session.getSessionId());
        channelToSessionMap.remove(session.getChannel1());
        channelToSessionMap.remove(session.getChannel2());

        if (session.getPlayer1() != null) {
            userIdToSessionMap.remove(session.getPlayer1().getId());
        }
        if (session.getPlayer2() != null) {
            userIdToSessionMap.remove(session.getPlayer2().getId());}
    }

    /**
     * Tìm GameSession dựa trên Kênh (Channel) của người chơi.
     * Được gọi bởi LobbyService/ServerHandler khi điều phối packet.
     *
     * @param channel Kênh của người chơi
     * @return GameSession, hoặc null nếu không tìm thấy
     */
    public GameSession getSession(Channel channel) {
        return channelToSessionMap.get(channel);
    }

    /**
     * Xử lý logic khi một người chơi ngắt kết nối hoặc thoát.
     * Được gọi bởi LobbyService.
     */
    public void playerLeftGame(User user, Channel channel) {
        GameSession session = getSession(channel);
        if (session != null) {
            // Ủy quyền cho GameSession tự xử lý logic thoát
            session.playerLeftGame(channel);
        }
    }

    /**
     * Kiểm tra xem một User (dựa trên ID) có đang ở trong
     * bất kỳ GameSession nào không.
     */
    public boolean isUserInGame(String userId) {
        // Phương pháp này hơi chậm (O(n)),
        // nhưng an toàn và không cần thêm map ánh xạ
        // (UserId -> GameSession)
        for (GameSession session : activeSessions.values()) {
            if (session.getPlayer1().getId().equals(userId) ||
                    session.getPlayer2().getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }
}

