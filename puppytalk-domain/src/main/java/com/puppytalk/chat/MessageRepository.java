package com.puppytalk.chat;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    
    /**
     * 메시지 저장
     */
    void save(Message message);
    
    /**
     * ID로 메시지 조회
     */
    Optional<Message> findById(MessageId id);
    
    /**
     * 채팅방의 모든 메시지 조회 (시간순 정렬)
     */
    List<Message> findByChatRoomIdOrderByCreatedAt(ChatRoomId chatRoomId);
    
    /**
     * 채팅방의 최근 메시지 조회 (페이징)
     * @deprecated 커서 기반 페이징으로 대체됨
     */
    @Deprecated
    List<Message> findByChatRoomIdOrderByCreatedAtDesc(ChatRoomId chatRoomId, int limit);
    
    /**
     * 채팅방의 메시지 조회 (커서 기반 페이징)
     * 커서(messageId) 이후의 메시지들을 시간 오름차순으로 조회
     * 
     * @param chatRoomId 채팅방 ID
     * @param cursor 커서 (이전 조회의 마지막 메시지 ID), null이면 첫 페이지
     * @param size 조회할 메시지 개수
     * @return 메시지 목록 (오래된 순서부터)
     */
    List<Message> findByChatRoomId(ChatRoomId chatRoomId, MessageId cursor, int size);
    
    /**
     * 채팅방의 마지막 메시지 조회
     */
    Optional<Message> findLatestByChatRoomId(ChatRoomId chatRoomId);
    
    /**
     * 채팅방의 메시지 개수 조회
     */
    long countByChatRoomId(ChatRoomId chatRoomId);
    
    /**
     * 채팅방의 특정 타입 메시지 개수 조회
     */
    long countByChatRoomIdAndType(ChatRoomId chatRoomId, MessageType type);
    
    /**
     * 메시지 존재 여부 확인
     */
    boolean existsById(MessageId id);
    
    /**
     * AI 메시지 생성을 위한 최근 메시지 조회
     * 
     * @param chatRoomId 채팅방 ID
     * @param limit 조회할 메시지 개수 (최신 메시지부터)
     * @return 최신 메시지부터 정렬된 리스트
     */
    List<Message> findRecentMessages(ChatRoomId chatRoomId, int limit);
}