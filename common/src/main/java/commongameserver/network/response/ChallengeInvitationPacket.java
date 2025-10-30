package commongameserver.network.response;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ SERVER -> CLIENT (người bị thách đấu).
 * Thông báo cho client này biết rằng có một người chơi khác
 * (challenger) muốn thách đấu họ.
 *
 * (Đã cập nhật để chứa thêm thông tin điểm)
 */
public class ChallengeInvitationPacket extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 203L; // ID cho ChallengeInvitationPacket

    private String challengerId;
    private String challengerUsername;
    private int challengerTotalScore;
    private int challengerTotalWins;

    /**
     * Constructor rỗng
     * Cần thiết cho một số cơ chế deserialization.
     */
    public ChallengeInvitationPacket() {
         // Cần gọi super() với một loại mặc định hoặc null
         // nhưng tốt nhất là đảm bảo Packet.java xử lý việc này

    }

    /**
     * Constructor đầy đủ
     * @param challengerId ID của người gửi lời mời
     * @param challengerUsername Tên của người gửi lời mời
     * @param challengerTotalScore Tổng điểm của người gửi lời mời
     * @param challengerTotalWins Tổng trận thắng của người gửi lời mời
     */
    public ChallengeInvitationPacket(String challengerId, String challengerUsername, int challengerTotalScore, int challengerTotalWins) {
        // Gọi constructor của lớp cha (Packet) để thiết lập loại gói tin
        this.challengerId = challengerId;
        this.challengerUsername = challengerUsername;
        this.challengerTotalScore = challengerTotalScore;
        this.challengerTotalWins = challengerTotalWins;
    }

    // --- Getters và Setters ---

    public String getChallengerId() {
        return challengerId;
    }

    public void setChallengerId(String challengerId) {
        this.challengerId = challengerId;
    }

    public String getChallengerUsername() {
        return challengerUsername;
    }

    public void setChallengerUsername(String challengerUsername) {
        this.challengerUsername = challengerUsername;
    }

    public int getChallengerTotalScore() {
        return challengerTotalScore;
    }

    public void setChallengerTotalScore(int challengerTotalScore) {
        this.challengerTotalScore = challengerTotalScore;
    }

    public int getChallengerTotalWins() {
        return challengerTotalWins;
    }

    public void setChallengerTotalWins(int challengerTotalWins) {
        this.challengerTotalWins = challengerTotalWins;
    }

     @Override
    public PacketType getType() {
        return PacketType.CHALLENGE_INVITATION_PACKET;
    }


    @Override
    public String toString() {
        return "ChallengeInvitationPacket{" +
                "challengerId='" + challengerId + '\'' +
                ", challengerUsername='" + challengerUsername + '\'' +
                ", challengerTotalScore=" + challengerTotalScore +
                ", challengerTotalWins=" + challengerTotalWins +
                '}';
    }
}

