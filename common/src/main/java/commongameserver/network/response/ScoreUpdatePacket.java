package commongameserver.network.response;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ SERVER -> CLIENT (đối thủ) để cập nhật điểm.
 * KHÔNG chứa nextTargetNumber.
 */
public class ScoreUpdatePacket extends Packet implements Serializable {

    private static final long serialVersionUID = 215L; 
    private final int myScore;
    private final int opponentScore;

    public ScoreUpdatePacket(int myScore, int opponentScore) {
        this.myScore = myScore;
        this.opponentScore = opponentScore;
    }

    @Override
    public PacketType getType(){
        return PacketType.SCORE_UPDATE_PACKET;
    }

    public int getMyScore() { return myScore; }
    public int getOpponentScore() { return opponentScore; }
}
