package com.puppytalk.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 메시지 Repository 인터페이스
 */
public interface MessageRepository {
    
    /**
     * 메시지 저장
     */
    Message save(Message message);
    
    /**
     * ID로 메시지 조회
     */
    Optional<Message> findById(MessageId messageId);
    
    /**
     * 채팅방의 메시지 목록 조회 (최신순)
     */
    List<Message> findByChatRoomIdOrderBySentAtDesc(ChatRoomId chatRoomId);
    
    /**
     * 채팅방의 메시지 목록 조회 (페이징)
     */
    List<Message> findByChatRoomIdOrderBySentAtDesc(ChatRoomId chatRoomId, int offset, int limit);
    
    /**
     * 채팅방의 최근 메시지 조회
     */
    Optional<Message> findLatestByChatRoomId(ChatRoomId chatRoomId);
    
    /**
     * 특정 타입의 메시지 조회
     */
    List<Message> findByChatRoomIdAndType(ChatRoomId chatRoomId, MessageType type);
    
    /**
     * 특정 시간 이후의 메시지 조회
     */
    List<Message> findByChatRoomIdAndSentAtAfter(ChatRoomId chatRoomId, LocalDateTime after);
    
    /**
     * 채팅방의 메시지 개수 조회
     */
    long countByChatRoomId(ChatRoomId chatRoomId);
    
    /**
     * 메시지 삭제
     */
    void deleteById(MessageId messageId);
    
    /**
     * 채팅방의 모든 메시지 삭제
     */
    void deleteByChatRoomId(ChatRoomId chatRoomId);
}