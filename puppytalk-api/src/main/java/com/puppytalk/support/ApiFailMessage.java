package com.puppytalk.support;

/**
 * API 실패 응답 메시지 상수
 */
public enum ApiFailMessage {
    
    // Pet 관련 실패 메시지
    PET_NOT_FOUND("반려동물을 찾을 수 없습니다"),
    PET_ACCESS_DENIED("반려동물에 대한 접근 권한이 없습니다"),
    PET_ALREADY_DELETED("이미 삭제된 반려동물입니다"),
    PET_NAME_INVALID("반려동물 이름이 유효하지 않습니다"),
    PET_PERSONA_NOT_FOUND("페르소나를 찾을 수 없습니다"),
    
    // Chat 관련 실패 메시지 (미래를 위한 예약)
    CHAT_ROOM_NOT_FOUND("채팅방을 찾을 수 없습니다"),
    CHAT_MESSAGE_NOT_FOUND("채팅 메시지를 찾을 수 없습니다"),
    CHAT_ACCESS_DENIED("채팅방에 대한 접근 권한이 없습니다"),
    CHAT_MESSAGE_EMPTY("메시지 내용이 비어있습니다"),
    
    // 공통 실패 메시지
    VALIDATION_FAILED("입력값 검증에 실패했습니다"),
    OPERATION_FAILED("작업 처리 중 오류가 발생했습니다"),
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다"),
    BAD_REQUEST("잘못된 요청입니다"),
    UNAUTHORIZED("인증이 필요합니다"),
    FORBIDDEN("접근 권한이 없습니다"),
    NOT_FOUND("요청한 리소스를 찾을 수 없습니다");
    
    private final String message;
    
    ApiFailMessage(String message) {
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