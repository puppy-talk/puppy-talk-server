package com.puppy.talk.event;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.MessageIdentity;
import com.puppy.talk.chat.SenderType;
import com.puppy.talk.user.UserIdentity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 메시지 전송 이벤트
 * 
 * 메시지가 전송되었을 때 발생하는 도메인 이벤트입니다.
 * 이 이벤트를 통해 활동 추적, 푸시 알림 등의 부가 작업을 분리할 수 있습니다.
 */
public record MessageSentEvent(
    String eventId,
    LocalDateTime occurredOn,
    MessageIdentity messageId,
    ChatRoomIdentity chatRoomId,
    UserIdentity userId,
    SenderType senderType,
    String content,
    boolean isRead
) implements DomainEvent {

    public MessageSentEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredOn == null) {
            occurredOn = LocalDateTime.now();
        }
        if (messageId == null) {
            throw new IllegalArgumentException("MessageId cannot be null");
        }
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (senderType == null) {
            throw new IllegalArgumentException("SenderType cannot be null");
        }
    }

    /**
     * 새로운 메시지 전송 이벤트를 생성합니다.
     */
    public static MessageSentEvent of(
        MessageIdentity messageId,
        ChatRoomIdentity chatRoomId,
        UserIdentity userId,
        SenderType senderType,
        String content,
        boolean isRead
    ) {
        return new MessageSentEvent(
            null, // eventId will be generated
            null, // occurredOn will be set to now
            messageId,
            chatRoomId,
            userId,
            senderType,
            content,
            isRead
        );
    }

    /**
     * 사용자가 보낸 메시지인지 확인합니다.
     */
    public boolean isUserMessage() {
        return senderType == SenderType.USER;
    }

    /**
     * 펫이 보낸 메시지인지 확인합니다.
     */
    public boolean isPetMessage() {
        return senderType == SenderType.PET;
    }
}