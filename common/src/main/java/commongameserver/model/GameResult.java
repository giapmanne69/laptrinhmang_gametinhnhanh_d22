package commongameserver.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Lớp này đại diện cho kết quả của MỘT trận đấu đã hoàn thành.
 * Nó được dùng để lưu trữ vào cơ sở dữ liệu và có thể được gửi
 * đến client nếu có chức năng xem lịch sử đấu.
 *
 * Implement Serializable để có thể gửi qua Netty.
 *
 * Cập nhật: Sử dụng String cho tất cả các ID.
 */
public class GameResult implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 2L; // Thay đổi version ID vì cấu trúc thay đổi

    private String gameId;
    private String player1Id;
    private String player2Id;
    private String player1Username;
    private String player2Username;
    private int player1Score;
    private int player2Score;

    /**
     * ID của người chiến thắng.
     * Có thể là player1Id, player2Id, hoặc "0" (hoặc null) nếu hòa.
     */
    private String winnerId;
    private Date timestamp; // Thời điểm trận đấu kết thúc

    // Constructor rỗng (no-arg constructor)
    public GameResult() {
    }

    // Constructor đầy đủ để dễ dàng tạo đối tượng
    public GameResult(String gameId, String player1Id, String player2Id,
                      String player1Username, String player2Username,
                      int player1Score, int player2Score,
                      String winnerId, Date timestamp) {
        this.gameId = gameId;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.player1Username = player1Username;
        this.player2Username = player2Username;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.winnerId = winnerId;
        this.timestamp = timestamp;
    }

    // --- Getters và Setters ---

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(String player1Id) {
        this.player1Id = player1Id;
    }

    public String getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(String player2Id) {
        this.player2Id = player2Id;
    }

    public String getPlayer1Username() {
        return player1Username;
    }

    public void setPlayer1Username(String player1Username) {
        this.player1Username = player1Username;
    }

    public String getPlayer2Username() {
        return player2Username;
    }

    public void setPlayer2Username(String player2Username) {
        this.player2Username = player2Username;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // --- Phương thức toString() để dễ dàng Gỡ lỗi (Debug) ---

    @Override
    public String toString() {
        return "GameResult{" +
                "gameId='" + gameId + '\'' +
                ", player1Id='" + player1Id + '\'' + " (Score: " + player1Score + ")" +
                ", player2Id='" + player2Id + '\'' + " (Score: " + player2Score + ")" +
                ", winnerId='" + winnerId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    // --- equals() và hashCode() (dựa trên gameId) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameResult that = (GameResult) o;
        return Objects.equals(gameId, that.gameId); // Dùng Objects.equals cho String
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId);
    }
}

