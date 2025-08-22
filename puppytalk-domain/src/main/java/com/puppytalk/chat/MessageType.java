package com.puppytalk.chat;

/**
 * 메시지 타입
 */
public enum MessageType {
    
    /**
     * 사용자가 보낸 메시지
     */
    USER("사용자 메시지"),
    
    /**
     * 반려동물(AI)이 보낸 메시지
     */
    PET("반려동물 메시지");
    
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