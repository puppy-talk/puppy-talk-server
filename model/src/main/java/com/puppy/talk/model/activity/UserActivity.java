package com.puppy.talk.model.activity;

import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.user.UserIdentity;

import java.time.LocalDateTime;

/**
 * 사용자 활동 도메인 모델
 */
public record UserActivity(
    UserActivityIdentity identity,
    UserIdentity userId,
    ChatRoomIdentity chatRoomId,
    ActivityType activityType,
    LocalDateTime activityAt,
    LocalDateTime createdAt
) {
    
    public UserActivity {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        if (activityType == null) {
            throw new IllegalArgumentException("ActivityType cannot be null");
        }
        if (activityAt == null) {
            throw new IllegalArgumentException("ActivityAt cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
    }
    
    /**
     * 새로운 사용자 활동을 생성합니다.
     */
    public static UserActivity of(
        UserIdentity userId,
        ChatRoomIdentity chatRoomId,
        ActivityType activityType,
        LocalDateTime activityAt
    ) {
        return new UserActivity(
            null, // identity는 저장 시 생성됨
            userId,
            chatRoomId,
            activityType,
            activityAt,
            LocalDateTime.now()
        );
    }
    
    /**
     * 식별자를 포함한 새로운 UserActivity를 생성합니다.
     */
    public UserActivity withIdentity(UserActivityIdentity identity) {
        return new UserActivity(
            identity,
            this.userId,
            this.chatRoomId,
            this.activityType,
            this.activityAt,
            this.createdAt
        );
    }
}