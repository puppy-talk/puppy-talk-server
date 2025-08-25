package com.puppytalk.support;

/**
 * API 성공 응답 메시지 상수
 */
public enum ApiSuccessMessage {
    
    // User
    USER_CREATE_SUCCESS("사용자가 생성되었습니다"),
    USER_GET_SUCCESS("사용자 정보를 조회했습니다"),

    // Pet
    PET_CREATE_SUCCESS("반려동물이 생성되었습니다"),
    PET_LIST_SUCCESS("반려동물 목록을 조회했습니다"),
    PET_DETAIL_SUCCESS("반려동물 정보를 조회했습니다"),
    PET_DELETE_SUCCESS("반려동물이 삭제되었습니다"),
    
    // Chat
    CHAT_MESSAGE_SEND_SUCCESS("메시지가 전송되었습니다"),
    CHAT_ROOM_CREATE_SUCCESS("채팅방이 생성되었습니다"),
    CHAT_ROOM_FIND_SUCCESS("기존 채팅방을 조회했습니다"),
    CHAT_ROOM_LIST_SUCCESS("채팅방 목록을 조회했습니다"),

    // Message,
    MESSAGE_LIST_SUCCESS("메시지 목록을 조회했습니다"),
    MESSAGE_FOUND_SUCCESS("메시지를 조회했습니다"),
    NEW_MESSAGE_FOUND_SUCCESS("새로운 메시지가 있습니다."),
    NO_NEW_MESSAGE_FOUND_SUCCESS("새로운 메시지가 없습니다."),

    // Notification
    NOTIFICATION_CREATE_SUCCESS("알림이 생성되었습니다"),
    NOTIFICATION_STATUS_UPDATE_SUCCESS("알림 상태가 업데이트되었습니다"),
    NOTIFICATION_LIST_SUCCESS("알림 목록을 조회했습니다"),
    NOTIFICATION_CLEANUP_SUCCESS("알림 정리가 완료되었습니다");
    
    private final String message;
    
    ApiSuccessMessage(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return message;
    }
}