package com.puppytalk.chat;

/**
 * 채팅방 식별자 값 객체
 */
public record ChatRoomId(Long value) {
    
    /**
     * ChatRoomId 생성 정적 팩토리 메서드
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
     * 저장된 채팅방인지 확인
     */
    public boolean isStored() {
        return value != null && value > 0;
    }
}