package com.puppytalk.activity;

/**
 * 사용자 활동 유형
 * 
 * Backend 관점: 성능과 안정성을 위한 최소한의 활동 타입 정의
 */
public enum ActivityType {
    
    /**
     * 로그인 활동
     */
    LOGIN("로그인"),
    
    /**
     * 메시지 전송 활동
     */
    MESSAGE_SENT("메시지 전송"),
    
    /**
     * 채팅방 진입 활동
     */
    CHAT_OPENED("채팅방 진입");
    
    private final String description;
    
    ActivityType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 중요 활동인지 판단 (비활성 사용자 감지 기준)
     */
    public boolean isCriticalActivity() {
        return this == LOGIN || this == MESSAGE_SENT || this == CHAT_OPENED;
    }
}