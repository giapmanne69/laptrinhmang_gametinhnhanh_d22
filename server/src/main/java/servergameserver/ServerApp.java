package servergameserver;

import servergameserver.netty.GameServer;

/**
 * Lớp chính (Main Class) để khởi chạy Server.
 */
public class ServerApp {

    // Cổng (port) mà server sẽ lắng nghe
    private static final int PORT = 9090;

    public static void main(String[] args) {
        // (Bạn có thể khởi tạo các Service/DAO tại đây nếu cần)
        System.out.println("Khởi động Game Server...");

        // --- SỬA LỖI ---
        // Phải truyền 'PORT' vào hàm khởi tạo của GameServer
        // vì GameServer.java định nghĩa 'public GameServer(int port)'
        GameServer server = new GameServer(PORT);
        
        try {
            // Chạy server trên cổng đã định
            // (Hàm run() không cần tham số nữa, vì port đã được
            // truyền vào constructor)
            server.run();
        } catch (Exception e) {
            System.err.println("Không thể khởi động server!");
            e.printStackTrace();
        }
    }
}

