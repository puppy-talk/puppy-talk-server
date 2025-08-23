package com.puppytalk.support;

public enum ApiSuccessMessage {
    
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
    CHAT_MESSAGE_LIST_SUCCESS("채팅 메시지 목록을 조회했습니다"),
    
    // Common
    OPERATION_SUCCESS("작업이 완료되었습니다");
    
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