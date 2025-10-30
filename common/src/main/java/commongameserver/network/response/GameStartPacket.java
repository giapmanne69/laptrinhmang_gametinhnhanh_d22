package commongameserver.network.response;

import commongameserver.model.User;
import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ SERVER -> CLIENT (cả hai người chơi)
 * khi trận đấu chính thức bắt đầu (sau khi cả hai đã đồng ý).
 *
 * Gói tin này chứa thông tin về đối thủ và
 * con số mục tiêu (target number) đầu tiên.
 */
public class GameStartPacket extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 204L; // ID cho GameStartPacket

    /**
     * Thông tin của đối thủ (để client hiển thị tên, điểm, v.v.)
     */
    private User opponent;

    /**
     * Con số mục tiêu đầu tiên (trong dãy 30 số) mà người chơi cần đạt được.
     */
    private int firstTargetNumber;

    /**
     * Constructor rỗng
     * Cần thiết cho việc deserialization.
     */
    public GameStartPacket() {
    }

    /**
     * Constructor
     * @param opponent Đối tượng User của đối thủ
     * @param firstTargetNumber Số mục tiêu đầu tiên
     */
    public GameStartPacket(User opponent, int firstTargetNumber) {
        this.opponent = opponent;
        this.firstTargetNumber = firstTargetNumber;
    }

    // --- Getters và Setters ---

    public User getOpponent() {
        return opponent;
    }

    public void setOpponent(User opponent) {
        this.opponent = opponent;
    }

    public int getFirstTargetNumber() {
        return firstTargetNumber;
    }

    public void setFirstTargetNumber(int firstTargetNumber) {
        this.firstTargetNumber = firstTargetNumber;
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là GAME_START_PACKET (theo yêu cầu của bạn)
     */
    @Override
    public PacketType getType() {
        return PacketType.GAME_START_PACKET;
    }

    @Override
    public String toString() {
        return "GameStartPacket{" +
                "opponent=" + (opponent != null ? opponent.getUsername() : "null") +
                ", firstTargetNumber=" + firstTargetNumber +
                '}';
    }
}
