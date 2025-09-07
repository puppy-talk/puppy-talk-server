package com.puppytalk.notification;

/**
 * 알림 유형
 * 
 * Backend 관점: 확장 가능한 알림 타입 정의
 */
public enum NotificationType {
    
    /**
     * 반려동물 메시지 (반려동물이 보내는 메시지)
     */
    PET_MESSAGE("반려동물 메시지"),
    
    /**
     * 비활성 사용자 알림 (반려동물이 보내는 메시지)
     */
    INACTIVITY_MESSAGE("비활성 사용자 메시지"),
    
    /**
     * 채팅 알림 (실시간 메시지)
     */
    CHAT_MESSAGE("채팅 메시지"),
    
    /**
     * 반려동물 상태 알림
     */
    PET_STATUS("반려동물 상태");
    
    private final String description;
    
    NotificationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 즉시 발송이 필요한 알림인지 판단
     */
    public boolean isUrgent() {
        return this == CHAT_MESSAGE;
    }
    
    /**
     * 배치 처리 가능한 알림인지 판단
     */
    public boolean isBatchable() {
        return this == PET_MESSAGE || this == INACTIVITY_MESSAGE || this == PET_STATUS;
    }
    
    /**
     * AI 생성이 필요한 알림인지 판단
     */
    public boolean requiresAiGeneration() {
        return this == PET_MESSAGE || this == INACTIVITY_MESSAGE;
    }
}