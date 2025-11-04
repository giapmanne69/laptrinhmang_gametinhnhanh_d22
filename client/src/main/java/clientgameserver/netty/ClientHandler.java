package clientgameserver.netty;

// Import controllers
import clientgameserver.ui.UIManager;
import clientgameserver.ui.controller.GameController;
import clientgameserver.ui.controller.LobbyController;
import clientgameserver.ui.controller.LoginController;
import clientgameserver.ui.controller.RegisterController;
import clientgameserver.ui.controller.LeaderboardController;

// Import packets
import commongameserver.network.Packet;
import commongameserver.network.PacketType;
import commongameserver.network.response.*; // Import tat ca response (bao gom ca OpponentLeftAfterGamePacket)

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;

/**
 * Xu ly logic khi NHAN duoc goi tin TU SERVER.
 * (Da cap nhat de xu ly OpponentLeftAfterGamePacket)
 */
public class ClientHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        PacketType type = packet.getType();

        switch (type) {
            // --- Dang nhap / Dang ky / Sanh ---
            case LOGIN_RESPONSE: 
                handleLoginResponse((LoginResponse) packet); 
                break;
            case REGISTER_RESPONSE: 
                handleRegisterResponse((RegisterResponse) packet); 
                break;
            case ONLINE_LIST_PACKET: 
                Platform.runLater(() -> LobbyController.getInstance().updateOnlineList((OnlineListPacket) packet)); 
                break;
            case CHALLENGE_INVITATION_PACKET: 
                Platform.runLater(() -> LobbyController.getInstance().showChallengePopup((ChallengeInvitationPacket) packet)); 
                break;
            case LEADERBOARD_PACKET: 
                Platform.runLater(() -> LeaderboardController.getInstance().updateLeaderboard((LeaderboardPacket) packet)); 
                break;
            case USER_UPDATE_PACKET:
                final UserUpdatePacket userPacket = (UserUpdatePacket) packet;
                Platform.runLater(() -> UIManager.getInstance().setCurrentUser(userPacket.getUpdatedUser()));
                break;

            // --- Trong Game ---
            case GAME_START_PACKET: 
                UIManager.getInstance().showGameScreen((GameStartPacket) packet); 
                break;
            case GAME_STATE_UPDATE_PACKET: 
                Platform.runLater(() -> GameController.getInstance().updateState((GameStateUpdatePacket) packet)); 
                break;
            case GAME_TIMER_PACKET: 
                Platform.runLater(() -> GameController.getInstance().updateTimer((GameTimerPacket) packet)); 
                break;
            case GAME_OVER_PACKET: 
                Platform.runLater(() -> GameController.getInstance().showGameOver((GameOverPacket) packet)); 
                break;
            case OPPONENT_EXIT_PACKET: 
                Platform.runLater(() -> GameController.getInstance().showOpponentLeft()); 
                break;
            case SCORE_UPDATE_PACKET:
                 Platform.runLater(() -> GameController.getInstance().updateScoreOnly((ScoreUpdatePacket) packet));
                 break;

            // --- Xu ly Choi Lai ---
            case PLAY_AGAIN_STATUS:
                final String statusMsg = ((PlayAgainStatusPacket) packet).getMessage();
                Platform.runLater(() -> GameController.getInstance().updatePlayAgainStatus(statusMsg));
                break;
            case PLAY_AGAIN_INVITATION:
                final String inviter = ((PlayAgainInvitationPacket) packet).getRequesterUsername();
                Platform.runLater(() -> GameController.getInstance().showPlayAgainInvitation(inviter));
                break;

            // --- (MOI) XU LY DOI THU THOAT SAU KHI CHOI XONG ---
            case OPPONENT_LEFT_AFTER_GAME:
                final String opponentName = ((OpponentLeftAfterGamePacket) packet).getOpponentUsername();
                Platform.runLater(() -> GameController.getInstance().showOpponentLeftAfterGame(opponentName));
                break;
            // --- KET THUC XU LY MOI ---
                
            case REQUEST_ONLINE_LIST:
                 System.out.println("ClientHandler: Nhan duoc REQUEST_ONLINE_LIST (Bo qua)");
                 break;
                 
            case ERROR_PACKET: 
                 System.err.println("ClientHandler: Nhan duoc ERROR_PACKET");
                 break;

            default:
                System.out.println("ClientHandler: Nhan duoc loai Packet khong xac dinh: " + type);
        }
    }

    private void handleLoginResponse(LoginResponse response) {
        if (response.isSuccess()) { 
            UIManager.getInstance().showLobbyScreen(response.getUser()); 
        } else { 
            LoginController.getInstance().showError(response.getMessage()); 
        }
    }
    
    private void handleRegisterResponse(RegisterResponse response) {
        if (response.isSuccess()) { 
            RegisterController.getInstance().showSuccess(response.getMessage()); 
        } else { 
            RegisterController.getInstance().showError(response.getMessage()); 
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("Loi trong ClientHandler: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}