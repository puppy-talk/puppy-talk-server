package com.puppytalk.support;

/**
 * API 성공 응답 메시지 상수
 */
public enum ApiSuccessMessage {
    
    // Pet 관련 성공 메시지
    PET_CREATE_SUCCESS("반려동물이 성공적으로 생성되었습니다"),
    PET_LIST_SUCCESS("반려동물 목록을 성공적으로 조회했습니다"),
    PET_DETAIL_SUCCESS("반려동물 정보를 성공적으로 조회했습니다"),
    PET_DELETE_SUCCESS("반려동물이 성공적으로 삭제되었습니다"),
    
    // Chat 관련 성공 메시지 (미래를 위한 예약)
    CHAT_MESSAGE_SEND_SUCCESS("메시지가 성공적으로 전송되었습니다"),
    CHAT_ROOM_CREATE_SUCCESS("채팅방이 성공적으로 생성되었습니다"),
    CHAT_ROOM_LIST_SUCCESS("채팅방 목록을 성공적으로 조회했습니다"),
    CHAT_MESSAGE_LIST_SUCCESS("채팅 메시지 목록을 성공적으로 조회했습니다"),
    
    // 공통 성공 메시지
    OPERATION_SUCCESS("작업이 성공적으로 완료되었습니다");
    
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