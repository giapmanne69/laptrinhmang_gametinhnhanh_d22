package clientgameserver.ui;

import clientgameserver.ui.controller.GameController;
import clientgameserver.ui.controller.LobbyController;
import clientgameserver.ui.controller.LoginController;
import commongameserver.model.User;
import commongameserver.network.response.GameStartPacket;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Lớp Singleton, quản lý việc tải FXML và chuyển đổi Scene.
 * (Đã cập nhật để cho phép cập nhật User mới sau khi chơi game)
 */
public class UIManager {

    // --- Singleton Pattern ---
    private static final UIManager instance = new UIManager();

    private UIManager() {
    }

    public static UIManager getInstance() {
        return instance;
    }

    // --- State ---
    private Stage primaryStage;
    private User currentUser; // Lưu lại User khi đăng nhập thành công

    /**
     * Khởi tạo UIManager với Stage chính của ứng dụng.
     */
    public void initialize(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * (HÀM MỚI) Cập nhật đối tượng User hiện tại và thông báo cho LobbyController.
     * @param user Đối tượng User mới (có điểm, thắng được cập nhật)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Nếu đang ở Lobby, gọi LobbyController để cập nhật UI ngay lập tức
        if (LobbyController.getInstance() != null) {
            LobbyController.getInstance().updateUserDisplay(user);
        }
    }

    /**
     * Tải một tệp FXML và hiển thị nó trên Stage chính.
     */
    private Parent loadAndSwitchScene(String fxmlFile) {
        try {
            String fxmlPath = "/clientgameserver/ui/view/" + fxmlFile;
            // 1. Lấy URL trước (Sửa lỗi getResource)
            java.net.URL fxmlUrl = getClass().getResource(fxmlPath);

            // 2. Kiểm tra URL
            if (fxmlUrl == null) {
                throw new IOException("Không tìm thấy tệp FXML: " + fxmlPath);
            }

            // 3. Khởi tạo FXMLLoader với URL
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            Platform.runLater(() -> {
                primaryStage.setScene(new Scene(root));
                primaryStage.centerOnScreen();
            });
            
            return root; 
        } catch (IOException e) {
            System.err.println("Không thể tải FXML: " + fxmlFile);
            e.printStackTrace();
            return null;
        }
    }

    // --- Các hàm chuyển màn hình ---

    public void showLoginScreen() {
        loadAndSwitchScene("login.fxml");
        if (LoginController.getInstance() != null) {
            LoginController.getInstance().reset();
        }
    }

    public void showRegisterScreen() {
        loadAndSwitchScene("register.fxml");
    }

    /**
     * Hiển thị sảnh chờ sau khi đăng nhập.
     */
    public void showLobbyScreen(User user) {
        if (user != null) {
            this.currentUser = user; // LƯU USER MỚI (Từ LoginResponse)
        }
        
        if (this.currentUser == null) {
            showLoginScreen(); // Lỗi: Quay về Login
            return;
        }

        loadAndSwitchScene("lobby.fxml");
        
        Platform.runLater(() -> {
            if (LobbyController.getInstance() != null) {
                // Khởi tạo Lobby với User MỚI NHẤT
                LobbyController.getInstance().initializeLobby(this.currentUser);
            }
        });
    }

    public void showGameScreen(GameStartPacket packet) {
        loadAndSwitchScene("game.fxml");

        Platform.runLater(() -> {
            if (GameController.getInstance() != null) {
                GameController.getInstance().initializeGame(packet);
            }
        });
    }

    /**
     * Hiển thị Bảng xếp hạng (dạng Pop-up).
     */
    public void showLeaderboard() {
        try {
            String fxmlPath = "/clientgameserver/ui/view/leaderboard.fxml";
            java.net.URL fxmlUrl = getClass().getResource(fxmlPath);

            if (fxmlUrl == null) {
                throw new IOException("Không tìm thấy tệp FXML: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage leaderboardStage = new Stage();
            leaderboardStage.setTitle("Bảng Xếp Hạng");
            leaderboardStage.initModality(Modality.APPLICATION_MODAL); 
            leaderboardStage.initOwner(primaryStage);
            leaderboardStage.setScene(new Scene(root));
            leaderboardStage.showAndWait(); 
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
