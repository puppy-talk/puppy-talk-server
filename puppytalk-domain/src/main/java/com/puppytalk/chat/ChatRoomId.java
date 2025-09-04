package com.puppytalk.chat;

/**
 * 채팅방 ID를 나타내는 값 객체
 */
public record ChatRoomId(Long value) {

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
}