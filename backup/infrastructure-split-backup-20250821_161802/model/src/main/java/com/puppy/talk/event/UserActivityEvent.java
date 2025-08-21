package com.puppy.talk.event;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.activity.ActivityType;

import java.time.LocalDateTime;

/**
 * 사용자 활동 이벤트
 * 메시지 송신, 수신, 채팅방 열기 등의 활동 시 발생
 */
public record UserActivityEvent(
    UserIdentity userId,
    ChatRoomIdentity chatRoomId,
    ActivityType activityType,
    LocalDateTime activityTime
) implements DomainEvent {
    
    public static UserActivityEvent of(
        UserIdentity userId,
        ChatRoomIdentity chatRoomId,
        ActivityType activityType
    ) {
        return new UserActivityEvent(
            userId,
            chatRoomId,
            activityType,
            LocalDateTime.now()
        );
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return activityTime;
    }
    
    @Override
    public String eventId() {
        return userId.id() + "-" + chatRoomId.id() + "-" + activityType + "-" + activityTime.toString();
    }
}