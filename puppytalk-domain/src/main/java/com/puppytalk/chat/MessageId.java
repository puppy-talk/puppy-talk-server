package com.puppytalk.chat;

/**
 * 메시지 식별자 값 객체
 */
public record MessageId(Long value) {
    
    /**
     * MessageId 생성 정적 팩토리 메서드
     */
    public static MessageId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("메시지 ID는 양수여야 합니다");
        }
        return new MessageId(value);
    }
    
    /**
     * 신규 메시지용 임시 ID 생성
     */
    public static MessageId newMessage() {
        return new MessageId(null);
    }
    
    /**
     * 저장된 메시지인지 확인
     */
    public boolean isStored() {
        return value != null && value > 0;
    }
}