package commongameserver.model;

/**
 * Định nghĩa các trạng thái (status) mà một người chơi
 * đang online có thể có.
 *
 * Implement Serializable để có thể gửi qua Netty.
 */
public enum PlayerStatus  {
    /**
     * Người chơi đang rỗi, ở sảnh chờ.
     * Có thể nhận lời mời thách đấu.
     */
    AVAILABLE,

    /**
     * Người chơi đang bận (đang trong một trận đấu).
     * Không thể nhận lời mời thách đấu.
     */
    BUSY
}
