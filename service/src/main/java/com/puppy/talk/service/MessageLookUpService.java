package com.puppy.talk.service;

import com.puppy.talk.exception.MessageNotFoundException;
import com.puppy.talk.infrastructure.MessageRepository;
import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.chat.Message;
import com.puppy.talk.model.chat.MessageIdentity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageLookUpService {

    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public Message findMessage(MessageIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        return messageRepository.findByIdentity(identity)
            .orElseThrow(() -> new MessageNotFoundException(identity));
    }

    @Transactional(readOnly = true)
    public List<Message> findMessagesByChatRoomId(ChatRoomIdentity chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        return messageRepository.findByChatRoomId(chatRoomId);
    }

    @Transactional(readOnly = true)
    public List<Message> findMessagesByChatRoomIdOrderByCreatedAtDesc(ChatRoomIdentity chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        return messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
    }

    @Transactional(readOnly = true)
    public List<Message> findUnreadMessagesByChatRoomId(ChatRoomIdentity chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        return messageRepository.findUnreadMessagesByChatRoomId(chatRoomId);
    }

    @Transactional
    public Message sendMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        return messageRepository.save(message);
    }

    @Transactional
    public void markMessageAsRead(MessageIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        if (!messageRepository.findByIdentity(identity).isPresent()) {
            throw new MessageNotFoundException(identity);
        }
        messageRepository.markAsRead(identity);
    }

    @Transactional
    public void markAllMessagesAsReadByChatRoomId(ChatRoomIdentity chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        messageRepository.markAllAsReadByChatRoomId(chatRoomId);
    }

    @Transactional
    public void deleteMessage(MessageIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        if (!messageRepository.findByIdentity(identity).isPresent()) {
            throw new MessageNotFoundException(identity);
        }
        messageRepository.deleteByIdentity(identity);
    }
}