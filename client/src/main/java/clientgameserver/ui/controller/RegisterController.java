package clientgameserver.ui.controller;

import clientgameserver.service.ClientNetworkService;
import clientgameserver.ui.UIManager;
import commongameserver.network.request.RegisterRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller cho màn hình register.fxml
 */
public class RegisterController {

    // --- Singleton Pattern ---
    private static RegisterController instance;

    public RegisterController() {
        instance = this;
    }

    public static RegisterController getInstance() {
        return instance;
    }

    // --- FXML Bindings ---
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    /**
     * Được gọi khi người dùng bấm nút "Đăng Ký".
     */
    @FXML
    void handleRegisterButtonAction(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Tên và mật khẩu không được để trống.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Mật khẩu xác nhận không khớp.");
            return;
        }

        // Gửi yêu cầu đăng ký
        errorLabel.setVisible(false);
        RegisterRequest request = new RegisterRequest(username, password);
        ClientNetworkService.getInstance().sendPacket(request);
    }

    /**
     * Được gọi khi người dùng bấm nút "Quay Lại".
     */
    @FXML
    void handleBackButtonAction(ActionEvent event) {
        // Quay về màn hình Login
        UIManager.getInstance().showLoginScreen();
    }

    /**
     * Được gọi bởi ClientHandler (từ luồng Netty)
     * nếu đăng ký thất bại.
     */
    public void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        });
    }

    /**
     * Được gọi bởi ClientHandler (từ luồng Netty)
     * nếu đăng ký thành công.
     */
    public void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Đăng ký thành công");
            alert.setHeaderText(message);
            alert.setContentText("Bạn có thể đăng nhập ngay bây giờ.");
            alert.showAndWait();
            
            // Quay về màn hình Login
            UIManager.getInstance().showLoginScreen();
        });
    }
}