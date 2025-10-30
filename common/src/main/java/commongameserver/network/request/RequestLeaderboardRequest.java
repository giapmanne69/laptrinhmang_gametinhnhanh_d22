package commongameserver.network.request;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ CLIENT -> SERVER khi người chơi
 * muốn xem bảng xếp hạng.
 *
 * Gói tin này không cần chứa dữ liệu, vì Server
 * chỉ cần biết yêu cầu này là đủ để truy vấn
 * và trả về LeaderboardPacket.
 */
public class RequestLeaderboardRequest extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 106L; // ID cho RequestLeaderboardRequest

    /**
     * Constructor rỗng
     */
    public RequestLeaderboardRequest() {
        // Không cần trường dữ liệu nào
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là REQUEST_LEADERBOARD_REQUEST
     */
    @Override
    public PacketType getType() {
        return PacketType.REQUEST_LEADERBOARD_REQUEST;
    }

    @Override
    public String toString() {
        return "RequestLeaderboardRequest{}";
    }
}
