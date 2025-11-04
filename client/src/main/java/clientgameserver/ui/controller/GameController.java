package clientgameserver.ui.controller;

import clientgameserver.service.ClientNetworkService;
import clientgameserver.ui.UIManager;
import commongameserver.model.GameResult;
import commongameserver.model.User;
import commongameserver.network.request.ExitGameRequest;
import commongameserver.network.request.PlayAgainRequest;
import commongameserver.network.request.SubmitAnswerRequest;
import commongameserver.network.response.GameStateUpdatePacket;
import commongameserver.network.response.GameStartPacket;
import commongameserver.network.response.GameTimerPacket;
import commongameserver.network.response.GameOverPacket;
import commongameserver.network.response.ScoreUpdatePacket;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import java.util.Optional;

/**
 * Controller cho màn hình game.fxml
 * (Đã CẬP NHẬT để hiển thị Điều Kiện Game)
 */
public class GameController {

    // --- Singleton Pattern ---
    private static GameController instance;

    public GameController() {
        instance = this;
    }

    public static GameController getInstance() {
        return instance;
    }

    // --- FXML Bindings (Info) ---
    @FXML private Label timerLabel;
    @FXML private Label yourScoreLabel;
    @FXML private Label opponentNameLabel;
    @FXML private Label opponentScoreLabel;
    @FXML private Label targetNumberLabel;
    @FXML private Label expressionLabel;
    
    // --- FXML BINDING MỚI ---
    @FXML private Label constraintLabel; // Label để hiển thị điều kiện

    // --- FXML Bindings (Game Over) ---
    @FXML private AnchorPane gameOverPane;
    @FXML private Label winnerLabel;
    @FXML private Label finalScoreLabel;
    @FXML private Label playAgainStatusLabel;

    // --- Trạng thái nội bộ ---
    private StringBuilder currentExpression = new StringBuilder();
    private boolean waitingForPlayAgain = false; 

    /**
     * Được gọi bởi UIManager khi game bắt đầu.
     * (CẬP NHẬT: Hiển thị điều kiện đầu tiên)
     */
    public void initializeGame(GameStartPacket packet) {
        User opponent = packet.getOpponent();
        
        Platform.runLater(() -> {
            // Reset UI
            targetNumberLabel.setText(String.valueOf(packet.getFirstTargetNumber()));
            
            // --- CẬP NHẬT ĐIỀU KIỆN ---
            constraintLabel.setText(packet.getConstraintDescription()); // Hiển thị điều kiện
            
            opponentNameLabel.setText(opponent.getUsername());
            yourScoreLabel.setText("0"); 
            opponentScoreLabel.setText("0");
            timerLabel.setText("02:00");
            
            clearExpression();
            
            gameOverPane.setVisible(false);
            playAgainStatusLabel.setText(""); 
            waitingForPlayAgain = false; 
        });
    }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN FXML (onAction) ---
    // (Các hàm onAction giữ nguyên)

    @FXML
    void handleNumberClick(ActionEvent event) {
        if (waitingForPlayAgain) return; 
        String number = ((Button) event.getSource()).getText();
        currentExpression.append(number);
        updateExpressionLabel();
    }

    @FXML
    void handleOperatorClick(ActionEvent event) {
        if (waitingForPlayAgain) return;
        String operator = ((Button) event.getSource()).getText();
        currentExpression.append(" ").append(operator).append(" ");
        updateExpressionLabel();
    }

    @FXML
    void handleSubmitClick(ActionEvent event) {
        if (waitingForPlayAgain || currentExpression.length() == 0) return;
        
        String finalExpression = currentExpression.toString().trim();
        ClientNetworkService.getInstance().sendPacket(new SubmitAnswerRequest(finalExpression));
        
        clearExpression();
    }

    @FXML
    void handleClearClick(ActionEvent event) {
        if (waitingForPlayAgain) return;
        clearExpression();
    }

    @FXML
    void handleExitClick(ActionEvent event) {
        ClientNetworkService.getInstance().sendPacket(new ExitGameRequest());
        UIManager.getInstance().showLobbyScreen(UIManager.getInstance().getCurrentUser());
    }

    @FXML
    void handlePlayAgainClick(ActionEvent event) {
        updatePlayAgainStatus("Đã gửi yêu cầu. Đang chờ đối thủ...");
        waitingForPlayAgain = true; 
        ClientNetworkService.getInstance().sendPacket(new PlayAgainRequest());
    }

    @FXML
    void handleExitToLobbyClick(ActionEvent event) {
        // Gửi ExitGameRequest để Server dọn dẹp
        ClientNetworkService.getInstance().sendPacket(new ExitGameRequest());
        UIManager.getInstance().showLobbyScreen(UIManager.getInstance().getCurrentUser());
    }
    
    // --- CÁC HÀM ĐƯỢC GỌI BỞI CLIENTHANDLER (LUỒNG NETTY) ---

