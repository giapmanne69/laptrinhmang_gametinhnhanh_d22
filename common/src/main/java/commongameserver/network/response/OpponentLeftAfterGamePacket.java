package commongameserver.network.response;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Goi tin duoc gui TU SERVER -> CLIENT (nguoi o lai)
 * khi doi thu thoat khoi man hinh Game Over (thay vi thoat giua tran).
 * (De thuc hien yeu cau: "Nguoi choi ... da thoat")
 */
public class OpponentLeftAfterGamePacket extends Packet implements Serializable {

    private static final long serialVersionUID = 215L; // ID moi

    private final String opponentUsername;

    /**
     * @param opponentUsername Ten cua nguoi choi da thoat
     */
    public OpponentLeftAfterGamePacket(String opponentUsername) {
        // Luu y: Ban can them OPPONENT_LEFT_AFTER_GAME vao PacketType.java
        this.opponentUsername = opponentUsername;
    }

    @Override
    public PacketType getType() {
        return PacketType.OPPONENT_LEFT_AFTER_GAME;
    }

    /**
     * Lay ten cua nguoi choi da thoat.
     */
    public String getOpponentUsername() {
        return opponentUsername;
    }
}