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
import commongameserver.network.response.*; 

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;

/**
 * Xử lý logic khi NHẬN được gói tin TỪ SERVER.
 * (Đã cập nhật để xử lý SCORE_UPDATE_PACKET và Play Again packets)
 */
public class ClientHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        PacketType type = packet.getType();

        switch (type) {
            // --- Đăng nhập / Đăng ký / Sảnh ---
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
            case REQUEST_ONLINE_LIST:
                // Dù Server có thể tự xử lý, ta vẫn nên log hoặc bỏ qua
                System.out.println("ClientHandler: Nhận được REQUEST_ONLINE_LIST (Bỏ qua)");
                break;
            case ERROR_PACKET: 
                 // TODO: Xử lý gói tin lỗi chung (nếu cần)
                 System.err.println("ClientHandler: Nhận được ERROR_PACKET");
                 break;


            // --- Trong Game ---
            case GAME_START_PACKET: 
                UIManager.getInstance().showGameScreen((GameStartPacket) packet); 
                break;
            case GAME_STATE_UPDATE_PACKET: 
                // Gói tin dành cho người vừa trả lời (cập nhật điểm và câu hỏi mới)
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
            
            // --- CẬP NHẬT ĐIỂM CHO ĐỐI THỦ ---
            case SCORE_UPDATE_PACKET:
                 // Gói tin dành cho đối thủ (chỉ cập nhật điểm)
                 Platform.runLater(() -> GameController.getInstance().updateScoreOnly((ScoreUpdatePacket) packet));
                 break;
            // --- KẾT THÚC CẬP NHẬT ĐIỂM ---
            case USER_UPDATE_PACKET:
                // Nhận User mới và cập nhật vào UIManager
                final UserUpdatePacket userPacket = (UserUpdatePacket) packet;
                Platform.runLater(() -> UIManager.getInstance().setCurrentUser(userPacket.getUpdatedUser()));
                break;

            // --- XỬ LÝ CHƠI LẠI (PLAY AGAIN) ---
            case PLAY_AGAIN_STATUS:
                final String statusMsg = ((PlayAgainStatusPacket) packet).getMessage();
                Platform.runLater(() -> GameController.getInstance().updatePlayAgainStatus(statusMsg));
                break;
            case PLAY_AGAIN_INVITATION:
                final String inviter = ((PlayAgainInvitationPacket) packet).getRequesterUsername();
                Platform.runLater(() -> GameController.getInstance().showPlayAgainInvitation(inviter));
                break;
            // --- KẾT THÚC XỬ LÝ CHƠI LẠI ---
            
            default:
                System.out.println("ClientHandler: Nhận được loại Packet không xác định: " + type);
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
        System.err.println("Lỗi trong ClientHandler: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
