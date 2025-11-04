package servergameserver.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Lớp tiện ích để quản lý kết nối đến cơ sở dữ liệu.
 * Sử dụng JDBC để kết nối.
 *
 * Lưu ý: Đây là một ví dụ đơn giản. Trong một ứng dụng
 * sản phẩm thực tế, bạn nên sử dụng một Connection Pool
 * (như HikariCP) để quản lý kết nối hiệu quả hơn.
 */
public class DatabaseConnection {

    // (ĐÃ THÊM) Đổi "localhost" thành IP của máy chủ CSDL nếu chạy riêng
    private static final String DB_HOST = "localhost"; 
    
    // Thay thế "iot_database" bằng tên CSDL game của bạn (ví dụ: "game_db")
    private static final String DB_NAME = "iot_database"; 
    
    // (ĐÃ SỬA) Tạo URL động bằng cách sử dụng DB_HOST và DB_NAME
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":3306/" + DB_NAME;
    
    // Thay thế bằng tên người dùng CSDL của bạn
    private static final String DB_USER = "root";
    // Thay thế bằng mật khẩu CSDL của bạn
    private static final String DB_PASSWORD = "Giap200469@@@";

    /**
     * Tải trình điều khiển JDBC (Driver) chỉ một lần khi lớp được nạp.
     */
    static {
        try {
            // Tải trình điều khiển MySQL JDBC
            // Đảm bảo bạn đã thêm 'mysql-connector-java' vào build.gradle của server
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy trình điều khiển MySQL JDBC. Hãy thêm thư viện vào classpath.");
            // Ném lỗi runtime để ứng dụng dừng lại nếu không thể tải driver
            throw new RuntimeException("Lỗi tải trình điều khiển CSDL", e);
        }
    }

    /**
     * Lấy một kết nối (Connection) mới đến cơ sở dữ liệu.
     *
     * @return một đối tượng Connection đã được thiết lập
     * @throws SQLException nếu có lỗi xảy ra khi kết nối (sai URL, user, pass...)
     */
    public static Connection getConnection() throws SQLException {
        // DriverManager sẽ cung cấp một kết nối dựa trên thông tin đã đăng ký
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Phương thức tiện ích để đóng kết nối CSDL một cách an toàn.
     * @param conn Connection cần đóng
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối CSDL: " + e.getMessage());
                // Không ném lại lỗi, chỉ ghi log
            }
        }
    }

    /**
     * Phương thức tiện ích để đóng Statement một cách an toàn.
     * @param stmt Statement cần đóng
     */
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng Statement: " + e.getMessage());
            }
        }
    }

    /**
     * Phương thức tiện ích để đóng ResultSet một cách an toàn.
     * @param rs ResultSet cần đóng
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng ResultSet: " + e.getMessage());
            }
        }
    }

    /**
     * Phương thức tiện ích để đóng an toàn cả ba tài nguyên.
     * @param conn Connection
     * @param stmt Statement (hoặc PreparedStatement)
     * @param rs ResultSet
     */
    public static void closeAll(Connection conn, Statement stmt, ResultSet rs) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }
}
