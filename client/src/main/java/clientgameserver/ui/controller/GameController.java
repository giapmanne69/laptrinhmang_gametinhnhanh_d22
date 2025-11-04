package clientgameserver.ui.controller;

import clientgameserver.service.ClientNetworkService;
import clientgameserver.ui.UIManager;
import commongameserver.model.GameResult;
import commongameserver.model.User;
// THÊM IMPORT NÀY
import commongameserver.network.request.LogoutRequest;
import commongameserver.network.request.ExitGameRequest;
import commongameserver.network.request.PlayAgainRequest;
import commongameserver.network.request.SubmitAnswerRequest;
import commongameserver.network.response.GameStateUpdatePacket;
import commongameserver.network.response.GameStartPacket;
import commongameserver.network.response.GameTimerPacket;
import commongameserver.network.response.GameOverPacket;
import commongameserver.network.response.ScoreUpdatePacket;
import commongameserver.network.request.LeftGameOverScreenRequest;
// Import OpponentLeftAfterGamePacket (de ham showOpponentLeftAfterGame hoat dong)
import commongameserver.network.response.OpponentLeftAfterGamePacket;
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
 * Controller cho man hinh game.fxml
 * (DA CAP NHAT: Sua lai logic 'Ve Sanh' de kich hoat thong bao thoat)
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
    @FXML private Label constraintLabel; // Da them o luot truoc

    // --- FXML Bindings (Game Over) ---
    @FXML private AnchorPane gameOverPane;
    @FXML private Label winnerLabel;
    @FXML private Label finalScoreLabel;
    @FXML private Label playAgainStatusLabel;

    // --- Trang thai noi bo ---
    private StringBuilder currentExpression = new StringBuilder();
    private boolean waitingForPlayAgain = false; 

    /**
     * Duoc goi boi UIManager khi game bat dau.
     */
    public void initializeGame(GameStartPacket packet) {
        User opponent = packet.getOpponent();
        
        Platform.runLater(() -> {
            targetNumberLabel.setText(String.valueOf(packet.getFirstTargetNumber()));
            constraintLabel.setText(packet.getConstraintDescription());
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

    // --- CAC HAM XU LY SU KIEN FXML (onAction) ---

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
        // Thoat khi game DANG CHAY
        ClientNetworkService.getInstance().sendPacket(new ExitGameRequest());
        UIManager.getInstance().showLobbyScreen(UIManager.getInstance().getCurrentUser());
    }

    /**
     * (DA SUA) Xu ly khi bam nut "Ve Sanh" (khi game DA KET THUC).
     * Chung ta gui LogoutRequest de kich hoat logic don dep
     * handleDisconnect ben phia Server (va gui OpponentLeftAfterGamePacket).
     */
    @FXML
    void handleExitToLobbyClick(ActionEvent event) {
        // --- SUA O DAY ---
        // Gui LogoutRequest (thay vi ExitGameRequest)
        ClientNetworkService.getInstance().sendPacket(new LeftGameOverScreenRequest()); 
        // --- KET THUC SUA ---
        
        // Quay ve sanh
        UIManager.getInstance().showLobbyScreen(UIManager.getInstance().getCurrentUser());
    }

    @FXML
    void handlePlayAgainClick(ActionEvent event) {
        updatePlayAgainStatus("Da gui yeu cau. Dang cho doi thu...");
        waitingForPlayAgain = true; 
        ClientNetworkService.getInstance().sendPacket(new PlayAgainRequest());
    }
    
    // --- CAC HAM DUOC GOI BOI CLIENTHANDLER (LUONG NETTY) ---

    public void updateState(GameStateUpdatePacket packet) {
        Platform.runLater(() -> {
            yourScoreLabel.setText(String.valueOf(packet.getYourScore()));
            opponentScoreLabel.setText(String.valueOf(packet.getOpponentScore()));
            
            if (packet.getNextTargetNumber() == -1) {
                targetNumberLabel.setText("DA XONG!");
                constraintLabel.setText("Chuc mung ban da hoan thanh!");
            } else {
                targetNumberLabel.setText(String.valueOf(packet.getNextTargetNumber()));
                constraintLabel.setText(packet.getNextConstraintDescription());
            }
        });
    }
    
    public void updateScoreOnly(ScoreUpdatePacket packet) {
        Platform.runLater(() -> {
            yourScoreLabel.setText(String.valueOf(packet.getMyScore()));
            opponentScoreLabel.setText(String.valueOf(packet.getOpponentScore()));
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
            
            if (result.getWinnerId() == null) { winnerText = "HOA!"; } 
            else if (result.getWinnerId().equals(myId)) { winnerText = "BAN THANG!"; } 
            else { winnerText = "BAN THUA!"; }

            winnerLabel.setText(winnerText);
            
            boolean iAmPlayer1 = result.getPlayer1Id().equals(myId);
            int myFinalScore = iAmPlayer1 ? result.getPlayer1Score() : result.getPlayer2Score();
            int opponentFinalScore = iAmPlayer1 ? result.getPlayer2Score() : result.getPlayer1Score();
            
            finalScoreLabel.setText(String.format("Ti so: %d - %d", myFinalScore, opponentFinalScore));
            
            playAgainStatusLabel.setText(""); 
            gameOverPane.setVisible(true);
            waitingForPlayAgain = true; 
        });
    }

    /**
     * (GIU NGUYEN) Xu ly khi doi thu thoat (DANG TRONG TRAN DAU).
     */
    public void showOpponentLeft() {
        Platform.runLater(() -> {
            if (!gameOverPane.isVisible()) { 
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Doi thu da thoat tran");
                alert.getButtonTypes().setAll(ButtonType.OK); 
                alert.setHeaderText("Doi thu cua ban da thoat khoi tran dau.");
                alert.setContentText("Ban duoc xu thang. Tro ve sanh.");
                
                alert.showAndWait();
                UIManager.getInstance().showLobbyScreen(UIManager.getInstance().getCurrentUser());
            } 
        });
    }

    /**
     * (HAM MOI) Xu ly khi doi thu thoat (SAU KHI TRAN DAU KET THUC).
     * Duoc goi khi nhan OpponentLeftAfterGamePacket.
     */
    public void showOpponentLeftAfterGame(String opponentUsername) {
        Platform.runLater(() -> {
            // Chi hien thi neu dang o man hinh Game Over
            if (gameOverPane.isVisible()) { 
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Doi thu da thoat");
                alert.getButtonTypes().setAll(ButtonType.OK); // Chi co nut OK
                
                // Hien thi thong bao nhu ban yeu cau
                alert.setHeaderText("Nguoi choi '" + opponentUsername + "' da thoat.");
                alert.setContentText("Quay ve sanh chinh.");
                
                // Cho nguoi dung bam OK
                alert.showAndWait();
                
                // Quay ve sanh
                UIManager.getInstance().showLobbyScreen(UIManager.getInstance().getCurrentUser());
            }
        });
    }

    // --- Cac ham xu ly Play Again ---
    
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
                 alert.setTitle("Moi Choi Lai");
                 alert.setHeaderText(requesterUsername + " muon choi lai voi ban.");
                 alert.setContentText("Ban co dong y khong?");
                 ButtonType okButton = new ButtonType("Dong y");
                 ButtonType noButton = new ButtonType("Tu choi");
                 alert.getButtonTypes().setAll(okButton, noButton);

                 Optional<ButtonType> result = alert.showAndWait();
                 
                 if (result.isPresent() && result.get() == okButton) {
                     updatePlayAgainStatus("Da chap nhan. Dang cho Server...");
                     ClientNetworkService.getInstance().sendPacket(new PlayAgainRequest());
                     waitingForPlayAgain = true; 
                 } 
            } else if (gameOverPane.isVisible() && waitingForPlayAgain) {
                 updatePlayAgainStatus("Doi phuong cung dong y. Dang cho Server...");
            }
        });
    }
    
    // --- Tien ich ---
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