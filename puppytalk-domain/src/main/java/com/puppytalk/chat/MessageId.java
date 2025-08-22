package com.puppytalk.chat;

import java.util.Objects;

/**
 * 메시지 식별자
 */
public class MessageId {
    
    private final Long value;
    
    private MessageId(Long value) {
        this.value = value;
    }
    
    /**
     * 메시지 ID 생성 정적 팩토리 메서드
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
     * 기존 메시지 ID 복원 (Repository용)
     */
    public static MessageId from(Long value) {
        return new MessageId(value);
    }
    
    /**
     * 저장된 ID인지 확인
     */
    public boolean isStored() {
        return value != null && value > 0;
    }
    
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MessageId other)) return false;
        return Objects.equals(value, other.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
    
    @Override
    public String toString() {
        return "MessageId{" +
                "value=" + value +
                '}';
    }
}