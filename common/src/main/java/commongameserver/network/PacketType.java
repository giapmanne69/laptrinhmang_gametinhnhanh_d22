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
    EXIT_GAME_REQUEST, // (Dùng khi đang trong trận)
    REQUEST_LEADERBOARD_REQUEST,
    LOGOUT_REQUEST, // (Dùng khi đăng xuất/thoát game)
    REQUEST_ONLINE_LIST,
    
    // --- (MOI) GÓI TIN THOÁT MÀN HÌNH GAME OVER ---
    LEFT_GAME_OVER_SCREEN_REQUEST,

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
    OPPONENT_EXIT_PACKET, // (Dùng khi đối thủ thoát GIỮA TRẬN)
    LEADERBOARD_PACKET,
    PLAY_AGAIN_STATUS,
    PLAY_AGAIN_INVITATION,
    
    // (MOI) GÓI TIN THÔNG BÁO THOÁT SAU GAME
    OPPONENT_LEFT_AFTER_GAME,
    
    USER_UPDATE_PACKET,
    ERROR_PACKET 
}