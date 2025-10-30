package commongameserver.network.response;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ SERVER -> CLIENT
 * để phản hồi yêu cầu đăng ký (RegisterRequest).
 */
public class RegisterResponse extends Packet implements Serializable {

    private static final long serialVersionUID = 212L; // ID mới
    private final boolean success;
    private final String message;

    /**
     * Constructor
     * @param success Trạng thái (true/false)
     * @param message Thông báo (ví dụ: "Tên đăng nhập đã tồn tại")
     */
    public RegisterResponse(boolean success, String message) {

        this.success = success;
        this.message = message;
    }

    @Override
    public PacketType getType(){
        return PacketType.REGISTER_RESPONSE;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}