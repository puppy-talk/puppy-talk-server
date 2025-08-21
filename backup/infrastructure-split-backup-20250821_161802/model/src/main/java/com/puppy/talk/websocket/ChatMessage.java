package com.puppy.talk.websocket;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.MessageIdentity;
import com.puppy.talk.chat.SenderType;
import com.puppy.talk.user.UserIdentity;

import java.time.LocalDateTime;

/**
 * WebSocket을 통해 전송되는 채팅 메시지
 */
public record ChatMessage(
    MessageIdentity messageId,
    ChatRoomIdentity chatRoomId,
    UserIdentity userId,
    SenderType senderType,
    String content,
    boolean isRead,
    LocalDateTime timestamp,
    ChatMessageType messageType
) {
    
    public static ChatMessage of(
        MessageIdentity messageId,
        ChatRoomIdentity chatRoomId,
        UserIdentity userId,
        SenderType senderType,
        String content,
        boolean isRead,
        ChatMessageType messageType
    ) {
        return new ChatMessage(
            messageId,
            chatRoomId,
            userId,
            senderType,
            content,
            isRead,
            LocalDateTime.now(),
            messageType
        );
    }
    
    public static ChatMessage newMessage(
        MessageIdentity messageId,
        ChatRoomIdentity chatRoomId,
        UserIdentity userId,
        SenderType senderType,
        String content,
        boolean isRead
    ) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content cannot be null or blank for MESSAGE type");
        }
        return of(messageId, chatRoomId, userId, senderType, content, isRead, ChatMessageType.MESSAGE);
    }
    
    public static ChatMessage typing(
        ChatRoomIdentity chatRoomId,
        UserIdentity userId,
        SenderType senderType
    ) {
        if (chatRoomId == null || userId == null || senderType == null) {
            throw new IllegalArgumentException("chatRoomId/userId/senderType cannot be null");
        }
        return of(null, chatRoomId, userId, senderType, null, false, ChatMessageType.TYPING);
    }
    
    public static ChatMessage stopTyping(
        ChatRoomIdentity chatRoomId,
        UserIdentity userId,
        SenderType senderType
    ) {
        if (chatRoomId == null || userId == null || senderType == null) {
            throw new IllegalArgumentException("chatRoomId/userId/senderType cannot be null");
        }
        return of(null, chatRoomId, userId, senderType, null, false, ChatMessageType.STOP_TYPING);
    }
    
    public static ChatMessage readReceipt(
        ChatRoomIdentity chatRoomId,
        UserIdentity userId,
        MessageIdentity lastReadMessageId
    ) {
        if (chatRoomId == null || userId == null || lastReadMessageId == null) {
            throw new IllegalArgumentException("chatRoomId/userId/lastReadMessageId cannot be null");
        }
        return of(lastReadMessageId, chatRoomId, userId, SenderType.USER, null, true, ChatMessageType.READ_RECEIPT);
    }
}