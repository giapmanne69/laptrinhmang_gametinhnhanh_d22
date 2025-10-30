package clientgameserver.ui.controller;

import clientgameserver.service.ClientNetworkService;
import clientgameserver.ui.UIManager;
import commongameserver.model.User;
import commongameserver.network.request.RequestLeaderboardRequest;
import commongameserver.network.response.LeaderboardPacket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 * Controller cho màn hình leaderboard.fxml
 */
public class LeaderboardController {

    // --- Singleton Pattern ---
    private static LeaderboardController instance;

    public LeaderboardController() {
        instance = this;
    }

    public static LeaderboardController getInstance() {
        return instance;
    }

    // --- FXML Bindings ---
    @FXML
    private ListView<User> leaderboardListView;

    // Dữ liệu cho ListView
    private ObservableList<User> leaderboardList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Thiết lập cách ListView hiển thị User
        leaderboardListView.setItems(leaderboardList);
        leaderboardListView.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    // Định dạng hiển thị: Tên - Điểm: X - Thắng: Y
                    setText(String.format("%s - Điểm: %d - Thắng: %d",
                            user.getUsername(),
                            user.getTotalScore(),
                            user.getTotalWins()
                    ));
                }
            }
        });

        // Tự động gửi yêu cầu lấy BXH khi màn hình được mở
        handleRefreshButtonAction(null);
    }

    /**
     * Được gọi khi người dùng bấm nút "Làm mới".
     */
    @FXML
    void handleRefreshButtonAction(ActionEvent event) {
        // Xóa danh sách cũ
        leaderboardList.clear();
        // Gửi yêu cầu
        ClientNetworkService.getInstance().sendPacket(new RequestLeaderboardRequest());
    }

    /**
     * Được gọi khi người dùng bấm nút "Quay Lại".
     */
    @FXML
    void handleBackButtonAction(ActionEvent event) {
        // Đóng cửa sổ Bảng xếp hạng
        // (Giả sử đây là một cửa sổ Pop-up)
        // Hoặc quay về sảnh (nếu bạn có User)
        
        // Cách 1: Đóng cửa sổ
        Stage stage = (Stage) leaderboardListView.getScene().getWindow();
        stage.close();
        
        // Cách 2: Quay về sảnh (nếu bạn đã lưu currentUser)
        // UIManager.getInstance().showLobbyScreen(currentUser);
    }

    /**
     * Được gọi bởi ClientHandler khi nhận được LeaderboardPacket.
     */
    public void updateLeaderboard(LeaderboardPacket packet) {
        Platform.runLater(() -> {
            leaderboardList.clear();
            leaderboardList.addAll(packet.getLeaderboard());
        });
    }
}