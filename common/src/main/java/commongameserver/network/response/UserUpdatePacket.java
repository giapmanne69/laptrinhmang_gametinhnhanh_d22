package commongameserver.network.response;

import commongameserver.model.User;
import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ SERVER -> CLIENT (sau khi ket thuc game)
 * để cập nhật thông tin User (điểm số, số trận thắng)
 * trong UI Manager của Client.
 */
public class UserUpdatePacket extends Packet implements Serializable {

    private static final long serialVersionUID = 215L; // ID mới

    private final User updatedUser;

    /**
     * Constructor
     * @param updatedUser Đối tượng User đã được tải lại từ CSDL (chứa điểm mới nhất).
     */
    public UserUpdatePacket(User updatedUser) {
        this.updatedUser = updatedUser;
    }

    @Override
    public PacketType getType(){
        return PacketType.USER_UPDATE_PACKET;
    }

    public User getUpdatedUser() {
        return updatedUser;
    }
}
