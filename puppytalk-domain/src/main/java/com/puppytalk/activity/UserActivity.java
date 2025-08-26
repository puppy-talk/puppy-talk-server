package com.puppytalk.activity;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.user.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserActivity {
    private final ActivityId id;
    private final UserId userId;
    private final ChatRoomId chatRoomId;
    private final ActivityType activityType;
    private final LocalDateTime activityAt;
    private final LocalDateTime createdAt;

    private UserActivity(ActivityId id, UserId userId, ChatRoomId chatRoomId,
                        ActivityType activityType, LocalDateTime activityAt, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.chatRoomId = chatRoomId;
        this.activityType = activityType;
        this.activityAt = activityAt;
        this.createdAt = createdAt;
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
        validateUserId(userId);
        validateActivityType(activityType);
        validateActivityAt(activityAt);
        
        return new UserActivity(
            null,
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
        validateUserId(userId);
        validateActivityType(activityType);
        validateActivityAt(activityAt);
        
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
     * 기존 사용자 활동 복원용 정적 팩토리 메서드 (Repository용)
     */
    public static UserActivity of(ActivityId id, UserId userId, ChatRoomId chatRoomId,
                                 ActivityType activityType, LocalDateTime activityAt, LocalDateTime createdAt) {
        if (id == null || !id.isValid()) {
            throw new IllegalArgumentException("저장된 활동 ID가 필요합니다");
        }
        validateUserId(userId);
        validateActivityType(activityType);
        validateActivityAt(activityAt);
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt must not be null");
        }

        return new UserActivity(id, userId, chatRoomId, activityType, activityAt, createdAt);
    }
    
    private static void validateUserId(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
    }
    
    private static void validateActivityType(ActivityType activityType) {
        if (activityType == null) {
            throw new IllegalArgumentException("ActivityType must not be null");
        }
    }
    
    private static void validateActivityAt(LocalDateTime activityAt) {
        if (activityAt == null) {
            throw new IllegalArgumentException("ActivityAt must not be null");
        }
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

    // getter
    public ActivityId id() { return id; }
    public UserId userId() { return userId; }
    public ChatRoomId chatRoomId() { return chatRoomId; }
    public ActivityType activityType() { return activityType; }
    public LocalDateTime activityAt() { return activityAt; }
    public LocalDateTime createdAt() { return createdAt; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UserActivity other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "UserActivity{" +
                "id=" + id +
                ", userId=" + userId +
                ", activityType=" + activityType +
                ", activityAt=" + activityAt +
                '}';
    }
}