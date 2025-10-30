package servergameserver.db;

import commongameserver.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lớp Singleton (DAO - Data Access Object)
 * Chịu trách nhiệm cho mọi thao tác CSDL liên quan đến User.
 * (Đã cập nhật để bao gồm getUserById cho GameSession)
 */
public class UserDao {

    // --- Singleton Pattern ---
    private static final UserDao instance = new UserDao();

    private UserDao() {
    }

    public static UserDao getInstance() {
        return instance;
    }

    /**
     * Tải lại thông tin User dựa trên ID.
     * Cần thiết để lấy thông tin điểm số/thắng lợi mới nhất sau khi update.
     *
     * @param userId ID của người dùng
     * @return Đối tượng User nếu tìm thấy, null nếu không
     */
    public User getUserById(String userId) {
        String sql = "SELECT id, username, total_score, total_wins FROM game_db.users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setUsername(rs.getString("username"));
                    user.setTotalScore(rs.getInt("total_score"));
                    user.setTotalWins(rs.getInt("total_wins"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tải User theo ID: " + userId + " | Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Xác thực người dùng (cho việc Đăng nhập).
     */
    public User authenticateUser(String username, String password) {
        // TODO: Mật khẩu PHẢI được hash. Đây chỉ là code demo.
        String sql = "SELECT id, username, total_score, total_wins FROM game_db.users " +
                     "WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setUsername(rs.getString("username"));
                    user.setTotalScore(rs.getInt("total_score"));
                    user.setTotalWins(rs.getInt("total_wins"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xác thực người dùng " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Kiểm tra xem tên đăng nhập đã tồn tại chưa (cho việc Đăng ký).
     */
    public boolean findUserByUsername(String username) {
        String sql = "SELECT 1 FROM game_db.users WHERE username = ? LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm người dùng " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Tạo một người dùng mới trong CSDL (cho việc Đăng ký).
     */
    public boolean createUser(String username, String password) {
        String sql = "INSERT INTO game_db.users (id, username, password, total_score, total_wins) " +
                     "VALUES (?, ?, ?, 0, 0)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String newUserId = UUID.randomUUID().toString();

            pstmt.setString(1, newUserId);
            pstmt.setString(2, username);
            pstmt.setString(3, password);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo người dùng " + username + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy TẤT CẢ người dùng (cho Bảng xếp hạng).
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, total_score, total_wins FROM game_db.users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setTotalScore(rs.getInt("total_score"));
                user.setTotalWins(rs.getInt("total_wins"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả người dùng: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return users;
    }

    /**
     * Cập nhật chỉ số (điểm, thắng) sau trận đấu.
     */
    public void updateUserStats(String userId, int scoreGained, boolean didWin) {
        if (scoreGained < 0) scoreGained = 0;

        String sql;
        if (didWin) {
            sql = "UPDATE game_db.users SET total_score = total_score + ?, total_wins = total_wins + 1 WHERE id = ?";
        } else {
            sql = "UPDATE game_db.users SET total_score = total_score + ? WHERE id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, scoreGained);
            pstmt.setString(2, userId);

            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật chỉ số cho " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
