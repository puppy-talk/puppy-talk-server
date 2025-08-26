package com.puppytalk.chat;

import java.util.Objects;

public class ChatRoomId {
    
    private final Long value;
    
    private ChatRoomId(Long value) {
        this.value = value;
    }

    /**
     * 하나의 매개변수를 받아 타입 변환 (데이터베이스에서 조회된 값용)
     */
    public static ChatRoomId from(Long value) {
        return new ChatRoomId(value);
    }
    
    /**
     * 항상 새로운 인스턴스를 생성해 반환 (신규 생성용)
     */
    public static ChatRoomId create() {
        return new ChatRoomId(null);
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