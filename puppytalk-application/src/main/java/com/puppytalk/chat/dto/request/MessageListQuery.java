package com.puppytalk.chat.dto.request;

import org.springframework.util.Assert;

/**
 * 메시지 목록 조회 쿼리 (커서 기반 페이징)
 */
public record MessageListQuery(
    Long chatRoomId,
    Long userId,
    Long cursor,
    Integer size
) {
    public MessageListQuery {
        Assert.notNull(chatRoomId, "ChatRoomId cannot be null");
        Assert.notNull(userId, "UserId cannot be null");
    }
    
    /**
     * 메시지 목록 조회 쿼리 정적 팩토리 메서드
     */
    public static MessageListQuery of(Long chatRoomId, Long userId, Long cursor, Integer size) {
        return new MessageListQuery(chatRoomId, userId, cursor, size);
    }

    
    /**
     * 실제 페이지 크기 반환
     */
    public int getSize() {
        return size != null ? size : 10; // 기본 10개
    }
}