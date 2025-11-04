package commongameserver.network.response;

import commongameserver.model.User;
import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Goi tin duoc gui TU SERVER -> CLIENT khi tran dau bat dau.
 * (Da cap nhat de bao gom Dieu Kien (Constraint) cho cau hoi dau tien)
 */
public class GameStartPacket extends Packet implements Serializable {

    private static final long serialVersionUID = 205L; // ID cho GameStartPacket

    private final User opponent;
    private final int firstTargetNumber;
    private final String constraintDescription; // <-- DIEU KIEN MOI DUOC THEM

    /**
     * Constructor
     * @param opponent Thong tin doi thu
     * @param firstTargetNumber So muc tieu dau tien
     * @param constraintDescription Mo ta dieu kien (vi du: "Phai dung 3 toan hang")
     */
    public GameStartPacket(User opponent, int firstTargetNumber, String constraintDescription) {
        this.opponent = opponent;
        this.firstTargetNumber = firstTargetNumber;
        this.constraintDescription = constraintDescription;
    }

    // --- Getters ---

    public User getOpponent() {
        return opponent;
    }

    public int getFirstTargetNumber() {
        return firstTargetNumber;
    }

    /**
     * Lay chuoi mo ta dieu kien cho cau hoi nay.
     */
    public String getConstraintDescription() {
        return constraintDescription;
    }

    @Override
    public PacketType getType() {
        return PacketType.GAME_START_PACKET;
    }

    @Override
    public String toString() {
        return "GameStartPacket{" +
                "opponent=" + opponent.getUsername() +
                ", firstTargetNumber=" + firstTargetNumber +
                ", constraintDescription='" + constraintDescription + '\'' +
                '}';
    }
}