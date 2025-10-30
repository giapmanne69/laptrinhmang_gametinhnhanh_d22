package clientgameserver.ui.controller;

import clientgameserver.service.ClientNetworkService;
import clientgameserver.ui.UIManager;
import commongameserver.network.request.LoginRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert; // Thêm Alert
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage; // Thêm Stage để đóng cửa sổ

/**
 * Controller cho màn hình login.fxml
 * (Đã cập nhật: Thêm logic đếm lỗi đăng nhập)
 */
public class LoginController {

    // --- Singleton Pattern ---
    private static LoginController instance;

    public LoginController() {
        instance = this;
    }

    public static LoginController getInstance() {
        return instance;
    }

    // --- FXML Bindings ---
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    // --- Trạng thái nội bộ (Logic Đếm Lỗi) ---
    private int loginAttempts = 0;
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    /**
     * Được gọi khi người dùng bấm nút "Đăng Nhập".
     */
    @FXML
    void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Tên và mật khẩu không được để trống.");
            // Không tăng bộ đếm lỗi nếu chưa nhập gì
            return;
        }

        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        LoginRequest request = new LoginRequest(username, password);
        ClientNetworkService.getInstance().sendPacket(request);
    }

    /**
     * Được gọi khi người dùng bấm "Đăng ký ngay".
     */
    @FXML
    void handleGoToRegisterAction(ActionEvent event) {
        // Reset bộ đếm khi chuyển màn hình
        resetLoginAttempts();
        UIManager.getInstance().showRegisterScreen();
    }

    /**
     * Được gọi bởi ClientHandler (từ luồng Netty)
     * nếu đăng nhập thất bại.
     * (ĐÃ CẬP NHẬT LOGIC ĐẾM LỖI)
     */
    public void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            loginButton.setDisable(false); // Cho phép thử lại

            // Tăng bộ đếm lỗi
            loginAttempts++;
            System.out.println("LoginController: Đăng nhập thất bại lần " + loginAttempts);

            // Kiểm tra nếu vượt quá giới hạn
            if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                System.out.println("LoginController: Vượt quá số lần đăng nhập. Đóng ứng dụng.");
                showFinalErrorAndClose("Bạn đã đăng nhập sai quá " + MAX_LOGIN_ATTEMPTS + " lần. Ứng dụng sẽ đóng.");
            }
        });
    }

    /**
     * (HÀM MỚI) Hiển thị lỗi cuối cùng và đóng cửa sổ.
     */
    private void showFinalErrorAndClose(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi Đăng Nhập");
        alert.setHeaderText(message);
        alert.setContentText("Vui lòng thử lại sau.");
        
        // Lấy Stage (cửa sổ) hiện tại và đóng nó
        Stage stage = (Stage) loginButton.getScene().getWindow();
        alert.showAndWait(); // Hiển thị lỗi, chờ người dùng bấm OK
        stage.close();       // Đóng cửa sổ chính
    }

    /**
     * (HÀM MỚI) Reset bộ đếm lỗi.
     * Được gọi khi chuyển màn hình hoặc đăng nhập thành công.
     */
    private void resetLoginAttempts() {
        loginAttempts = 0;
        System.out.println("LoginController: Reset bộ đếm lỗi đăng nhập.");
    }

    /**
     * Được gọi bởi UIManager khi màn hình Login được hiển thị lại.
     * (CẬP NHẬT: Gọi resetLoginAttempts)
     */
    public void reset() {
        Platform.runLater(() -> {
            usernameField.clear();
            passwordField.clear();
            errorLabel.setVisible(false);
            loginButton.setDisable(false);
            // Reset bộ đếm lỗi khi quay lại màn hình Login
            resetLoginAttempts();
        });
    }
}

