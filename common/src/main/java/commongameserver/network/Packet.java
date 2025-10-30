package commongameserver.network;

import java.io.Serializable;

/**
 * Lớp cơ sở (abstract class) cho tất cả các gói tin (Packet)
 * được gửi giữa Client và Server.
 *
 * Mọi gói tin đều phải implement Serializable để có thể gửi qua Netty
 * (sử dụng ObjectEncoder/ObjectDecoder).
 * Mọi gói tin phải định nghĩa một PacketType.
 */
public abstract class Packet implements Serializable {

    // Đảm bảo tính nhất quán khi serialize/deserialize
    private static final long serialVersionUID = 10L;

    /**
     * Phương thức trừu tượng (abstract) buộc tất cả các lớp con
     * phải cung cấp loại (Type) của chúng.
     *
     * @return PacketType của gói tin
     */
    public abstract PacketType getType();

}
