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

    // TODO: 아래 메서드는 호출하는 곳이 없는데, 어디서 사용되는 거지?
    /**
     * 첫 페이지 메시지 조회 쿼리 생성
     */
    public static MessageListQuery firstPage(Long chatRoomId, Long userId, int size) {
        return new MessageListQuery(chatRoomId, userId, null, size);
    }
    
    /**
     * 기본 크기로 첫 페이지 메시지 조회 쿼리 생성
     */
    public static MessageListQuery firstPage(Long chatRoomId, Long userId) {
        return new MessageListQuery(chatRoomId, userId, null, null);
    }
    
    /**
     * 다음 페이지 메시지 조회 쿼리 생성 (커서 기반)
     */
    public static MessageListQuery nextPage(Long chatRoomId, Long userId, Long cursor, int size) {
        return new MessageListQuery(chatRoomId, userId, cursor, size);
    }
    
    /**
     * 첫 페이지인지 확인
     */
    public boolean isFirstPage() {
        return cursor == null;
    }
    
    /**
     * 실제 페이지 크기 반환
     */
    public int getSize() {
        return size != null ? size : 10; // 기본 10개
    }
}