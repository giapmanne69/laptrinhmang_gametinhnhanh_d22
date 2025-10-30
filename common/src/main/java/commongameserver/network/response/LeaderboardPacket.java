package commongameserver.network.response;

import commongameserver.model.User;
import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;
import java.util.List;

/**
 * Gói tin được gửi TỪ SERVER -> CLIENT
 * để trả lời yêu cầu xem bảng xếp hạng.
 *
 * Gói tin này chứa một danh sách (List) các đối tượng User
 * đã được sắp xếp theo thứ hạng.
 */
public class LeaderboardPacket extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 208L; // ID cho LeaderboardPacket

    /**
     * Danh sách người chơi đã được sắp xếp theo thứ hạng.
     */
    private List<User> leaderboard;

    /**
     * Constructor rỗng
     * Cần thiết cho việc deserialization.
     */
    public LeaderboardPacket() {
    }

    /**
     * Constructor
     * @param leaderboard Danh sách User đã sắp xếp
     */
    public LeaderboardPacket(List<User> leaderboard) {
        this.leaderboard = leaderboard;
    }

    // --- Getter và Setter ---

    public List<User> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(List<User> leaderboard) {
        this.leaderboard = leaderboard;
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là LEADERBOARD_PACKET
     */
    @Override
    public PacketType getType() {
        // Giả định PacketType.java đã có LEADERBOARD_PACKET
        return PacketType.LEADERBOARD_PACKET;
    }

    @Override
    public String toString() {
        return "LeaderboardPacket{" +
                "leaderboardSize=" + (leaderboard != null ? leaderboard.size() : 0) +
                '}';
    }
}
