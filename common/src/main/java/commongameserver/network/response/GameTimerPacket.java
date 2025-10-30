package commongameserver.network.response;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ SERVER -> CLIENT (cả hai người chơi)
 * định kỳ (ví dụ: mỗi giây) để cập nhật
 * thời gian còn lại của ván đấu (tính bằng giây).
 */
public class GameTimerPacket extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 207L; // ID cho GameTimerPacket

    /**
     * Thời gian còn lại của ván đấu, tính bằng giây.
     */
    private int remainingTimeInSeconds;

    /**
     * Constructor rỗng
     * Cần thiết cho việc deserialization.
     */
    public GameTimerPacket() {
    }

    /**
     * Constructor
     * @param remainingTimeInSeconds Thời gian còn lại (giây)
     */
    public GameTimerPacket(int remainingTimeInSeconds) {
        this.remainingTimeInSeconds = remainingTimeInSeconds;
    }

    // --- Getter và Setter ---

    public int getRemainingTimeInSeconds() {
        return remainingTimeInSeconds;
    }

    public void setRemainingTimeInSeconds(int remainingTimeInSeconds) {
        this.remainingTimeInSeconds = remainingTimeInSeconds;
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là GAME_TIMER_PACKET
     */
    @Override
    public PacketType getType() {
        // Giả định PacketType.java đã có GAME_TIMER_PACKET
        return PacketType.GAME_TIMER_PACKET;
    }

    @Override
    public String toString() {
        return "GameTimerPacket{" +
                "remainingTimeInSeconds=" + remainingTimeInSeconds +
                '}';
    }
}
