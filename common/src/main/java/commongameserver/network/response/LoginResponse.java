package commongameserver.network.response;

import commongameserver.model.User;
import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ SERVER -> CLIENT
 * để phản hồi yêu cầu đăng nhập (LoginRequest).
 *
 * Chứa thông báo về việc đăng nhập thành công hay thất bại,
 * và thông tin User nếu thành công.
 */
public class LoginResponse extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 209L; // ID cho LoginResponse

    private boolean success;
    private String message;

    /**
     * Thông tin người dùng, sẽ là null nếu đăng nhập thất bại.
     */
    private User user;

    /**
     * Constructor rỗng
     * Cần thiết cho việc deserialization.
     */
    public LoginResponse() {
    }

    /**
     * Constructor cho trường hợp đăng nhập thành công
     * @param success Trạng thái (luôn là true)
     * @param message Thông báo (ví dụ: "Đăng nhập thành công")
     * @param user Đối tượng User của người vừa đăng nhập
     */
    public LoginResponse(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    /**
     * Constructor cho trường hợp đăng nhập thất bại
     * @param success Trạng thái (luôn là false)
     * @param message Lý do thất bại (ví dụ: "Sai mật khẩu")
     */
    public LoginResponse(boolean success, String message) {
        this(success, message, null);
    }

    // --- Getters và Setters ---

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là LOGIN_RESPONSE_PACKET
     */
    @Override
    public PacketType getType() {
        // Giả định PacketType.java đã có LOGIN_RESPONSE_PACKET
        return PacketType.LOGIN_RESPONSE;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", user=" + (user != null ? user.getUsername() : "null") +
                '}';
    }
}
