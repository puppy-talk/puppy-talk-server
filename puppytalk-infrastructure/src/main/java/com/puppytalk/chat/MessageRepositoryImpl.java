package com.puppytalk.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepositoryImpl implements MessageRepository {

    private final MessageJpaRepository messageJpaRepository;

    public MessageRepositoryImpl(MessageJpaRepository messageJpaRepository) {
        this.messageJpaRepository = messageJpaRepository;
    }

    @Override
    public Message save(Message message) {
        return null;
    }

    @Override
    public Optional<Message> findById(MessageId messageId) {
        return Optional.empty();
    }

    @Override
    public List<Message> findByChatRoomIdOrderBySentAtDesc(ChatRoomId chatRoomId) {
        return List.of();
    }

    @Override
    public List<Message> findByChatRoomIdOrderBySentAtDesc(ChatRoomId chatRoomId, int offset,
        int limit) {
        return List.of();
    }

    @Override
    public Optional<Message> findLatestByChatRoomId(ChatRoomId chatRoomId) {
        return Optional.empty();
    }

    @Override
    public List<Message> findByChatRoomIdAndType(ChatRoomId chatRoomId, MessageType type) {
        return List.of();
    }

    @Override
    public List<Message> findByChatRoomIdAndSentAtAfter(ChatRoomId chatRoomId,
        LocalDateTime after) {
        return List.of();
    }

    @Override
    public long countByChatRoomId(ChatRoomId chatRoomId) {
        return 0;
    }

    @Override
    public void deleteById(MessageId messageId) {

    }

    @Override
    public void deleteByChatRoomId(ChatRoomId chatRoomId) {

    }
}
