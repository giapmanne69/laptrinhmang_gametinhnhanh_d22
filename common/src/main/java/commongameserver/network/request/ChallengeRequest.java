package commongameserver.network.request;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ CLIENT -> SERVER khi một người chơi
 * muốn thách đấu một người chơi khác.
 */
public class ChallengeRequest extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 101L;

    /**
     * ID của người chơi BỊ thách đấu.
     */
    private String challengedUserId;

    /**
     * Constructor rỗng
     */
    public ChallengeRequest() {
    }

    /**
     * Constructor
     * @param challengedUserId ID của người chơi bị thách đấu
     */
    public ChallengeRequest(String challengedUserId) {
        this.challengedUserId = challengedUserId;
    }

    /**
     * Lấy ID của người chơi bị thách đấu.
     * @return ID người bị thách đấu
     */
    public String getChallengedUserId() {
        return challengedUserId;
    }

    public void setChallengedUserId(String challengedUserId) {
        this.challengedUserId = challengedUserId;
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là CHALLENGE_REQUEST
     */
    @Override
    public PacketType getType() {
        return PacketType.CHALLENGE_REQUEST;
    }

    @Override
    public String toString() {
        return "ChallengeRequest{" +
                "challengedUserId='" + challengedUserId + '\'' +
                '}';
    }
}
