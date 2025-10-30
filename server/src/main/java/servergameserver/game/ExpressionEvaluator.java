package servergameserver.game;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import java.util.regex.Pattern;

/**
 * Lớp tiện ích để phân tích và tính toán giá trị
 * của một chuỗi biểu thức toán học.
 * SỬ DỤNG THƯ VIỆN EXP4J (thay thế ScriptEngine/Nashorn) để tương thích với JDK mới.
 * Bao gồm kiểm tra đầu vào bằng Regex để tăng cường an toàn.
 */
public class ExpressionEvaluator {

    // Biểu thức chính quy (Regex) để kiểm tra chuỗi đầu vào.
    // Chỉ cho phép số (0-9), toán tử (+, -, *, /), dấu cách (\s), ngoặc đơn (())
    private static final Pattern VALID_EXPRESSION_PATTERN = Pattern.compile(
            "^[0-9+\\-*/()\\s]+$"
    );

    /**
     * Constructor rỗng. Khởi tạo exp4j không cần engine tĩnh.
     */
    public ExpressionEvaluator() {
        System.out.println("ExpressionEvaluator: Su dung thu vien exp4j.");
    }

    /**
     * Tính toán giá trị của một biểu thức toán học.
     *
     * @param expression Chuỗi biểu thức, ví dụ: "5 * 2 + 1"
     * @return Một đối tượng Double chứa kết quả nếu biểu thức hợp lệ.
     * Trả về null nếu biểu thức không hợp lệ, lỗi cú pháp, hoặc rỗng/null.
     */
    public Double evaluate(String expression) {
        // Kiểm tra null hoặc rỗng
        if (expression == null || expression.trim().isEmpty()) {
            System.err.println("Danh gia that bai: Bieu thuc rong hoac null.");
            return null;
        }

        // 1. Kiểm tra an toàn bằng Regex (Ngăn chặn ký tự lạ)
        if (!VALID_EXPRESSION_PATTERN.matcher(expression).matches()) {
            System.err.println("Danh gia that bai: Bieu thuc chua ky tu khong hop le: '" + expression + "'");
            return null;
        }

        try {
            // 2. Xây dựng biểu thức bằng exp4j
            Expression expr = new ExpressionBuilder(expression)
                    .build();

            // 3. Thực thi và lấy kết quả
            double result = expr.evaluate();

            // 4. Kiểm tra vô hạn (Infinity) và NaN (Không phải số)
            if (Double.isInfinite(result) || Double.isNaN(result)) {
                System.err.println("Danh gia that bai: Phep tinh khong hop le (Infinity/NaN): '" + expression + "'");
                return null;
            }

            // Trả về giá trị double hợp lệ
            return result;

        } catch (ArithmeticException e) {
            // Lỗi toán học (ví dụ: chia cho 0)
            System.err.println("Danh gia that bai: Loi toan hoc (chia cho 0?): '" + expression + "' | Loi: " + e.getMessage());
            return null;
        } catch (Exception e) {
            // Lỗi cú pháp hoặc lỗi không mong muốn khác
            System.err.println("Danh gia that bai: Loi cu phap/Khong xac dinh khi tinh '" + expression + "' | Bieu thuc: " + expression + " | Loi: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}