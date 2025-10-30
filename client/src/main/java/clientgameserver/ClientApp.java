package clientgameserver;

import clientgameserver.netty.NettyClient;
import clientgameserver.service.ClientNetworkService;
import clientgameserver.ui.UIManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Lớp JavaFX Application (UI) chính.
 * (Đã cập nhật để khởi chạy UIManager)
 */
public class ClientApp extends Application {

    private NettyClient nettyClient;
    private static final String HOST = "127.0.0.1"; // Địa chỉ Server
    private static final int PORT = 9090;          // Cổng Server

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("ClientApp: Đang khởi động UI...");

        // 1. Khởi tạo UIManager với Stage chính
        UIManager.getInstance().initialize(primaryStage);

        System.out.println("ClientApp: Khởi chạy Netty Client...");
        nettyClient = new NettyClient(HOST, PORT);

        // 2. Cung cấp NettyClient cho Service
        // (Để các Controller có thể gọi)
        ClientNetworkService.getInstance().setNettyClient(nettyClient);

        // 3. Chạy Netty Client trên luồng riêng
        Thread clientThread = new Thread(() -> {
            try {
                nettyClient.run();
            } catch (Exception e) {
                System.err.println("Lỗi Netty Client: " + e.getMessage());
                // TODO: Hiển thị lỗi này trên UI
                // UIManager.getInstance().showError("Không thể kết nối máy chủ.");
            }
        });
        
        clientThread.setDaemon(true); 
        clientThread.start();
        
        // 4. Hiển thị màn hình Login
        primaryStage.setTitle("Game Tính Nhanh");
        UIManager.getInstance().showLoginScreen();
        primaryStage.show();
    }

    /**
     * Được gọi khi ứng dụng JavaFX tắt.
     */
    @Override
    public void stop() throws Exception {
        System.out.println("ClientApp: Đang tắt...");
        if (nettyClient != null) {
            nettyClient.shutdown(); // Tắt Netty
        }
    }
}

