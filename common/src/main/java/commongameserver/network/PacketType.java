package commongameserver.network;

import java.io.Serializable;

/**
 * Enum (liệt kê) tất cả các loại gói tin (Packet)
 * (Đã cập nhật)
 */
public enum PacketType implements Serializable {

    // === Client -> Server (Requests) ===
    LOGIN_REQUEST,
    REGISTER_REQUEST,
    CHALLENGE_REQUEST,
    CHALLENGE_RESPONSE,
    SUBMIT_ANSWER_REQUEST,
    PLAY_AGAIN_REQUEST,
    EXIT_GAME_REQUEST,
    REQUEST_LEADERBOARD_REQUEST,
    LOGOUT_REQUEST,
    REQUEST_ONLINE_LIST,

    // === Server -> Client (Responses/Packets) ===
    SCORE_UPDATE_PACKET,
    LOGIN_RESPONSE,
    REGISTER_RESPONSE,
    ONLINE_LIST_PACKET,
    CHALLENGE_INVITATION_PACKET,
    GAME_START_PACKET,
    GAME_STATE_UPDATE_PACKET,
    GAME_TIMER_PACKET,
    GAME_OVER_PACKET,
    OPPONENT_EXIT_PACKET,
    LEADERBOARD_PACKET,
    PLAY_AGAIN_STATUS,      // <-- ĐÃ THÊM: Trạng thái chờ chơi lại
    PLAY_AGAIN_INVITATION,  // <-- ĐÃ THÊM: Lời mời chơi lại
    USER_UPDATE_PACKET,
    
    ERROR_PACKET // Gói tin lỗi chung
}

