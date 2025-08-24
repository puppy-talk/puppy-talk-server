package com.puppytalk.chat.domain;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.chat.Message;
import com.puppytalk.chat.MessageId;
import com.puppytalk.chat.MessageRepository;
import com.puppytalk.chat.MessageType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 테스트용 MessageRepository Mock 구현체
 */
public class MockMessageRepository implements MessageRepository {

    private final Map<MessageId, Message> messages = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public void save(Message message) {
        if (message.id() == null) {
            // 새로운 ID 생성
            MessageId newId = MessageId.of(idGenerator.getAndIncrement());
            Message savedMessage = Message.restore(
                newId,
                message.chatRoomId(),
                message.type(),
                message.content(),
                message.createdAt()
            );
            messages.put(newId, savedMessage);
        } else {
            // 기존 메시지 업데이트
            messages.put(message.id(), message);
        }
    }

    @Override
    public Optional<Message> findById(MessageId id) {
        return Optional.ofNullable(messages.get(id));
    }

    @Override
    public List<Message> findByChatRoomIdOrderByCreatedAt(ChatRoomId chatRoomId) {
        return messages.values().stream()
            .filter(message -> message.chatRoomId().equals(chatRoomId))
            .sorted(Comparator.comparing(Message::createdAt))
            .toList();
    }

    @Override
    @Deprecated
    public List<Message> findByChatRoomIdOrderByCreatedAtDesc(ChatRoomId chatRoomId, int limit) {
        return messages.values().stream()
            .filter(message -> message.chatRoomId().equals(chatRoomId))
            .sorted(Comparator.comparing(Message::createdAt).reversed())
            .limit(limit)
            .toList();
    }

    @Override
    public List<Message> findByChatRoomId(ChatRoomId chatRoomId, MessageId cursor, int size) {
        List<Message> chatMessages = messages.values().stream()
            .filter(message -> message.chatRoomId().equals(chatRoomId))
            .sorted(Comparator.comparing(message -> message.id().getValue()))
            .toList();

        if (cursor == null) {
            return chatMessages.stream().limit(size).toList();
        }

        return chatMessages.stream()
            .filter(message -> message.id().getValue() > cursor.getValue())
            .limit(size)
            .toList();
    }

    @Override
    public Optional<Message> findLatestByChatRoomId(ChatRoomId chatRoomId) {
        return messages.values().stream()
            .filter(message -> message.chatRoomId().equals(chatRoomId))
            .max(Comparator.comparing(message -> message.id().getValue()));
    }

    @Override
    public long countByChatRoomId(ChatRoomId chatRoomId) {
        return messages.values().stream()
            .filter(message -> message.chatRoomId().equals(chatRoomId))
            .count();
    }

    @Override
    public long countByChatRoomIdAndType(ChatRoomId chatRoomId, MessageType type) {
        return messages.values().stream()
            .filter(message -> message.chatRoomId().equals(chatRoomId) && message.type().equals(type))
            .count();
    }

    @Override
    public boolean existsById(MessageId id) {
        return messages.containsKey(id);
    }

    @Override
    public List<Message> findRecentMessages(ChatRoomId chatRoomId, int limit) {
        return messages.values().stream()
            .filter(message -> message.chatRoomId().equals(chatRoomId))
            .sorted(Comparator.comparing(Message::createdAt).reversed())
            .limit(limit)
            .toList();
    }

    @Override
    public List<Message> findByChatRoomIdAndCreatedAtAfter(ChatRoomId chatRoomId, LocalDateTime since) {
        return messages.values().stream()
            .filter(message -> message.chatRoomId().equals(chatRoomId))
            .filter(message -> message.createdAt().isAfter(since))
            .sorted(Comparator.comparing(Message::createdAt))
            .toList();
    }

    // 테스트용 헬퍼 메서드
    public void addMessage(Message message) {
        if (message.id() != null) {
            messages.put(message.id(), message);
        }
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages.values());
    }

    public void clear() {
        messages.clear();
        idGenerator.set(1);
    }

    public Map<MessageId, Message> getAllMessages() {
        return new HashMap<>(messages);
    }
}