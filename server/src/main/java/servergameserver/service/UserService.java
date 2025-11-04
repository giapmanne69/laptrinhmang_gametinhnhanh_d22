package servergameserver.service;

import commongameserver.model.User;
import commongameserver.network.request.LoginRequest;
import commongameserver.network.request.RegisterRequest; // Thêm import
import commongameserver.network.response.LoginResponse;
import commongameserver.network.response.RegisterResponse; // Thêm import
import io.netty.channel.Channel;
import servergameserver.db.UserDao;
import servergameserver.manager.ConnectionManager;

/**
 * Lớp Singleton, chịu trách nhiệm xử lý logic nghiệp vụ
 * liên quan đến User.
 * (Đã cập nhật để bao gồm logic Đăng ký)
 */
public class UserService {

    // --- Singleton Pattern ---
    private static final UserService instance = new UserService();
    private UserService() {}
    public static UserService getInstance() {
        return instance;
    }

    // --- Dependencies ---
    private final UserDao userDao = UserDao.getInstance();
    private final ConnectionManager connManager = ConnectionManager.getInstance();
    private final LobbyService lobbyService = LobbyService.getInstance();

    /**
     * Xử lý yêu cầu đăng nhập.
     */
    public void handleLoginRequest(Channel channel, LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        System.out.println("UserService: Xử lý đăng nhập cho " + username);

        User user = userDao.authenticateUser(username, password);

        if (user == null) {
            System.out.println("UserService: Đăng nhập thất bại. Sai thông tin cho " + username);
            LoginResponse response = new LoginResponse(false, "Sai tên đăng nhập hoặc mật khẩu.", null);
            channel.writeAndFlush(response);
            return;
        }

        if (connManager.getChannel(user.getId()) != null) {
            System.out.println("UserService: Đăng nhập thất bại. " + username + " đã online.");
            LoginResponse response = new LoginResponse(false, "Tài khoản này đã được đăng nhập ở nơi khác.", null);
            channel.writeAndFlush(response);
            return;
        }

        System.out.println("UserService: Đăng nhập thành công cho " + user.getUsername());
        connManager.registerUser(user, channel);
        lobbyService.handleUserLogin(user, channel);
    }

    /**
     * (HÀM MỚI) Xử lý yêu cầu đăng ký.
     */
    public void handleRegisterRequest(Channel channel, RegisterRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        System.out.println("UserService: Xử lý đăng ký cho " + username);

        if (username == null || username.length() < 4 || password == null || password.length() < 4) {
            RegisterResponse response = new RegisterResponse(false, "Tên và mật khẩu phải dài ít nhất 4 ký tự.");
            channel.writeAndFlush(response);
            return;
        }

        if (userDao.findUserByUsername(username)) {
            System.out.println("UserService: Đăng ký thất bại. Tên " + username + " đã tồn tại.");
            RegisterResponse response = new RegisterResponse(false, "Tên đăng nhập này đã tồn tại.");
            channel.writeAndFlush(response);
            return;
        }

        boolean createSuccess = userDao.createUser(username, password);

        if (createSuccess) {
            System.out.println("UserService: Đăng ký thành công cho " + username);
            RegisterResponse response = new RegisterResponse(true, "Đăng ký thành công. Bạn có thể đăng nhập.");
            channel.writeAndFlush(response);
        } else {
            System.out.println("UserService: Đăng ký thất bại. Lỗi CSDL.");
            RegisterResponse response = new RegisterResponse(false, "Lỗi máy chủ. Không thể tạo tài khoản.");
            channel.writeAndFlush(response);
        }
    }

    /**
     * Xử lý khi ngắt kết nối.
     */
    public void handleDisconnect(Channel channel) {
        User user = connManager.getUser(channel);

        if (user != null) {
            System.out.println("UserService: " + user.getUsername() + " đã ngắt kết nối.");
            lobbyService.handleUserDisconnect(user, channel);
            connManager.removeUser(channel);
        }
    }
}