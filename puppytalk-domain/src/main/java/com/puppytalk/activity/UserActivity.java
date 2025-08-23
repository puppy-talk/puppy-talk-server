package com.puppytalk.activity;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.user.UserId;

import java.time.LocalDateTime;

/**
 * 사용자 활동 도메인 모델
 * 
 * Backend 관점: 성능 최적화를 위한 불변 설계
 */
public record UserActivity(
    ActivityId id,
    UserId userId,
    ChatRoomId chatRoomId,
    ActivityType activityType,
    LocalDateTime activityAt,
    LocalDateTime createdAt
) {
    
    public UserActivity {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        if (activityType == null) {
            throw new IllegalArgumentException("ActivityType must not be null");
        }
        if (activityAt == null) {
            throw new IllegalArgumentException("ActivityAt must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt must not be null");
        }
        // chatRoomId는 LOGIN/LOGOUT 활동에서는 null일 수 있음
    }
    
    /**
     * 새로운 사용자 활동 생성 (정적 팩토리 메서드)
     */
    public static UserActivity createActivity(
        UserId userId,
        ChatRoomId chatRoomId,
        ActivityType activityType,
        LocalDateTime activityAt
    ) {
        return new UserActivity(
            null, // ID는 저장 시 생성됨
            userId,
            chatRoomId,
            activityType,
            activityAt,
            LocalDateTime.now()
        );
    }
    
    /**
     * 채팅방 없는 활동 생성 (LOGIN/LOGOUT)
     */
    public static UserActivity createGlobalActivity(
        UserId userId,
        ActivityType activityType,
        LocalDateTime activityAt
    ) {
        if (!activityType.equals(ActivityType.LOGIN) && !activityType.equals(ActivityType.LOGOUT)) {
            throw new IllegalArgumentException("Global activity is only allowed for LOGIN/LOGOUT");
        }
        
        return new UserActivity(
            null,
            userId,
            null, // 전역 활동은 채팅방 없음
            activityType,
            activityAt,
            LocalDateTime.now()
        );
    }
    
    /**
     * ID를 포함한 새로운 UserActivity 생성 (Repository에서 사용)
     */
    public UserActivity withId(ActivityId id) {
        return new UserActivity(
            id,
            this.userId,
            this.chatRoomId,
            this.activityType,
            this.activityAt,
            this.createdAt
        );
    }
    
    /**
     * 중요 활동인지 판단 (비활성 사용자 감지에 사용)
     */
    public boolean isCriticalActivity() {
        return activityType.isCriticalActivity();
    }
    
    /**
     * 지정된 시간 이후의 활동인지 확인
     */
    public boolean isAfter(LocalDateTime dateTime) {
        return activityAt.isAfter(dateTime);
    }
    
    /**
     * 지정된 시간 이전의 활동인지 확인  
     */
    public boolean isBefore(LocalDateTime dateTime) {
        return activityAt.isBefore(dateTime);
    }
}