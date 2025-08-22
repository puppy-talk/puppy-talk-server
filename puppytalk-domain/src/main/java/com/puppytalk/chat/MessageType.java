package com.puppytalk.chat;

/**
 * 메시지 타입 열거형
 */
public enum MessageType {
    
    /**
     * 사용자가 전송한 메시지
     */
    USER("사용자"),
    
    /**
     * 반려동물이 전송한 메시지 (AI 생성)
     */
    PET("반려동물");
    
    private final String description;
    
    MessageType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 사용자 메시지인지 확인
     */
    public boolean isUserMessage() {
        return this == USER;
    }
    
    /**
     * 반려동물 메시지인지 확인
     */
    public boolean isPetMessage() {
        return this == PET;
    }
}