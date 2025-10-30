package clientgameserver.service;

import clientgameserver.netty.NettyClient;
import commongameserver.network.Packet;

/**
 * Lớp Singleton, đóng vai trò "cầu nối"
 * để các UI Controller có thể gửi Packet
 * mà không cần giữ tham chiếu trực tiếp đến NettyClient.
 */
public class ClientNetworkService {

    // --- Singleton Pattern ---
    private static final ClientNetworkService instance = new ClientNetworkService();

    private ClientNetworkService() {
    }

    public static ClientNetworkService getInstance() {
        return instance;
    }

    // --- Logic ---
    private NettyClient nettyClient;

    /**
     * Được gọi bởi ClientApp khi khởi động,
     * để gán thể hiện (instance) NettyClient.
     */
    public void setNettyClient(NettyClient client) {
        this.nettyClient = client;
    }

    /**
     * Hàm tiện ích để các Controller gọi.
     * @param packet Gói tin cần gửi
     */
    public void sendPacket(Packet packet) {
        if (nettyClient != null) {
            nettyClient.sendPacket(packet);
        } else {
            System.err.println("Lỗi: ClientNetworkService chưa được khởi tạo NettyClient!");
        }
    }
}
