package clientgameserver.ui.controller;

import commongameserver.network.response.ScoreUpdatePacket; // CẦN THÊM IMPORT NÀY
import java.util.Optional;

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
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

/**
 * Controller cho màn hình game.fxml
 * (Đã cập nhật: Thêm hàm updateScoreOnly)
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

    // --- FXML Bindings (Giữ nguyên) ---
    @FXML private Label timerLabel;
    @FXML private Label yourScoreLabel;
    @FXML private Label opponentNameLabel;
    @FXML private Label opponentScoreLabel;
    @FXML private Label targetNumberLabel;
    @FXML private Label expressionLabel;
    @FXML private AnchorPane gameOverPane;
    @FXML private Label winnerLabel;
    @FXML private Label finalScoreLabel;
    @FXML private Label playAgainStatusLabel;

    // --- Trạng thái nội bộ ---
    private StringBuilder currentExpression = new StringBuilder();
    private boolean waitingForPlayAgain = false;

    // --- CÁC HÀM XỬ LÝ GAME STATE (Cần đảm bảo logic cập nhật điểm) ---

    /**
     * Cập nhật điểm số và số mục tiêu mới (Cho người vừa trả lời).
     */
    public void updateState(GameStateUpdatePacket packet) {
        Platform.runLater(() -> {
            // Cập nhật điểm của mình (yourScore) và điểm của đối thủ (opponentScore)
            yourScoreLabel.setText(String.valueOf(packet.getYourScore()));
            opponentScoreLabel.setText(String.valueOf(packet.getOpponentScore()));
            
            // Cập nhật số mục tiêu
            if (packet.getNextTargetNumber() == -1) {
                targetNumberLabel.setText("ĐÃ XONG!");
            } else {
                targetNumberLabel.setText(String.valueOf(packet.getNextTargetNumber()));
            }
        });
    }
    
    /**
     * (HÀM SỬA LỖI ĐIỂM) Cập nhật điểm số khi đối thủ trả lời.
     * Được gọi khi Client nhận ScoreUpdatePacket.
     */
    public void updateScoreOnly(ScoreUpdatePacket packet) {
        Platform.runLater(() -> {
            // MyScore của gói tin là điểm của bạn
            yourScoreLabel.setText(String.valueOf(packet.getMyScore()));
            // OpponentScore của gói tin là điểm của đối thủ
            opponentScoreLabel.setText(String.valueOf(packet.getOpponentScore()));
            
            // KHÔNG cập nhật targetNumberLabel hay nextTargetNumber
        });
    }

    // --- (Các hàm khởi tạo, updateTimer, showGameOver, onAction giữ nguyên) ---
    
    public void initializeGame(GameStartPacket packet) {
        User opponent = packet.getOpponent();
        
        Platform.runLater(() -> {
            targetNumberLabel.setText(String.valueOf(packet.getFirstTargetNumber()));
            opponentNameLabel.setText(opponent.getUsername());
            yourScoreLabel.setText("0"); 
            opponentScoreLabel.setText("0");
            // ... (phần còn lại của initializeGame)
            waitingForPlayAgain = false;
        });
    }
    
    public void updateTimer(GameTimerPacket packet) {
        Platform.runLater(() -> {
            int seconds = packet.getRemainingTimeInSeconds();
            timerLabel.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
        });
    }

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
                 alert.setContentText("Bạn có đồng ý không? (Đồng ý sẽ gửi yêu cầu chơi lại)");
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
        ClientNetworkService.getInstance().sendPacket(new ExitGameRequest());
        UIManager.getInstance().showLobbyScreen(UIManager.getInstance().getCurrentUser());
    }

    private void updateExpressionLabel() { expressionLabel.setText(currentExpression.length() == 0 ? "..." : currentExpression.toString()); }
    private void clearExpression() { currentExpression.setLength(0); updateExpressionLabel(); }
}
