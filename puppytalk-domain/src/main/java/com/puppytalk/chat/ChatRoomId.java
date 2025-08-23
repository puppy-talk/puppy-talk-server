package com.puppytalk.chat;

import java.util.Objects;

public class ChatRoomId {
    
    private final Long value;
    
    private ChatRoomId(Long value) {
        this.value = value;
    }

    public static ChatRoomId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("채팅방 ID는 양수여야 합니다");
        }
        return new ChatRoomId(value);
    }
    
    public static ChatRoomId from(Long value) {
        return new ChatRoomId(value);
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
        if (!(obj instanceof ChatRoomId other)) return false;
        return Objects.equals(value, other.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
    
    @Override
    public String toString() {
        return "ChatRoomId{" +
                "value=" + value +
                '}';
    }
}