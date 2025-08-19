package com.puppy.talk.chat;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageLookUpServiceImpl implements MessageLookUpService {

    private final MessageRepository messageRepository;

    @Override
    @Transactional(readOnly = true)
    public Message findMessage(MessageIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        return messageRepository.findByIdentity(identity)
            .orElseThrow(() -> new MessageNotFoundException(identity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findMessagesByChatRoomId(ChatRoomIdentity chatRoomId) {
        validateChatRoomId(chatRoomId);
        return messageRepository.findByChatRoomId(chatRoomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findMessagesByChatRoomIdOrderByCreatedAtDesc(ChatRoomIdentity chatRoomId) {
        validateChatRoomId(chatRoomId);
        return messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findUnreadMessagesByChatRoomId(ChatRoomIdentity chatRoomId) {
        validateChatRoomId(chatRoomId);
        return messageRepository.findUnreadMessagesByChatRoomId(chatRoomId);
    }
    
    /**
     * ChatRoomId 유효성을 검증합니다.
     */
    private void validateChatRoomId(ChatRoomIdentity chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
    }

    @Override
    @Transactional
    public Message sendMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        return messageRepository.save(message);
    }

    @Override
    @Transactional
    public void markMessageAsRead(MessageIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        
        messageRepository.findByIdentity(identity)
            .ifPresentOrElse(
                message -> messageRepository.markAsRead(identity),
                () -> { throw new MessageNotFoundException(identity); }
            );
    }

    @Override
    @Transactional
    public void markAllMessagesAsReadByChatRoomId(ChatRoomIdentity chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        messageRepository.markAllAsReadByChatRoomId(chatRoomId);
    }

    @Override
    @Transactional
    public void deleteMessage(MessageIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        
        messageRepository.findByIdentity(identity)
            .ifPresentOrElse(
                message -> messageRepository.deleteByIdentity(identity),
                () -> { throw new MessageNotFoundException(identity); }
            );
    }
}
