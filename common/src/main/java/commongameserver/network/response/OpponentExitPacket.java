package commongameserver.network.response;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ SERVER -> CLIENT
 * để thông báo rằng đối thủ trong trận đấu hiện tại đã thoát
 * (do mất kết nối hoặc chủ động nhấn nút Thoát).
 */
public class OpponentExitPacket extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 211L; // ID cho OpponentExitPacket

    private String message;

    /**
     * Constructor rỗng
     * Cần thiết cho việc deserialization.
     */
    public OpponentExitPacket() {
    }

    /**
     * Constructor
     * @param message Thông báo (ví dụ: "Đối thủ đã thoát khỏi trận đấu.")
     */
    public OpponentExitPacket(String message) {
        this.message = message;
    }

    // --- Getter và Setter ---

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là OPPONENT_EXIT_PACKET
     */
    @Override
    public PacketType getType() {
        // Giả định PacketType.java đã có OPPONENT_EXIT_PACKET
        return PacketType.OPPONENT_EXIT_PACKET;
    }

    @Override
    public String toString() {
        return "OpponentExitPacket{" +
                "message='" + message + '\'' +
                '}';
    }
}
