package commongameserver.network.request;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ CLIENT -> SERVER
 * khi người dùng yêu cầu tạo tài khoản mới.
 */
public class RegisterRequest extends Packet implements Serializable {

    private static final long serialVersionUID = 107L; // ID mới
    private final String username;
    private final String password;

    /**
     * Constructor
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     */
    public RegisterRequest(String username, String password) {
        // Giả định Packet.java có constructor nhận PacketType
        this.username = username;
        this.password = password;
    }

    @Override
    public PacketType getType(){
        return PacketType.REGISTER_REQUEST;
    }
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}