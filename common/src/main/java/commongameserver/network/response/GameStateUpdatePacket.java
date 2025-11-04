package commongameserver.network.response;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Goi tin duoc Server gui den Client (nguoi vua tra loi)
 * de cap nhat trang thai van dau.
 * (Da cap nhat de bao gom Dieu Kien (Constraint) cho cau hoi TIEP THEO)
 */
public class GameStateUpdatePacket extends Packet implements Serializable {

    private static final long serialVersionUID = 206L; // ID cho GameStateUpdatePacket

    private final int yourScore;
    private final int opponentScore;
    private final int nextTargetNumber;
    private final String nextConstraintDescription; // <-- DIEU KIEN MOI DUOC THEM

    /**
     * Khoi tao goi tin cap nhat trang thai.
     *
     * @param yourScore Diem so hien tai cua BAN (nguoi nhan goi tin).
     * @param opponentScore Diem so hien tai cua DOI THU.
     * @param nextTargetNumber So muc tieu tiep theo cho BAN.
     * (Gui -1 neu ban da hoan thanh 30 so).
     * @param nextConstraintDescription Mo ta dieu kien cho cau hoi tiep theo.
     * (Gui null hoac "" neu da hoan thanh).
     */
    public GameStateUpdatePacket(int yourScore, int opponentScore, int nextTargetNumber, String nextConstraintDescription) {
        this.yourScore = yourScore;
        this.opponentScore = opponentScore;
        this.nextTargetNumber = nextTargetNumber;
        this.nextConstraintDescription = nextConstraintDescription;
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

    /**
     * Lay chuoi mo ta dieu kien cho cau hoi TIEP THEO.
     */
    public String getNextConstraintDescription() {
        return nextConstraintDescription;
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
                ", nextConstraintDescription='" + nextConstraintDescription + '\'' +
                '}';
    }
}