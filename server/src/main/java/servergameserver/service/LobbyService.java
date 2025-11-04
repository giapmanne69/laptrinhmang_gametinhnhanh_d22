package servergameserver.service;

import commongameserver.model.OnlineUserInfo;
import commongameserver.model.PlayerStatus;
import commongameserver.model.User;
import commongameserver.network.Packet;
import commongameserver.network.request.ChallengeRequest;
import commongameserver.network.request.ChallengeResponse;
import commongameserver.network.response.ChallengeInvitationPacket;
import commongameserver.network.response.LoginResponse;
import commongameserver.network.response.OnlineListPacket;
// THÊM IMPORT GÓI TIN MỚI
import commongameserver.network.response.OpponentLeftAfterGamePacket;
import commongameserver.network.response.PlayAgainInvitationPacket;
import commongameserver.network.response.PlayAgainStatusPacket;
import io.netty.channel.Channel;
import servergameserver.db.UserDao;
import servergameserver.manager.ConnectionManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Lớp Singleton, quản lý sảnh chờ.
 * (Đã cập nhật logic handleUserDisconnect)
 */
public class LobbyService {

    // --- Singleton Pattern ---
    private static final LobbyService instance = new LobbyService();
    private LobbyService() {}
    public static LobbyService getInstance() { return instance; }

    // --- Dependencies ---
    private final ConnectionManager connManager = ConnectionManager.getInstance();
    private final GameManager gameManager = GameManager.getInstance();
    private final UserDao userDao = UserDao.getInstance();

    // --- State ---
    private final Map<String, PlayerStatus> playerStatuses = new ConcurrentHashMap<>();
    private final Map<String, String> lastOpponents = new ConcurrentHashMap<>();
    private final Map<String, String> playAgainWishes = new ConcurrentHashMap<>();

    
    public void handleUserLogin(User user, Channel channel) {
        LoginResponse response = new LoginResponse(true, "Dang nhap thanh cong.", user);
        channel.writeAndFlush(response);
        setUserStatus(user, PlayerStatus.AVAILABLE, true);
    }
public void handleUserDisconnect(User user, Channel channel) {
        // 1. Kiem tra xem ho co dang trong tran dau khong
        gameManager.playerLeftGame(user, channel); 
        
        String userId = user.getId();
        
        // 2. Thong bao cho doi thu (neu co)
        notifyOpponentOfDisconnect(userId);

        // 3. Don dep trang thai
        playerStatuses.remove(userId);
        lastOpponents.remove(userId); 
        playAgainWishes.remove(userId); 
        playAgainWishes.entrySet().removeIf(entry -> entry.getValue().equals(userId));
        
        // 4. Cap nhat danh sach cho moi nguoi
        broadcastOnlineList();
    }
    public void handleLeftGameOverScreen(Channel channel) {
        User user = connManager.getUser(channel);
        if (user == null) return; // User khong ton tai?

        // Chi thong bao cho doi thu (neu co)
        notifyOpponentOfDisconnect(user.getId());
        
        // Don dep logic choi lai
        lastOpponents.remove(user.getId());
        playAgainWishes.remove(user.getId());
        playAgainWishes.entrySet().removeIf(entry -> entry.getValue().equals(user.getId()));
    }

private void notifyOpponentOfDisconnect(String leavingUserId) {
        // Kiem tra xem nguoi nay co trong map 'lastOpponents' khong
        String opponentId = lastOpponents.get(leavingUserId); 
        
        if (opponentId != null) {
            // Neu co (tuc la ho vua choi xong va doi thu co the dang cho):
            
            // A. Lay thong tin nguoi thoat (de lay ten)
            User leavingUser = connManager.getUser(leavingUserId);
            if (leavingUser == null) return; // Khong tim thay ten, bo qua

            // B. Thong bao cho doi thu (nguoi o lai)
            Channel opponentChannel = connManager.getChannel(opponentId);
            if (opponentChannel != null && opponentChannel.isOpen()) {
                // Kiem tra xem doi thu co con o man hinh cho khong
                if (lastOpponents.containsKey(opponentId)) {
                    System.out.println("LobbyService: " + leavingUser.getUsername() + " da thoat (sau game). Thong bao cho " + opponentId);
                    opponentChannel.writeAndFlush(new OpponentLeftAfterGamePacket(leavingUser.getUsername()));
                }
            }
            
            // C. Xoa entry nguoc lai cua doi thu
            lastOpponents.remove(opponentId); 
        }
    }
    
