package clientgameserver.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Lớp chịu trách nhiệm khởi tạo và quản lý kết nối Netty
 * (Bootstrap) đến Server.
 */
public class NettyClient {

    private final String host;
    private final int port;
    private EventLoopGroup group;
    private Channel channel; // Kênh (Channel) kết nối tới Server

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception {
        // Client chỉ cần 1 EventLoopGroup
        group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    // Không dùng .handler() (cho ServerSocket)
                    // mà dùng .handler() (cho SocketChannel)
                    // Nó được đặt bên trong Initializer
                    .handler(new ClientChannelInitializer());

            System.out.println("NettyClient: Đang kết nối tới " + host + ":" + port + "...");
            
            // Bắt đầu kết nối
            ChannelFuture f = b.connect(host, port).sync();
            
            // Lưu lại kênh để gửi dữ liệu sau này
            this.channel = f.channel();
            System.out.println("NettyClient: Kết nối thành công!");

            // Chờ cho đến khi kênh bị đóng
            f.channel().closeFuture().sync();

        } finally {
            // Tắt nhóm luồng khi kết thúc
            System.out.println("NettyClient: Ngắt kết nối.");
            shutdown();
        }
    }

    /**
     * Gửi một gói tin (Packet) đến Server.
     * (Hàm này sẽ được gọi bởi các UI Controller)
     * @param packet Gói tin cần gửi
     */
    public void sendPacket(Object packet) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(packet);
        } else {
            System.err.println("NettyClient: Không thể gửi packet. Kênh chưa kết nối.");
        }
    }

    /**
     * Tắt EventLoopGroup.
     */
    public void shutdown() {
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}
