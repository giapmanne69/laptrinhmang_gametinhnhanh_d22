package commongameserver.network.request;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ CLIENT -> SERVER khi người chơi
 * muốn thoát khỏi trận đấu hiện tại.
 *
 * Gói tin này không cần chứa thêm dữ liệu,
 * vì Server có thể xác định người chơi nào gửi từ Channel.
 */
public class ExitGameRequest extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 105L;

    /**
     * Constructor rỗng
     */
    public ExitGameRequest() {
        // Không cần trường dữ liệu nào
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là EXIT_GAME_REQUEST
     */
    @Override
    public PacketType getType() {
        return PacketType.EXIT_GAME_REQUEST;
    }

    @Override
    public String toString() {
        return "ExitGameRequest{}";
    }
}
