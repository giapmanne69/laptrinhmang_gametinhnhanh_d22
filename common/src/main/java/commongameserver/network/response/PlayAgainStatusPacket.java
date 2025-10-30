package commongameserver.network.response;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ SERVER -> CLIENT (người vừa bấm Chơi Lại).
 * Thông báo trạng thái chờ đối thủ phản hồi.
 */
public class PlayAgainStatusPacket extends Packet implements Serializable {

    private static final long serialVersionUID = 213L; // ID mới
    private final String message; // Ví dụ: "Đang chờ đối thủ đồng ý..."

    public PlayAgainStatusPacket(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public PacketType getType(){
        return PacketType.PLAY_AGAIN_STATUS;
    }
}
