package commongameserver.network.request;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ CLIENT -> SERVER.
 * Đây là phản hồi của người chơi (B) sau khi nhận được
 * lời thách đấu (ChallengeInvitationPacket) từ người chơi (A) (thông qua Server).
 */
public class ChallengeResponse extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 102L;

    /**
     * ID của người chơi đã GỬI lời mời thách đấu (Người chơi A).
     * Server cần biết phản hồi này là dành cho ai.
     */
    private String challengerId;

    /**
     * Quyết định của người chơi (B):
     * - true: Chấp nhận thách đấu
     * - false: Từ chối thách đấu
     */
    private boolean accepted;

    /**
     * Constructor rỗng
     */
    public ChallengeResponse() {
    }

    /**
     * Constructor
     * @param challengerId ID của người đã gửi lời mời
     * @param accepted     Trạng thái đồng ý (true) hay từ chối (false)
     */
    public ChallengeResponse(String challengerId, boolean accepted) {
        this.challengerId = challengerId;
        this.accepted = accepted;
    }

    // --- Getters và Setters ---

    public String getChallengerId() {
        return challengerId;
    }

    public void setChallengerId(String challengerId) {
        this.challengerId = challengerId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là CHALLENGE_RESPONSE
     */
    @Override
    public PacketType getType() {
        return PacketType.CHALLENGE_RESPONSE;
    }

    @Override
    public String toString() {
        return "ChallengeResponse{" +
                "challengerId='" + challengerId + '\'' +
                ", accepted=" + accepted +
                '}';
    }
}
