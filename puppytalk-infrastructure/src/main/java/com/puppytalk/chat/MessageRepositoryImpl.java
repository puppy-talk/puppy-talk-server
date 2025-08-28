package com.puppytalk.chat;

import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import java.util.List;
import java.util.Optional;

@Repository
public class MessageRepositoryImpl implements MessageRepository {
    
    private final MessageJpaRepository jpaRepository;
    
    public MessageRepositoryImpl(MessageJpaRepository jpaRepository) {
        if (jpaRepository == null) {
            throw new IllegalArgumentException("MessageJpaRepository must not be null");
        }
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public void save(Message message) {
        Assert.notNull(message, "Message must not be null");
        
        MessageJpaEntity entity;
        
        if (message.id() != null && message.id().isStored()) {
            // 기존 메시지 업데이트
            entity = jpaRepository.findById(message.id().getValue())
                    .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다"));
            
            // ✅ updateFromDomain 패턴 사용
            entity.update(message);
        } else {
            // 새 메시지 생성
            entity = MessageJpaEntity.from(message);
        }
        
        jpaRepository.save(entity);
    }
    
    @Override
    public Optional<Message> findById(MessageId id) {
        Assert.notNull(id, "MessageId must not be null");
        
        if (!id.isStored()) {
            return Optional.empty();
        }
        
        return jpaRepository.findById(id.getValue())
                .map(MessageJpaEntity::toDomain);
    }
    
    @Override
    public List<Message> findByChatRoomIdOrderByCreatedAt(ChatRoomId chatRoomId) {
        Assert.notNull(chatRoomId, "ChatRoomId must not be null");
        
        return jpaRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId.getValue())
                .stream()
                .map(MessageJpaEntity::toDomain)
                .toList();
    }
    
    @Override
    @Deprecated
    public List<Message> findByChatRoomIdOrderByCreatedAtDesc(ChatRoomId chatRoomId, int limit) {
        Assert.notNull(chatRoomId, "ChatRoomId must not be null");
        Assert.isTrue(limit > 0, "Limit must be positive");
        
        if (!chatRoomId.isStored()) {
            return List.of();
        }
        
        return jpaRepository.findByChatRoomIdOrderByCreatedAtDescWithLimit(chatRoomId.getValue(), limit)
                .stream()
                .map(MessageJpaEntity::toDomain)
                .toList();
    }
    
    @Override
    public List<Message> findByChatRoomId(ChatRoomId chatRoomId, MessageId messageId, int size) {
        Assert.notNull(chatRoomId, "ChatRoomId must not be null");
        Assert.isTrue(size > 0, "Size must be positive");

        Long cursor = (messageId != null && messageId.isStored()) ? messageId.getValue() : null;
        
        return jpaRepository.findByChatRoomIdWithCursor(chatRoomId.getValue(), cursor, size)
                .stream()
                .map(MessageJpaEntity::toDomain)
                .toList();
    }
    
    @Override
    public Optional<Message> findLatestByChatRoomId(ChatRoomId chatRoomId) {
        Assert.notNull(chatRoomId, "ChatRoomId must not be null");
        
        return jpaRepository.findLatestByChatRoomId(chatRoomId.getValue())
                .map(MessageJpaEntity::toDomain);
    }
    
    @Override
    public long countByChatRoomId(ChatRoomId chatRoomId) {
        Assert.notNull(chatRoomId, "ChatRoomId must not be null");
        
        if (!chatRoomId.isStored()) {
            return 0;
        }
        
        return jpaRepository.countByChatRoomId(chatRoomId.getValue());
    }
    
    @Override
    public long countByChatRoomIdAndType(ChatRoomId chatRoomId, MessageType type) {
        Assert.notNull(chatRoomId, "ChatRoomId must not be null");
        Assert.notNull(type, "MessageType must not be null");
        
        if (!chatRoomId.isStored()) {
            return 0;
        }
        
        return jpaRepository.countByChatRoomIdAndType(chatRoomId.getValue(), type);
    }
    
    @Override
    public boolean existsById(MessageId id) {
        Assert.notNull(id, "MessageId must not be null");
        
        if (!id.isStored()) {
            return false;
        }
        
        return jpaRepository.existsById(id.getValue());
    }
    
    @Override
    public List<Message> findRecentMessages(ChatRoomId chatRoomId, int limit) {
        Assert.notNull(chatRoomId, "ChatRoomId must not be null");
        Assert.isTrue(limit > 0, "Limit must be positive");
        
        if (!chatRoomId.isStored()) {
            return List.of();
        }
        
        return jpaRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId.getValue(), limit)
                .stream()
                .map(MessageJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Message> findByChatRoomIdAndCreatedAtAfter(ChatRoomId chatRoomId, java.time.LocalDateTime since) {
        Assert.notNull(chatRoomId, "ChatRoomId must not be null");
        Assert.notNull(since, "Since time must not be null");
        
        if (!chatRoomId.isStored()) {
            return List.of();
        }
        
        return jpaRepository.findByChatRoomIdAndCreatedAtAfter(chatRoomId.getValue(), since)
                .stream()
                .map(MessageJpaEntity::toDomain)
                .toList();
    }

}