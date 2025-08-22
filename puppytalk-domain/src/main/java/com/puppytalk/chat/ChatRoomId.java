package com.puppytalk.chat;

import java.util.Objects;

/**
 * 채팅방 식별자
 */
public class ChatRoomId {
    
    private final Long value;
    
    private ChatRoomId(Long value) {
        this.value = value;
    }
    
    /**
     * 채팅방 ID 생성 정적 팩토리 메서드
     */
    public static ChatRoomId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("채팅방 ID는 양수여야 합니다");
        }
        return new ChatRoomId(value);
    }
    
    /**
     * 신규 채팅방용 임시 ID 생성
     */
    public static ChatRoomId newChatRoom() {
        return new ChatRoomId(null);
    }
    
    /**
     * 기존 채팅방 ID 복원 (Repository용)
     */
    public static ChatRoomId from(Long value) {
        return new ChatRoomId(value);
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