    // ... (Cac ham khac: handleChallengeRequest, handleChallengeResponse, 
    //      registerLastOpponents, handlePlayAgainRequest, createOnlineListPacket, 
    //      broadcastOnlineList, sendOnlineListToUser, setUserStatus, sendErrorToChannel
    //      giu nguyen nhu code ban da co/toi da cung cap) ...

     // Vi du (ham nay ban da co):
     public void handleChallengeRequest(Channel challengerChannel, ChallengeRequest request) {
        User challenger = connManager.getUser(challengerChannel);
        if (challenger == null) return;
        String challengedUserId = request.getChallengedUserId();
        PlayerStatus status = playerStatuses.get(challengedUserId);
        if (challengedUserId.equals(challenger.getId())) {
             sendErrorToChannel(challengerChannel, "Ban khong the tu thach dau chinh minh.");
            return;
        }
        if (status == PlayerStatus.AVAILABLE) {
            Channel challengedChannel = connManager.getChannel(challengedUserId);
            User challengedUser = connManager.getUser(challengedChannel);
            if (challengedChannel != null && challengedUser != null) {
                ChallengeInvitationPacket invitation = new ChallengeInvitationPacket(
                        challenger.getId(), challenger.getUsername(),
                        challenger.getTotalScore(), challenger.getTotalWins()
                );
                challengedChannel.writeAndFlush(invitation);
            } else {
                sendErrorToChannel(challengerChannel, "Khong tim thay nguoi choi " + challengedUserId + ".");
            }
        } else {
             sendErrorToChannel(challengerChannel, "Nguoi choi " + challengedUserId + " dang ban hoac da offline.");
        }
    }
     
     // Vi du (ham nay ban da co):
     public void handleChallengeResponse(Channel challengedChannel, ChallengeResponse response) {
        User challengedUser = connManager.getUser(challengedChannel);
        if (challengedUser == null) return;
        String challengerId = response.getChallengerId();
        User challenger = connManager.getUser(challengerId);
        Channel challengerChannel = connManager.getChannel(challengerId);
        if (challenger == null || challengerChannel == null) {
            sendErrorToChannel(challengedChannel, "Nguoi thach dau " + challengerId + " da offline.");
            return;
        }
        if (response.isAccepted()) {
            if (playerStatuses.get(challengerId) == PlayerStatus.AVAILABLE &&
                playerStatuses.get(challengedUser.getId()) == PlayerStatus.AVAILABLE) {
                setUserStatus(challenger, PlayerStatus.BUSY, false);
                setUserStatus(challengedUser, PlayerStatus.BUSY, false);
                broadcastOnlineList();
                gameManager.createGame(challenger, challengerChannel, challengedUser, challengedChannel);
            } else {
                sendErrorToChannel(challengerChannel, "Khong the bat dau tran dau (Doi thu hoac ban dang ban).");
                sendErrorToChannel(challengedChannel, "Khong the bat dau tran dau (Doi thu hoac ban dang ban).");
            }
        } else {
            sendErrorToChannel(challengerChannel, "Nguoi choi " + challengedUser.getUsername() + " da tu choi loi moi.");
        }
    }

