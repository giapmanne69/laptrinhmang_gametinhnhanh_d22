package commongameserver.network.request;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ CLIENT -> SERVER
 * khi người dùng chủ động bấm nút "Đăng xuất" (hoặc "Thoát")
 * khỏi Sảnh chờ (Lobby).
 */
public class LogoutRequest extends Packet implements Serializable {

    private static final long serialVersionUID = 108L; // ID mới

    @Override
    public PacketType getType(){
        return PacketType.LOGOUT_REQUEST;
    }
}