    /**
     * Cập nhật điểm số và số mục tiêu mới (Cho người vừa trả lời).
     * (CẬP NHẬT: Hiển thị điều kiện tiếp theo)
     */
    public void updateState(GameStateUpdatePacket packet) {
        Platform.runLater(() -> {
            yourScoreLabel.setText(String.valueOf(packet.getYourScore()));
            opponentScoreLabel.setText(String.valueOf(packet.getOpponentScore()));
            
            // Cập nhật số mục tiêu
            if (packet.getNextTargetNumber() == -1) {
                targetNumberLabel.setText("ĐÃ XONG!");
                constraintLabel.setText("Chúc mừng bạn đã hoàn thành!"); // Thông báo hoàn thành
            } else {
                targetNumberLabel.setText(String.valueOf(packet.getNextTargetNumber()));
                // --- CẬP NHẬT ĐIỀU KIỆN ---
                constraintLabel.setText(packet.getNextConstraintDescription()); // Hiển thị điều kiện MỚI
            }
        });
    }
    
    /**
     * Cập nhật điểm số khi đối thủ trả lời.
     */
    public void updateScoreOnly(ScoreUpdatePacket packet) {
        Platform.runLater(() -> {
            yourScoreLabel.setText(String.valueOf(packet.getMyScore()));
            opponentScoreLabel.setText(String.valueOf(packet.getOpponentScore()));
            // Không cập nhật điều kiện ở đây
        });
    }

    /**
     * Cập nhật đồng hồ.
     */
    public void updateTimer(GameTimerPacket packet) {
        Platform.runLater(() -> {
            int seconds = packet.getRemainingTimeInSeconds();
            timerLabel.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
        });
    }

    /**
     * Hiển thị màn hình Game Over.
     */
    public void showGameOver(GameOverPacket packet) {
        Platform.runLater(() -> {
            GameResult result = packet.getGameResult();
            String myId = UIManager.getInstance().getCurrentUser().getId();
            String winnerText;
            
            if (result.getWinnerId() == null) { winnerText = "HÒA!"; } 
            else if (result.getWinnerId().equals(myId)) { winnerText = "BẠN THẮNG!"; } 
            else { winnerText = "BẠN THUA!"; }

            winnerLabel.setText(winnerText);
            
            boolean iAmPlayer1 = result.getPlayer1Id().equals(myId);
            int myFinalScore = iAmPlayer1 ? result.getPlayer1Score() : result.getPlayer2Score();
            int opponentFinalScore = iAmPlayer1 ? result.getPlayer2Score() : result.getPlayer1Score();
            
            finalScoreLabel.setText(String.format("Tỉ số: %d - %d", myFinalScore, opponentFinalScore));
            
            gameOverPane.setVisible(true);
            waitingForPlayAgain = true; 
        });
    }

    /**
     * Hiển thị thông báo đối thủ thoát.
     */
    public void showOpponentLeft() {
        Platform.runLater(() -> {
            if (!gameOverPane.isVisible()) { 
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Đối thủ đã thoát");
                alert.setHeaderText("Đối thủ của bạn đã thoát trận.");
                alert.setContentText("Bạn được xử thắng. Trở về sảnh.");
                alert.showAndWait();
                
                UIManager.getInstance().showLobbyScreen(UIManager.getInstance().getCurrentUser());
            }
        });
    }
    
    // --- Các hàm xử lý Play Again ---

    public void updatePlayAgainStatus(String message) {
        Platform.runLater(() -> {
            if (gameOverPane.isVisible()) {
                playAgainStatusLabel.setText(message);
            }
        });
    }

    public void showPlayAgainInvitation(String requesterUsername) {
        Platform.runLater(() -> {
            if (gameOverPane.isVisible() && !waitingForPlayAgain) { 
                 Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                 alert.setTitle("Mời Chơi Lại");
                 alert.setHeaderText(requesterUsername + " muốn chơi lại với bạn.");
                 alert.setContentText("Bạn có đồng ý không?");
                 ButtonType okButton = new ButtonType("Đồng ý");
                 ButtonType noButton = new ButtonType("Từ chối");
                 alert.getButtonTypes().setAll(okButton, noButton);

                 Optional<ButtonType> result = alert.showAndWait();
                 
                 if (result.isPresent() && result.get() == okButton) {
                     updatePlayAgainStatus("Đã chấp nhận. Đang chờ đối thủ...");
                     ClientNetworkService.getInstance().sendPacket(new PlayAgainRequest());
                     waitingForPlayAgain = true; 
                 } 
            } else if (gameOverPane.isVisible() && waitingForPlayAgain) {
                 updatePlayAgainStatus("Đối phương cũng đồng ý. Đang chờ Server...");
            }
        });
    }
    
    // --- Tiện ích ---
    private void updateExpressionLabel() {
        if (currentExpression.length() == 0) {
            expressionLabel.setText("..."); 
        } else {
            expressionLabel.setText(currentExpression.toString());
        }
    }

    private void clearExpression() {
        currentExpression.setLength(0);
        updateExpressionLabel();
    }
}