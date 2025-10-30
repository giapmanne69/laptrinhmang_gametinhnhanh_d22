package servergameserver.manager;

import commongameserver.model.User;
import commongameserver.network.Packet;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lớp Singleton, chịu trách nhiệm theo dõi tất cả các kết nối (Channel)
 * và ánh xạ chúng với thông tin User đã đăng nhập.
 * Lớp này BẮT BUỘC phải là thread-safe (an toàn đa luồng).
 */
public class ConnectionManager {

    // --- Singleton Pattern ---
    private static final ConnectionManager instance = new ConnectionManager();

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        return instance;
    }

    // --- State ---
    // Sử dụng ConcurrentHashMap để đảm bảo thread-safe

    // Ánh xạ từ UserId (String) -> Channel (để gửi tin nhắn)
    private final Map<String, Channel> userToChannelMap = new ConcurrentHashMap<>();
    
    // Ánh xạ từ Channel -> User (để tra cứu khi có tin nhắn đến)
    private final Map<Channel, User> channelToUserMap = new ConcurrentHashMap<>();

    /**
     * Đăng ký một người dùng (sau khi login thành công).
     */
    public void registerUser(User user, Channel channel) {
        if (user == null || channel == null) {
            return;
        }
        String userId = user.getId();
        
        // Lưu cả hai chiều ánh xạ
        userToChannelMap.put(userId, channel);
        channelToUserMap.put(channel, user);
        
        System.out.println("ConnectionManager: Đã đăng ký User " + user.getUsername() + " với Kênh " + channel.id());
    }

    /**
     * Hủy đăng ký một người dùng (khi ngắt kết nối).
     * @return Trả về User vừa bị ngắt kết nối,
     * hoặc null nếu kênh này chưa được đăng ký.
     */
    public User removeUser(Channel channel) {
        if (channel == null) {
            return null;
        }
        
        // 1. Lấy User từ Kênh
        User user = channelToUserMap.remove(channel);
        
        // 2. Nếu User tồn tại, xóa ánh xạ ngược lại
        if (user != null) {
            userToChannelMap.remove(user.getId());
            System.out.println("ConnectionManager: Đã hủy đăng ký User " + user.getUsername() + " (Kênh " + channel.id() + ")");
        } else {
             System.out.println("ConnectionManager: Kênh " + channel.id() + " bị ngắt kết nối (chưa đăng nhập).");
        }
        
        return user;
    }

    /**
     * Lấy User dựa trên Channel.
     */
    public User getUser(Channel channel) {
        return channelToUserMap.get(channel);
    }

    /**
     * Lấy User dựa trên UserId.
     * (Hàm này có thể cần thiết cho LobbyService)
     */
    public User getUser(String userId) {
        Channel channel = userToChannelMap.get(userId);
        if (channel != null) {
            return channelToUserMap.get(channel);
        }
        return null;
    }

    /**
     * Lấy Channel dựa trên UserId.
     */
    public Channel getChannel(String userId) {
        return userToChannelMap.get(userId);
    }
    
    /**
     * Lấy danh sách tất cả User đang online (đã đăng ký).
     */
    public List<User> getOnlineUsers() {
        // Trả về một bản sao (copy) của danh sách
        return new ArrayList<>(channelToUserMap.values());
    }

    /**
     * Gửi một gói tin đến TẤT CẢ các client đã đăng nhập
     * và đang kết nối.
     * Được gọi bởi LobbyService.broadcastOnlineList().
     *
     * @param packet Gói tin cần gửi (ví dụ: OnlineListPacket)
     */
    public void broadcast(Packet packet) {
        if (packet == null) {
            return;
        }
        
        System.out.println("ConnectionManager: Đang broadcast " + packet.getType() + " đến " + channelToUserMap.size() + " clients.");

        // Duyệt qua tất cả các kênh (Channel) đang được quản lý
        for (Channel channel : channelToUserMap.keySet()) {
            // Kiểm tra xem kênh có còn hoạt động và đang mở không
            if (channel != null && channel.isOpen() && channel.isActive()) {
                channel.writeAndFlush(packet);
            }
        }
    }
}

