package com.puppytalk.chat.dto.request;

import org.springframework.util.Assert;

/**
 * 채팅방 목록 조회 쿼리
 */
public record ChatRoomListQuery(
    Long userId
) {
    public ChatRoomListQuery {
        Assert.notNull(userId, "UserId cannot be null");
    }
    
    /**
     * 채팅방 목록 조회 쿼리 정적 팩토리 메서드
     */
    public static ChatRoomListQuery of(Long userId) {
        return new ChatRoomListQuery(userId);
    }
}