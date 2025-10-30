package commongameserver.network.response;

// Giả định GameResult nằm trong package này dựa trên file bạn cung cấp
import commongameserver.model.GameResult;
import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ SERVER -> CLIENT (cả hai người chơi)
 * khi ván đấu kết thúc.
 * Gói tin này chứa kết quả cuối cùng của trận đấu.
 */
public class GameOverPacket extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 205L; // ID cho GameOverPacket

    private GameResult gameResult;

    /**
     * Constructor rỗng
     * Cần thiết cho việc deserialization.
     */
    public GameOverPacket() {
    }

    /**
     * Constructor
     * @param gameResult Đối tượng chứa toàn bộ kết quả trận đấu
     */
    public GameOverPacket(GameResult gameResult) {
        this.gameResult = gameResult;
    }

    // --- Getter và Setter ---

    public GameResult getGameResult() {
        return gameResult;
    }

    public void setGameResult(GameResult gameResult) {
        this.gameResult = gameResult;
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là GAME_OVER
     */
    @Override
    public PacketType getType() {
        return PacketType.GAME_OVER_PACKET;
    }

    @Override
    public String toString() {
        return "GameOverPacket{" +
                "gameResult=" + (gameResult != null ? gameResult.toString() : "null") +
                '}';
    }
}
