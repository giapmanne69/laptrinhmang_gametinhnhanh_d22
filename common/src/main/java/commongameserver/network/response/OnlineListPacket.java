package commongameserver.network.response;

import commongameserver.model.OnlineUserInfo;
import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;
import java.util.List;

/**
 * Gói tin được gửi TỪ SERVER -> CLIENT (tất cả client đang online)
 * mỗi khi có sự thay đổi về danh sách online (người mới
 * đăng nhập, đăng xuất, hoặc thay đổi trạng thái bận/rỗi).
 *
 * Chứa danh sách tất cả người chơi đang online và trạng thái của họ.
 */
public class OnlineListPacket extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 210L; // ID cho OnlineListPacket

    /**
     * Danh sách người chơi online và trạng thái của họ.
     */
    private List<OnlineUserInfo> onlineUsers;

    /**
     * Constructor rỗng
     * Cần thiết cho việc deserialization.
     */
    public OnlineListPacket() {
    }

    /**
     * Constructor
     * @param onlineUsers Danh sách người chơi
     */
    public OnlineListPacket(List<OnlineUserInfo> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    // --- Getter và Setter ---

    public List<OnlineUserInfo> getOnlineUsers() {
        return onlineUsers;
    }

    public void setOnlineUsers(List<OnlineUserInfo> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là ONLINE_LIST_PACKET
     */
    @Override
    public PacketType getType() {
        // Giả định PacketType.java đã có ONLINE_LIST_PACKET
        return PacketType.ONLINE_LIST_PACKET;
    }

    @Override
    public String toString() {
        return "OnlineListPacket{" +
                "userCount=" + (onlineUsers != null ? onlineUsers.size() : 0) +
                '}';
    }
}
