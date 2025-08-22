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
        // cursor는 null일 수 있음 (첫 페이지)
        // size는 null일 수 있음 (기본 사이즈 사용)
    }
    
    /**
     * 메시지 목록 조회 쿼리 정적 팩토리 메서드
     */
    public static MessageListQuery of(Long chatRoomId, Long userId, Long cursor, Integer size) {
        return new MessageListQuery(chatRoomId, userId, cursor, size);
    }
    
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
     * 모든 메시지를 조회하는 쿼리 생성 (무제한)
     */
    public static MessageListQuery allMessages(Long chatRoomId, Long userId) {
        return new MessageListQuery(chatRoomId, userId, null, -1);
    }
    
    /**
     * 첫 페이지인지 확인
     */
    public boolean isFirstPage() {
        return cursor == null;
    }
    
    /**
     * 크기 제한 없이 모든 메시지를 조회하는지 확인
     * size가 -1인 경우 모든 메시지 조회로 처리
     */
    public boolean isUnlimited() {
        return size != null && size == -1;
    }
    
    /**
     * 실제 페이지 크기 반환
     */
    public int getEffectiveSize() {
        if (size == null) {
            return 20; // 기본 20개
        }
        if (size == -1) {
            return Integer.MAX_VALUE; // 제한 없음
        }
        return size;
    }
}