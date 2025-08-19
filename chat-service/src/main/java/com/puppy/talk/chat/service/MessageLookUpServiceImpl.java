package com.puppy.talk.chat.service.impl;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.Message;
import com.puppy.talk.chat.MessageIdentity;
import com.puppy.talk.chat.service.MessageLookUpService;
import com.puppy.talk.chat.MessageNotFoundException;
import com.puppy.talk.chat.MessageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class MessageLookUpServiceImpl implements MessageLookUpService {

    private final MessageRepository messageRepository;

    @Override
    @Transactional(readOnly = true)
    public Message findMessage(MessageIdentity identity) {
        Assert.notNull(identity, "Identity cannot be null");
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
        Assert.notNull(chatRoomId, "ChatRoomId cannot be null");
    }

    @Override
    @Transactional
    public Message sendMessage(Message message) {
        Assert.notNull(message, "Message cannot be null");
        return messageRepository.save(message);
    }

    @Override
    @Transactional
    public void markMessageAsRead(MessageIdentity identity) {
        Assert.notNull(identity, "Identity cannot be null");
        
        messageRepository.findByIdentity(identity)
            .ifPresentOrElse(
                message -> messageRepository.markAsRead(identity),
                () -> { throw new MessageNotFoundException(identity); }
            );
    }

    @Override
    @Transactional
    public void markAllMessagesAsReadByChatRoomId(ChatRoomIdentity chatRoomId) {
        Assert.notNull(chatRoomId, "ChatRoomId cannot be null");
        messageRepository.markAllAsReadByChatRoomId(chatRoomId);
    }

    @Override
    @Transactional
    public void deleteMessage(MessageIdentity identity) {
        Assert.notNull(identity, "Identity cannot be null");
        
        messageRepository.findByIdentity(identity)
            .ifPresentOrElse(
                message -> messageRepository.deleteByIdentity(identity),
                () -> { throw new MessageNotFoundException(identity); }
            );
    }
}
