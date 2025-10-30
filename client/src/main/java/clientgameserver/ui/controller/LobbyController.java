package clientgameserver.ui.controller;

import clientgameserver.service.ClientNetworkService;
import clientgameserver.ui.UIManager;
import commongameserver.model.OnlineUserInfo;
import commongameserver.model.PlayerStatus;
import commongameserver.model.User;
import commongameserver.network.request.ChallengeRequest;
import commongameserver.network.request.ChallengeResponse;
import commongameserver.network.request.LogoutRequest;
import commongameserver.network.request.RequestLeaderboardRequest;
import commongameserver.network.request.RequestOnlineListPacket;
import commongameserver.network.response.ChallengeInvitationPacket;
import commongameserver.network.response.OnlineListPacket;
import commongameserver.network.response.UserUpdatePacket; // THÊM IMPORT NÀY
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;

import java.util.Optional;

/**
 * Controller cho màn hình lobby.fxml
 * (Đã cập nhật để xử lý đồng bộ điểm người dùng sau khi chơi game)
 */
public class LobbyController {

    // --- Singleton Pattern ---
    private static LobbyController instance;

    public LobbyController() {
        instance = this;
    }

    public static LobbyController getInstance() {
        return instance;
    }

    // --- FXML Bindings ---
    @FXML private Label welcomeLabel;
    @FXML private Label scoreLabel;
    @FXML private ListView<OnlineUserInfo> onlineUsersListView;
    @FXML private Button challengeButton;

    // --- State ---
    private User currentUser;
    private final ObservableList<OnlineUserInfo> onlineUsersList = FXCollections.observableArrayList();

    /**
     * (HÀM MỚI) Cập nhật nhãn hiển thị điểm và tên.
     * Được gọi bởi UIManager.setCurrentUser() sau khi nhận UserUpdatePacket.
     * Hoặc được gọi bởi initializeLobby.
     */
    public void updateUserDisplay(User updatedUser) {
        if (updatedUser == null) return;
        this.currentUser = updatedUser; // Cập nhật state nội bộ
        
        Platform.runLater(() -> {
            welcomeLabel.setText("Chào mừng, " + currentUser.getUsername() + "!");
            // CẬP NHẬT ĐIỂM SỐ VÀ TRẬN THẮNG MỚI
            scoreLabel.setText(String.format("Điểm: %d | Thắng: %d",
                    currentUser.getTotalScore(),
                    currentUser.getTotalWins()
            ));
        });
        // Yêu cầu làm mới danh sách online để người khác thấy điểm mới của mình 
        handleRefreshButtonAction(null);
    }

    /**
     * Được gọi tự động bởi FXML loader.
     */
    @FXML
    public void initialize() {
        onlineUsersListView.setItems(onlineUsersList);

        onlineUsersListView.setCellFactory(lv -> new ListCell<OnlineUserInfo>() {
            @Override
            protected void updateItem(OnlineUserInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getUser() == null) {
                    setText(null);
                    setTextFill(Color.BLACK);
                } else {
                    User user = item.getUser();
                    PlayerStatus status = item.getStatus();
                    setText(String.format("%s (Điểm: %d) - [%s]",
                            user.getUsername(), user.getTotalScore(),
                            status == PlayerStatus.BUSY ? "Đang Bận" : "Sẵn Sàng"));
                    setTextFill(status == PlayerStatus.BUSY ? Color.GRAY : Color.BLACK);
                }
            }
        });

        onlineUsersListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                // Kiểm tra currentUser trước khi truy cập getId()
                if (currentUser == null || newSelection == null ||
                    newSelection.getStatus() == PlayerStatus.BUSY ||
                    newSelection.getUser().getId().equals(currentUser.getId())) {
                    challengeButton.setDisable(true);
                } else {
                    challengeButton.setDisable(false);
                }
            }
        );
        challengeButton.setDisable(true);
    }

    /**
     * Được gọi bởi UIManager khi màn hình Sảnh được hiển thị.
     */
    public void initializeLobby(User user) {
        // Thay vì hiển thị trực tiếp ở đây, chúng ta gọi hàm updateUserDisplay
        updateUserDisplay(user); 
    }

    // --- FXML Handlers ---

    @FXML
    void handleChallengeButtonAction(ActionEvent event) {
        OnlineUserInfo selectedInfo = onlineUsersListView.getSelectionModel().getSelectedItem();
        if (selectedInfo == null || selectedInfo.getUser() == null || currentUser == null) {
             showError("Lỗi", "Bạn phải chọn một người chơi hợp lệ để thách đấu.");
            return;
        }
        User selectedUser = selectedInfo.getUser();
        if (selectedInfo.getStatus() == PlayerStatus.BUSY) {
            showError("Không thể thách đấu", "Người chơi " + selectedUser.getUsername() + " đang bận.");
            return;
        }
        if (selectedUser.getId().equals(currentUser.getId())) {
            showError("Không thể thách đấu", "Bạn không thể tự thách đấu chính mình.");
            return;
        }
        System.out.println("Lobby: Gửi lời mời tới " + selectedUser.getUsername());
        ClientNetworkService.getInstance().sendPacket(new ChallengeRequest(selectedUser.getId()));
    }

    /**
     * Được gọi khi bấm nút "Làm Mới" hoặc khi vào sảnh.
     */
    @FXML
    void handleRefreshButtonAction(ActionEvent event) {
        System.out.println("LobbyController: Gửi yêu cầu làm mới danh sách online...");
        ClientNetworkService.getInstance().sendPacket(new RequestOnlineListPacket());
    }

    @FXML
    void handleLeaderboardButtonAction(ActionEvent event) {
        // Yêu cầu BXH
        ClientNetworkService.getInstance().sendPacket(new RequestLeaderboardRequest());
        UIManager.getInstance().showLeaderboard();
    }

    @FXML
    void handleLogoutButtonAction(ActionEvent event) {
        System.out.println("LobbyController: Gửi yêu cầu Logout...");
        ClientNetworkService.getInstance().sendPacket(new LogoutRequest());
        UIManager.getInstance().showLoginScreen();
    }

    // --- Hàm được gọi bởi ClientHandler (Luồng Netty) ---

    public void updateOnlineList(OnlineListPacket packet) {
        Platform.runLater(() -> {
            OnlineUserInfo currentSelection = onlineUsersListView.getSelectionModel().getSelectedItem();
            onlineUsersList.clear();
            if (packet != null && packet.getOnlineUsers() != null) {
                onlineUsersList.addAll(packet.getOnlineUsers());
            }

            if (currentSelection != null) {
                for (OnlineUserInfo newUserInfo : onlineUsersList) {
                    if (newUserInfo.getUser().getId().equals(currentSelection.getUser().getId())) {
                        onlineUsersListView.getSelectionModel().select(newUserInfo);
                        break;
                    }
                }
            }
            // Logic cập nhật nút thách đấu
        });
    }

    public void showChallengePopup(ChallengeInvitationPacket packet) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Bạn có lời mời thách đấu!");
            alert.setHeaderText(String.format("%s (Điểm: %d | Thắng: %d) muốn thách đấu bạn!",
                    packet.getChallengerUsername(), packet.getChallengerTotalScore(), packet.getChallengerTotalWins()));
            alert.setContentText("Bạn có đồng ý không?");

            ButtonType okButton = new ButtonType("Đồng ý");
            ButtonType rejectButton = new ButtonType("Từ chối");
            alert.getButtonTypes().setAll(okButton, rejectButton);

            Optional<ButtonType> result = alert.showAndWait();
            boolean accepted = (result.isPresent() && result.get() == okButton);

            ChallengeResponse response = new ChallengeResponse(packet.getChallengerId(), accepted);
            ClientNetworkService.getInstance().sendPacket(response);
        });
    }

    // --- Tiện ích ---
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
