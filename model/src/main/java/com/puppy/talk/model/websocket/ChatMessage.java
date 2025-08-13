package com.puppy.talk.model.websocket;

import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.chat.MessageIdentity;
import com.puppy.talk.model.chat.SenderType;
import com.puppy.talk.model.user.UserIdentity;

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
        return of(messageId, chatRoomId, userId, senderType, content, isRead, ChatMessageType.MESSAGE);
    }
    
    public static ChatMessage typing(
        ChatRoomIdentity chatRoomId,
        UserIdentity userId,
        SenderType senderType
    ) {
        return of(null, chatRoomId, userId, senderType, null, false, ChatMessageType.TYPING);
    }
    
    public static ChatMessage stopTyping(
        ChatRoomIdentity chatRoomId,
        UserIdentity userId,
        SenderType senderType
    ) {
        return of(null, chatRoomId, userId, senderType, null, false, ChatMessageType.STOP_TYPING);
    }
    
    public static ChatMessage readReceipt(
        ChatRoomIdentity chatRoomId,
        UserIdentity userId,
        MessageIdentity lastReadMessageId
    ) {
        return new ChatMessage(
            lastReadMessageId,
            chatRoomId,
            userId,
            SenderType.USER,
            null,
            true,
            LocalDateTime.now(),
            ChatMessageType.READ_RECEIPT
        );
    }
}