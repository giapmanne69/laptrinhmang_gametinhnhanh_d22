package clientgameserver.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * Thiết lập pipeline cho Client.
 * Tương tự như Server, nhưng sử dụng ClientHandler.
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // (Chúng ta cũng tắt cảnh báo 'deprecated' ở đây)

        // 1. Decoder: Chuyển ByteBuf -> Object (Packet)
        @SuppressWarnings("deprecation")
        ObjectDecoder decoder = new ObjectDecoder(
                1024 * 1024, // Kích thước tối đa
                ClassResolvers.cacheDisabled(getClass().getClassLoader())
        );
        pipeline.addLast(decoder);

        // 2. Encoder: Chuyển Object (Packet) -> ByteBuf
        @SuppressWarnings("deprecation")
        ObjectEncoder encoder = new ObjectEncoder();
        pipeline.addLast(encoder);

        // 3. Logic Handler
        // ClientHandler sẽ nhận các gói tin PHẢN HỒI từ Server
        pipeline.addLast(new ClientHandler());
    }
}
