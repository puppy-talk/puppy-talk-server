package com.puppytalk.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
public class MessageRepositoryImpl implements MessageRepository {

    private final MessageJpaRepository jpaRepository;

    public MessageRepositoryImpl(MessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Message create(Message message) {
        Assert.notNull(message, "Message must not be null");

        MessageJpaEntity entity = MessageJpaEntity.from(message);
        MessageJpaEntity savedEntity = jpaRepository.save(entity);

        return savedEntity.toDomain();
    }

    @Override
    public Message update(Message message) {
        Assert.notNull(message, "Message must not be null");
        Assert.notNull(message.getId(), "Message ID must not be null for update");
        Assert.isTrue(message.getId().value() != null, "Message must be stored for update");

        MessageJpaEntity entity = jpaRepository.findById(message.getId().value())
            .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다"));

        entity.update(message);
        MessageJpaEntity savedEntity = jpaRepository.save(entity);

        return savedEntity.toDomain();
    }

    @Override
    public Optional<Message> findById(MessageId id) {
        Assert.notNull(id, "MessageId must not be null");

        if (id.value() != null) {
            return jpaRepository.findById(id.value())
                .map(MessageJpaEntity::toDomain);
        }

        return Optional.empty();
    }

    @Override
    public List<Message> findByChatRoomIdWithCursor(ChatRoomId chatRoomId) {
        Assert.notNull(chatRoomId, "ChatRoomId must not be null");

        return jpaRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId.value())
            .stream()
            .map(MessageJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Message> findByChatRoomIdWithCursor(ChatRoomId chatRoomId, MessageId messageId,
        int size) {
        Assert.notNull(chatRoomId, "ChatRoomId must not be null");
        Assert.isTrue(size > 0, "Size must be positive");

        Long cursor = (messageId != null && messageId.value() != null) ? messageId.value() : null;

        return jpaRepository.findByChatRoomIdWithCursor(chatRoomId.value(), cursor, size)
            .stream()
            .map(MessageJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Message> findRecentMessages(ChatRoomId chatRoomId, int limit) {
        Assert.notNull(chatRoomId, "ChatRoomId must not be null");
        Assert.isTrue(limit > 0, "Limit must be positive");

        if (chatRoomId.value() == null) {
            throw new IllegalArgumentException("채팅방 ID 값은 필수입니다");
        }

        return jpaRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId.value(), limit)
            .stream()
            .map(MessageJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Message> findByChatRoomIdAndCreatedAtAfter(ChatRoomId chatRoomId,
        LocalDateTime since) {
        Assert.notNull(chatRoomId, "ChatRoomId must not be null");
        Assert.notNull(since, "Since time must not be null");

        if (chatRoomId.value() == null) {
            throw new IllegalArgumentException("채팅방 ID 값은 필수입니다");
        }
        
        return jpaRepository.findByChatRoomIdAndCreatedAtAfter(chatRoomId.value(), since)
            .stream()
            .map(MessageJpaEntity::toDomain)
            .toList();

    }
}