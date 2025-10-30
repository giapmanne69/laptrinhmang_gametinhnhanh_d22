package servergameserver.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * Lớp này được gọi cho MỖI kết nối client mới.
 *
 * LƯU Ý QUAN TRỌNG:
 * ObjectDecoder và ObjectEncoder đã bị deprecated (lỗi thời)
 * vì lý do bảo mật. Chúng ta sử dụng @SuppressWarnings
 * để tiếp tục dự án, nhưng trong một môi trường thực tế,
 * bạn nên chuyển sang dùng JSON (Gson) hoặc Protobuf.
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    // ServerHandler là @Sharable, chúng ta chia sẻ 1 thể hiện
    private final ServerHandler sharedServerHandler;

    public ServerChannelInitializer(ServerHandler sharedServerHandler) {
        this.sharedServerHandler = sharedServerHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // TẮT CẢNH BÁO (DEPRECATED)
        // Chúng ta biết nó lỗi thời, nhưng chấp nhận rủi ro
        // để tiếp tục dùng Java Serializable.
        
        // 1. Decoder: Chuyển ByteBuf -> Object (Packet)
        // (Sử dụng ClassLoader hiện tại thay vì null)
        @SuppressWarnings("deprecation")
        ObjectDecoder decoder = new ObjectDecoder(
                1024 * 1024, // Kích thước tối đa 1MB
                ClassResolvers.cacheDisabled(getClass().getClassLoader())
        );
        pipeline.addLast(decoder);

        // 2. Encoder: Chuyển Object (Packet) -> ByteBuf
        @SuppressWarnings("deprecation")
        ObjectEncoder encoder = new ObjectEncoder();
        pipeline.addLast(encoder);

        // 3. Logic Handler (Không đổi)
        pipeline.addLast(sharedServerHandler);
    }
}