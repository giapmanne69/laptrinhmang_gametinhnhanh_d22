package commongameserver.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Một lớp DTO (Data Transfer Object) đơn giản
 * dùng để kết hợp thông tin User (dài hạn)
 * với Trạng thái (hiện tại) của họ.
 *
 * Được dùng trong OnlineListPacket.
 */
public class OnlineUserInfo implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 4L; // ID cho OnlineUserInfo

    private User user;
    private PlayerStatus status;

    /**
     * Constructor rỗng
     * Cần thiết cho việc deserialization.
     */
    public OnlineUserInfo() {
    }

    /**
     * Constructor
     * @param user Thông tin người dùng
     * @param status Trạng thái (AVAILABLE or BUSY)
     */
    public OnlineUserInfo(User user, PlayerStatus status) {
        this.user = user;
        this.status = status;
    }

    // --- Getters và Setters ---

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OnlineUserInfo{" +
                "user=" + (user != null ? user.getUsername() : "null") +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnlineUserInfo that = (OnlineUserInfo) o;
        // Hai OnlineUserInfo bằng nhau nếu User bằng nhau
        return Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user);
    }
}
