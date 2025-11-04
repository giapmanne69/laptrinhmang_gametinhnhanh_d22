package commongameserver.network.request;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Goi tin duoc gui TU CLIENT -> SERVER
 * khi nguoi choi bam nut "Ve Sanh" (sau khi game da ket thuc).
 * Goi tin nay KHONG dang xuat nguoi choi, chi thong bao
 * cho Server don dep logic "Choi Lai".
 */
public class LeftGameOverScreenRequest extends Packet implements Serializable {

    private static final long serialVersionUID = 110L; // ID moi

    public LeftGameOverScreenRequest() {
    }

    @Override
    public PacketType getType() {
        return PacketType.LEFT_GAME_OVER_SCREEN_REQUEST;
    }
}