    // Vi du (ham nay ban da co):
    private OnlineListPacket createOnlineListPacket() {
        List<String> onlineUserIds = new ArrayList<>(playerStatuses.keySet());
        List<OnlineUserInfo> userInfos = onlineUserIds.stream()
                .map(userId -> {
                    User freshUser = userDao.getUserById(userId); 
                    PlayerStatus status = playerStatuses.getOrDefault(userId, PlayerStatus.AVAILABLE);
                    if (freshUser != null) {
                        return new OnlineUserInfo(freshUser, status);
                    }
                    return null; 
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new OnlineListPacket(userInfos);
    }
    
    // Vi du (ham nay ban da co):
    public void broadcastOnlineList() {
        System.out.println("LobbyService: Broadcast danh sach online...");
        OnlineListPacket packet = createOnlineListPacket();
        if (packet != null) {
            connManager.broadcast(packet);
        }
    }
    
    // Vi du (ham nay ban da co):
    public void sendOnlineListToUser(Channel channel) {
         User user = connManager.getUser(channel);
         if (user == null) return;
         System.out.println("LobbyService: Gui danh sach online cho " + user.getUsername());
         OnlineListPacket packet = createOnlineListPacket();
         if (packet != null) {
            channel.writeAndFlush(packet);
         }
    }
    
    // Vi du (ham nay ban da co):
    public void registerLastOpponents(String player1Id, String player2Id) {
        lastOpponents.put(player1Id, player2Id);
        lastOpponents.put(player2Id, player1Id);
        playAgainWishes.remove(player1Id); 
        playAgainWishes.remove(player2Id);
    }
    
    // Vi du (ham nay ban da co):
    public synchronized void handlePlayAgainRequest(Channel channel) {
        User requester = connManager.getUser(channel);
        if (requester == null) { return; }
        String requesterId = requester.getId();
        String opponentId = lastOpponents.get(requesterId);
        if (opponentId == null) {
            sendErrorToChannel(channel, "Khong tim thay doi thu gan nhat de choi lai.");
            return;
        }

        System.out.println("Lobby: " + requester.getUsername() + " muon choi lai voi " + opponentId);
        playAgainWishes.put(requesterId, opponentId);
        
        channel.writeAndFlush(new PlayAgainStatusPacket("Da gui yeu cau. Dang cho doi thu..."));
        String opponentWish = playAgainWishes.get(opponentId);

        if (opponentWish != null && opponentWish.equals(requesterId)) {
            System.out.println("Lobby: TRUNG KHOP CHOI LAI! Bat dau game...");
            
            playAgainWishes.remove(requesterId);
            playAgainWishes.remove(opponentId);
            lastOpponents.remove(requesterId);
            lastOpponents.remove(opponentId);

            User opponent = connManager.getUser(opponentId);
            Channel opponentChannel = connManager.getChannel(opponentId);

            if (opponent == null || opponentChannel == null || !opponentChannel.isOpen()) {
                 sendErrorToChannel(channel, "Khong the choi lai, doi thu da offline.");
                 setUserStatus(requester, PlayerStatus.AVAILABLE, true);
                return;
            }

            if (playerStatuses.get(requesterId) == PlayerStatus.AVAILABLE &&
                playerStatuses.get(opponentId) == PlayerStatus.AVAILABLE) {

                setUserStatus(requester, PlayerStatus.BUSY, false);
                setUserStatus(opponent, PlayerStatus.BUSY, false);
                broadcastOnlineList();
                gameManager.createGame(requester, channel, opponent, opponentChannel);
                
            } else {
                 sendErrorToChannel(channel, "Khong the choi lai, mot trong hai nguoi choi da ban.");
                 sendErrorToChannel(opponentChannel, "Khong the choi lai, mot trong hai nguoi choi da ban.");
                 if(playerStatuses.get(requesterId) != PlayerStatus.BUSY) setUserStatus(requester, PlayerStatus.AVAILABLE, false);
                 if(playerStatuses.get(opponentId) != PlayerStatus.BUSY) setUserStatus(opponent, PlayerStatus.AVAILABLE, false);
                 broadcastOnlineList();
            }
            
        } else {
            System.out.println("Lobby: " + requester.getUsername() + " dang cho " + opponentId + " dong y choi lai.");
            
            User opponentUser = connManager.getUser(opponentId);
            Channel opponentChannel = connManager.getChannel(opponentId);
            if (opponentUser != null && opponentChannel != null && opponentChannel.isOpen()) {
                 System.out.println("Lobby: Gui loi moi choi lai toi " + opponentUser.getUsername());
                 opponentChannel.writeAndFlush(new PlayAgainInvitationPacket(requester.getUsername()));
            } else {
                 System.out.println("Lobby: Doi thu " + opponentId + " khong online de nhan loi moi choi lai.");
                 sendErrorToChannel(channel, "Doi thu khong online de nhan loi moi choi lai.");
                 playAgainWishes.remove(requesterId); 
            }
        }
    }

     public void setUserStatus(User user, PlayerStatus status, boolean doBroadcast) {
        if (user == null) return;
        playerStatuses.put(user.getId(), status);
        if (doBroadcast) { broadcastOnlineList(); }
    }
    
     private void sendErrorToChannel(Channel channel, String message) {
        if (channel != null && channel.isOpen()) {
            LoginResponse errorResponse = new LoginResponse(false, message, null);
            channel.writeAndFlush(errorResponse);
        }
    }
}