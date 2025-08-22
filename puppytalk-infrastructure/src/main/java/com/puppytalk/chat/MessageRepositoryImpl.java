package com.puppytalk.chat;

import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class MessageRepositoryImpl implements MessageRepository {
    
    private final MessageJpaRepository jpaRepository;
    
    public MessageRepositoryImpl(MessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public void save(Message message) {
        MessageJpaEntity entity = toJpaEntity(message);
        MessageJpaEntity savedEntity = jpaRepository.save(entity);
        
        // ID가 새로 생성된 경우 도메인 객체에 반영 (필요시 별도 처리)
        // 현재 도메인 객체는 불변이므로 저장된 ID를 반환하는 방식으로 처리할 수도 있음
    }
    
    @Override
    public Optional<Message> findById(MessageId id) {
        if (id == null || !id.isStored()) {
            return Optional.empty();
        }
        
        return jpaRepository.findById(id.getValue())
                .map(this::toDomainEntity);
    }
    
    @Override
    public List<Message> findByChatRoomIdOrderByCreatedAt(ChatRoomId chatRoomId) {
        // 도메인 서비스에서 이미 검증된 파라미터로 방어적 검사 최소화
        
        return jpaRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId.getValue())
                .stream()
                .map(this::toDomainEntity)
                .toList();
    }
    
    @Override
    @Deprecated
    public List<Message> findByChatRoomIdOrderByCreatedAtDesc(ChatRoomId chatRoomId, int limit) {
        if (chatRoomId == null || !chatRoomId.isStored() || limit <= 0) {
            return List.of();
        }
        
        return jpaRepository.findByChatRoomIdOrderByCreatedAtDescWithLimit(chatRoomId.getValue(), limit)
                .stream()
                .map(this::toDomainEntity)
                .toList();
    }
    
    @Override
    public List<Message> findByChatRoomId(ChatRoomId chatRoomId, MessageId cursor, int size) {
        // 도메인 서비스에서 이미 검증된 파라미터로 방어적 검사 최소화
        Long cursorValue = (cursor != null && cursor.isStored()) ? cursor.getValue() : null;
        
        return jpaRepository.findByChatRoomIdWithCursor(chatRoomId.getValue(), cursorValue, size)
                .stream()
                .map(this::toDomainEntity)
                .toList();
    }
    
    @Override
    public Optional<Message> findLatestByChatRoomId(ChatRoomId chatRoomId) {
        // 도메인 서비스에서 이미 검증된 파라미터로 방어적 검사 최소화
        
        return jpaRepository.findLatestByChatRoomId(chatRoomId.getValue())
                .map(this::toDomainEntity);
    }
    
    @Override
    public long countByChatRoomId(ChatRoomId chatRoomId) {
        if (chatRoomId == null || !chatRoomId.isStored()) {
            return 0;
        }
        
        return jpaRepository.countByChatRoomId(chatRoomId.getValue());
    }
    
    @Override
    public long countByChatRoomIdAndType(ChatRoomId chatRoomId, MessageType type) {
        if (chatRoomId == null || !chatRoomId.isStored() || type == null) {
            return 0;
        }
        
        return jpaRepository.countByChatRoomIdAndType(chatRoomId.getValue(), type);
    }
    
    @Override
    public boolean existsById(MessageId id) {
        if (id == null || !id.isStored()) {
            return false;
        }
        
        return jpaRepository.existsById(id.getValue());
    }
    
    private MessageJpaEntity toJpaEntity(Message message) {
        MessageJpaEntity entity = new MessageJpaEntity(
            message.getChatRoomId().getValue(),
            message.getType(),
            message.getContent()
        );
        
        // ID가 있는 경우 설정 (업데이트)
        if (message.getId().isStored()) {
            entity.setId(message.getId().getValue());
        }
        
        return entity;
    }
    
    private Message toDomainEntity(MessageJpaEntity entity) {
        return Message.restore(
            MessageId.from(entity.getId()),
            ChatRoomId.from(entity.getChatRoomId()),
            entity.getType(),
            entity.getContent(),
            entity.getCreatedAt()
        );
    }
}