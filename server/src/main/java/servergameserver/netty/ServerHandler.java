package servergameserver.netty;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;
import commongameserver.network.request.RegisterRequest; 
import commongameserver.network.request.ChallengeRequest;
import commongameserver.network.request.ChallengeResponse;
import commongameserver.network.request.LoginRequest;
import commongameserver.network.request.SubmitAnswerRequest;
import commongameserver.network.request.LeftGameOverScreenRequest; 
import commongameserver.network.request.LogoutRequest;
import commongameserver.network.request.RequestOnlineListPacket; 
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import servergameserver.game.GameSession;
import servergameserver.service.GameManager;
import servergameserver.service.LeaderboardService;
import servergameserver.service.LobbyService;
import servergameserver.service.UserService;
import java.util.List; 
import commongameserver.model.User; 
import commongameserver.network.response.LeaderboardPacket; 

@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<Packet> {

    private final UserService userService = UserService.getInstance();
    private final LobbyService lobbyService = LobbyService.getInstance();
    private final GameManager gameManager = GameManager.getInstance();
    private final LeaderboardService leaderboardService = LeaderboardService.getInstance();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        PacketType type = packet.getType();
        
        // LOG CHẨN ĐOÁN CHUNG: Gói tin được nhận
        System.out.println("SERVERHANDLER: NHAN PACKET. Type: " + type + ", Kênh: " + ctx.channel().id());

        switch (type) {
            case LOGIN_REQUEST:
                userService.handleLoginRequest(ctx.channel(), (LoginRequest) packet);
                break;
            case REGISTER_REQUEST:
                userService.handleRegisterRequest(ctx.channel(), (RegisterRequest) packet);
                break;
            case CHALLENGE_REQUEST:
                lobbyService.handleChallengeRequest(ctx.channel(), (ChallengeRequest) packet);
                break;
            case CHALLENGE_RESPONSE:
                lobbyService.handleChallengeResponse(ctx.channel(), (ChallengeResponse) packet);
                break;
                
            // --- XỬ LÝ SUBMIT ANSWER ---
            case SUBMIT_ANSWER_REQUEST:
                System.out.println("SERVERHANDLER: Dinh tuyen toi GameSession.");
                
                GameSession gameSession = gameManager.getSession(ctx.channel());
                if (gameSession != null) {
                    gameSession.handlePlayerAnswer(ctx.channel(), ((SubmitAnswerRequest) packet).getExpression());
                } else {
                     System.err.println("SERVERHANDLER: LOI - Khong tim thay GameSession. Bo qua SUBMIT_ANSWER.");
                }
                break;
            // --- KẾT THÚC SUBMIT ANSWER ---
                
            case LOGOUT_REQUEST:
                userService.handleDisconnect(ctx.channel());
                break;
                
            case PLAY_AGAIN_REQUEST:
                lobbyService.handlePlayAgainRequest(ctx.channel());
                break;

            case EXIT_GAME_REQUEST:
                GameSession sessionToExit = gameManager.getSession(ctx.channel());
                if (sessionToExit != null) {
                    sessionToExit.playerLeftGame(ctx.channel());
                }
                break;
                
            case REQUEST_ONLINE_LIST: 
                lobbyService.sendOnlineListToUser(ctx.channel());
                break;

            case REQUEST_LEADERBOARD_REQUEST:
                List<User> sortedLeaderboard = leaderboardService.getLeaderboard();
                LeaderboardPacket leaderboardPacket = new LeaderboardPacket(sortedLeaderboard);
                ctx.channel().writeAndFlush(leaderboardPacket);
                break;
            case LEFT_GAME_OVER_SCREEN_REQUEST:
                // Khong ngat ket noi user, chi don dep logic Play Again
                lobbyService.handleLeftGameOverScreen(ctx.channel());
                break;
            default:
                // LOG CHẨN ĐOÁN: Bất kỳ gói tin nào không xử lý được
                System.out.println("SERVERHANDLER: CANH BAO - Nhan duoc Packet khong duoc xu ly: " + type);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Kết nối mới: Một Client đã tham gia - " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Kết nối bị mất: Client đã thoát - " + ctx.channel().remoteAddress());
        userService.handleDisconnect(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("Lỗi trong ServerHandler: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
