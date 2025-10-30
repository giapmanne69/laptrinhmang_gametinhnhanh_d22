package commongameserver.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Lớp này đại diện cho thông tin cốt lõi của một người chơi.
 * Nó được dùng để gửi thông tin người chơi giữa Server và Client,
 * ví dụ như trong danh sách online, bảng xếp hạng, hoặc khi đăng nhập.
 *
 * Implement Serializable để có thể gửi qua Netty.
 * Sử dụng String cho ID.
 */
public class User implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 3L; // Một serialVersionUID duy nhất

    private String id;
    private String username;
    private String password; // Chỉ dùng cho việc login/register. Không nên gửi đi.

    private int totalScore; // Tổng điểm tích lũy
    private int totalWins;  // Tổng số trận thắng

    /**
     * Constructor rỗng (no-arg constructor)
     * Cần thiết cho việc deserialization.
     */
    public User() {
    }

    /**
     * Constructor cho việc tạo user mới (ví dụ: đăng ký)
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.totalScore = 0;
        this.totalWins = 0;
    }

    /**
     * Constructor đầy đủ (ví dụ: khi tải từ CSDL để gửi cho client)
     */
    public User(String id, String username, int totalScore, int totalWins) {
        this.id = id;
        this.username = username;
        this.totalScore = totalScore;
        this.totalWins = totalWins;
        // Mật khẩu KHÔNG BAO GIỜ được gửi đi sau khi đã đăng nhập
        this.password = null;
    }

    // --- Getters và Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Lấy mật khẩu (chủ yếu dùng khi Client gửi đăng nhập/đăng ký)
     * @return Mật khẩu
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    // --- Phương thức tiện ích ---

    /**
     * Xóa mật khẩu khỏi đối tượng
     * (Dùng trên Server trước khi gửi đối tượng User cho Client khác)
     */
    public void clearPassword() {
        this.password = null;
    }

    // --- Phương thức toString() để dễ dàng Gỡ lỗi (Debug) ---

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", totalScore=" + totalScore +
                ", totalWins=" + totalWins +
                '}';
    }

    // --- equals() và hashCode() ---
    // So sánh dựa trên ID vì nó là duy nhất.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        // Giả định ID là duy nhất và không null khi đã được gán
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
