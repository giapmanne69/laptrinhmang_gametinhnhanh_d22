package commongameserver.network.response;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

/**
 * Gói tin được Server gửi đến Client (người vừa trả lời)
 * để cập nhật trạng thái ván đấu.
 * Phiên bản này hỗ trợ logic "chơi độc lập".
 */
public class GameStateUpdatePacket extends Packet {

    private final int yourScore;
    private final int opponentScore;
    private final int nextTargetNumber;

    /**
     * Khởi tạo gói tin cập nhật trạng thái.
     *
     * @param yourScore        Điểm số hiện tại của BẠN (người nhận gói tin).
     * @param opponentScore    Điểm số hiện tại của ĐỐI THỦ.
     * @param nextTargetNumber Số mục tiêu tiếp theo cho BẠN.
     * (Gửi -1 nếu bạn đã hoàn thành 30 số).
     */
    public GameStateUpdatePacket(int yourScore, int opponentScore, int nextTargetNumber) {
        this.yourScore = yourScore;
        this.opponentScore = opponentScore;
        this.nextTargetNumber = nextTargetNumber;
    }

    public int getYourScore() {
        return yourScore;
    }

    public int getOpponentScore() {
        return opponentScore;
    }

    public int getNextTargetNumber() {
        return nextTargetNumber;
    }

    @Override
    public PacketType getType() {
        return PacketType.GAME_STATE_UPDATE_PACKET;
    }

    @Override
    public String toString() {
        return "GameStateUpdatePacket{" +
                "yourScore=" + yourScore +
                ", opponentScore=" + opponentScore +
                ", nextTargetNumber=" + nextTargetNumber +
                '}';
    }
}

