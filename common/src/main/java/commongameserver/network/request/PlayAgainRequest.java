package commongameserver.network.request;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ CLIENT -> SERVER khi người chơi
 * muốn chơi lại với đối thủ hiện tại sau khi ván đấu kết thúc.
 *
 * Server sẽ nhận gói tin này và thông báo cho đối thủ.
 * Gói tin này không cần chứa dữ liệu, vì Server biết
 * người chơi nào đã gửi nó và họ đang ở trong phiên chơi nào.
 */
public class PlayAgainRequest extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 104L; // ID cho PlayAgainRequest

    /**
     * Constructor rỗng
     */
    public PlayAgainRequest() {
        // Không cần trường dữ liệu nào
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là PLAY_AGAIN_REQUEST
     */
    @Override
    public PacketType getType() {
        return PacketType.PLAY_AGAIN_REQUEST;
    }

    @Override
    public String toString() {
        return "PlayAgainRequest{}";
    }
}
