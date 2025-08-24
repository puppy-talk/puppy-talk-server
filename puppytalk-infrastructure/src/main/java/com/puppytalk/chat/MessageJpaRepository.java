package com.puppytalk.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, Long> {
    
    /**
     * 채팅방의 모든 메시지 조회 (시간순 정렬)
     */
    List<MessageJpaEntity> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
    
    /**
     * 채팅방의 최근 메시지 조회 (페이징)
     * @deprecated 커서 기반 페이징으로 대체됨
     */
    @Deprecated
    @Query("SELECT m FROM MessageJpaEntity m WHERE m.chatRoomId = :chatRoomId ORDER BY m.createdAt DESC LIMIT :limit")
    List<MessageJpaEntity> findByChatRoomIdOrderByCreatedAtDescWithLimit(@Param("chatRoomId") Long chatRoomId, 
                                                                        @Param("limit") int limit);
    
    /**
     * 채팅방의 메시지 조회 (커서 기반 페이징)
     * 커서 이후의 메시지들을 ID 오름차순으로 조회 (시간순 보장)
     * 다음 페이지 존재 여부 확인을 위해 +1개 조회
     */
    @Query("SELECT m FROM MessageJpaEntity m WHERE m.chatRoomId = :chatRoomId " +
           "AND (:cursor IS NULL OR m.id > :cursor) " +
           "ORDER BY m.id ASC LIMIT :size")
    List<MessageJpaEntity> findByChatRoomIdWithCursor(@Param("chatRoomId") Long chatRoomId,
                                                     @Param("cursor") Long cursor,
                                                     @Param("size") int size);
    
    /**
     * 채팅방의 마지막 메시지 조회 (인덱스 최적화: id DESC)
     */
    @Query("SELECT m FROM MessageJpaEntity m WHERE m.chatRoomId = :chatRoomId ORDER BY m.id DESC LIMIT 1")
    Optional<MessageJpaEntity> findLatestByChatRoomId(@Param("chatRoomId") Long chatRoomId);
    
    /**
     * 채팅방의 메시지 개수 조회
     */
    long countByChatRoomId(Long chatRoomId);
    
    /**
     * 채팅방의 특정 타입 메시지 개수 조회
     */
    long countByChatRoomIdAndType(Long chatRoomId, MessageType type);
    
    /**
     * AI 메시지 생성을 위한 최근 메시지 조회 (최신순)
     */
    @Query("SELECT m FROM MessageJpaEntity m WHERE m.chatRoomId = :chatRoomId ORDER BY m.createdAt DESC LIMIT :limit")
    List<MessageJpaEntity> findByChatRoomIdOrderByCreatedAtDesc(@Param("chatRoomId") Long chatRoomId, 
                                                               @Param("limit") int limit);
    
    /**
     * 특정 시간 이후의 새로운 메시지 조회 (폴링용)
     */
    @Query("SELECT m FROM MessageJpaEntity m WHERE m.chatRoomId = :chatRoomId AND m.createdAt > :since ORDER BY m.createdAt ASC")
    List<MessageJpaEntity> findByChatRoomIdAndCreatedAtAfter(@Param("chatRoomId") Long chatRoomId,
                                                           @Param("since") java.time.LocalDateTime since);
}