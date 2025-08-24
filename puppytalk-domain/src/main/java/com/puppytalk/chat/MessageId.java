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
    
    public static MessageId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("메시지 ID는 양수여야 합니다");
        }
        return new MessageId(value);
    }
    
    public static MessageId create() {
        return new MessageId(null);
    }
    
    public static MessageId from(Long value) {
        return new MessageId(value);
    }
    
    public boolean isValid() {
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