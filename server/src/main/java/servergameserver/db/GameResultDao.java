package servergameserver.db;

import commongameserver.model.GameResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Lớp DAO (Data Access Object)
 * Chịu trách nhiệm xử lý các thao tác CSDL (CRUD)
 * cho đối tượng GameResult.
 */
public class GameResultDao {

    private static final GameResultDao instance = new GameResultDao();

    private GameResultDao() {
    }

    public static GameResultDao getInstance() {
        return instance;
    }

    /**
     * Lưu kết quả của một trận đấu vào cơ sở dữ liệu.
     *
     * @param result Đối tượng GameResult chứa thông tin trận đấu.
     * @return true nếu lưu thành công, false nếu thất bại.
     */
    public boolean saveGameResult(GameResult result) {
        String sql = "INSERT INTO game_db.game_results (game_id, player1_id, player2_id, " +
                "player1_username, player2_username, player1_score, player2_score, " +
                "winner_id, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Tự động tạo một ID duy nhất cho trận đấu nếu nó chưa có
        if (result.getGameId() == null || result.getGameId().isEmpty()) {
            result.setGameId(UUID.randomUUID().toString());
        }

        // Tự động đặt thời gian kết thúc nếu nó chưa có
        if (result.getTimestamp() == null) {
            result.setTimestamp(new java.util.Date());
        }

        // Sử dụng try-with-resources để đảm bảo Connection và PreparedStatement
        // được tự động đóng ngay cả khi có lỗi xảy ra.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Thiết lập các tham số cho câu lệnh SQL
            pstmt.setString(1, result.getGameId());
            pstmt.setString(2, result.getPlayer1Id());
            pstmt.setString(3, result.getPlayer2Id());
            pstmt.setString(4, result.getPlayer1Username());
            pstmt.setString(5, result.getPlayer2Username());
            pstmt.setInt(6, result.getPlayer1Score());
            pstmt.setInt(7, result.getPlayer2Score());
            pstmt.setString(8, result.getWinnerId()); // "DRAW" hoặc ID người thắng

            // Chuyển đổi java.util.Date sang java.sql.Timestamp
            pstmt.setTimestamp(9, new Timestamp(result.getTimestamp().getTime()));

            // Thực thi câu lệnh (trả về số hàng bị ảnh hưởng)
            int affectedRows = pstmt.executeUpdate();

            // Trả về true nếu ít nhất một hàng được chèn thành công
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu GameResult vào CSDL: " + e.getMessage());
            // Bạn có thể ghi log chi tiết hơn ở đây (e.printStackTrace())
            return false;
        }
    }

    /*
     * Bạn có thể thêm các phương thức khác ở đây, ví dụ:
     *
     * public List<GameResult> getHistoryForPlayer(String playerId) {
     * // Viết SQL để SELECT * FROM game_results
     * // WHERE player1_id = ? OR player2_id = ?
     * // ORDER BY timestamp DESC
     * }
     *
     * public List<GameResult> getRecentMatches(int limit) {
     * // Viết SQL để SELECT ... LIMIT ?
     * }
     */
}
