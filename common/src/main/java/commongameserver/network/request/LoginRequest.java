package commongameserver.network.request;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ CLIENT -> SERVER khi người chơi
 * yêu cầu đăng nhập vào hệ thống.
 */
public class LoginRequest extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 100L; // ID cho LoginRequest

    private String username;
    private String password;

    /**
     * Constructor rỗng
     */
    public LoginRequest() {
    }

    /**
     * Constructor
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // --- Getters và Setters ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là LOGIN_REQUEST
     */
    @Override
    public PacketType getType() {
        return PacketType.LOGIN_REQUEST;
    }

    @Override
    public String toString() {
        // Không bao giờ in mật khẩu ra log trong sản phẩm thực tế
        // Đây chỉ dùng cho mục đích gỡ lỗi (debug)
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
