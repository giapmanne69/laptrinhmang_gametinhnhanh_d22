package servergameserver.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Lớp chịu trách nhiệm khởi tạo (bootstrap) Netty Server.
 * (Đã viết lại để chia sẻ ServerHandler @Sharable
 * và quản lý EventLoopGroup một cách chính xác)
 */
public class GameServer {

    private final int port;
    
    // Các nhóm luồng (EventLoopGroup) cần được lưu
    // lại làm biến thành viên để có thể tắt (shutdown)
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public GameServer(int port) {
        this.port = port;
    }

    /**
     * Khởi chạy Netty Server.
     */
    public void run() throws Exception {
        // 1. EventLoopGroup cho 'boss' (chấp nhận kết nối)
        bossGroup = new NioEventLoopGroup(1);

        // 2. EventLoopGroup cho 'worker' (xử lý I/O)
        workerGroup = new NioEventLoopGroup();

        // --- SỬA ĐỔI QUAN TRỌNG ---
        // 3. Tạo thể hiện (instance) ServerHandler @Sharable MỘT LẦN
        final ServerHandler sharedServerHandler = new ServerHandler();

        try {
            // 4. ServerBootstrap là lớp trợ giúp để khởi tạo server
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // Sử dụng kênh NIO
                    
                    // Thêm Logging cho 'boss' group (hữu ích khi debug)
                    .handler(new LoggingHandler(LogLevel.INFO)) 
                    
                    // --- SỬA ĐỔI QUAN TRỌNG ---
                    // 5. Truyền Handler @Sharable vào Initializer
                    //    (Thay vì new ServerChannelInitializer())
                    .childHandler(new ServerChannelInitializer(sharedServerHandler)) 
                    
                    .option(ChannelOption.SO_BACKLOG, 128) // Hàng đợi kết nối
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // Giữ kết nối

            // 6. Bind (lắng nghe) vào cổng và chờ đồng bộ
            System.out.println("GameServer bắt đầu lắng nghe trên cổng " + port + "...");
            ChannelFuture f = b.bind(port).sync();

            // 7. Chờ cho đến khi kênh server bị đóng
            f.channel().closeFuture().sync();

        } finally {
            // 8. Tắt (shutdown) các EventLoopGroup một cách mượt mà
            shutdown();
        }
    }

    /**
     * Tắt các EventLoopGroup một cách an toàn.
     */
    public void shutdown() {
        System.out.println("Đang tắt GameServer...");
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }
}

