package commongameserver.network.request;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ CLIENT -> SERVER khi người chơi
 * nộp một đáp án (phép tính).
 */
public class SubmitAnswerRequest extends Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 105L; // ID cho SubmitAnswerRequest

    /**
     * Phép tính mà người chơi nhập, ví dụ: "1+2*3"
     */
    private String expression;

    /**
     * Constructor rỗng
     */
    public SubmitAnswerRequest() {
    }

    /**
     * Constructor
     * @param expression Phép tính (dưới dạng chuỗi)
     */
    public SubmitAnswerRequest(String expression) {
        this.expression = expression;
    }

    // --- Getters và Setters ---

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * Ghi đè phương thức getType() từ lớp Packet cơ sở.
     * @return Loại gói tin là SUBMIT_ANSWER_REQUEST
     */
    @Override
    public PacketType getType() {
        return PacketType.SUBMIT_ANSWER_REQUEST;
    }

    @Override
    public String toString() {
        return "SubmitAnswerRequest{" +
                "expression='" + expression + '\'' +
                '}';
    }
}
