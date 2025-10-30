package commongameserver.network.response;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ SERVER -> CLIENT (đối thủ).
 * Thông báo rằng người chơi vừa đấu cùng muốn chơi lại.
 */
public class PlayAgainInvitationPacket extends Packet implements Serializable {

    private static final long serialVersionUID = 214L; // ID mới
    private final String requesterUsername; // Tên người muốn chơi lại

    public PlayAgainInvitationPacket(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    @Override
    public PacketType getType(){
        return PacketType.PLAY_AGAIN_INVITATION;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }
}
