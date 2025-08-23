package com.puppytalk.activity.dto.request;

/**
 * 활동 기록 명령
 */
public record ActivityRecordCommand(
    Long userId,
    Long chatRoomId, // null 가능 (LOGIN/LOGOUT 시)
    String activityType // ActivityType enum name
) {
    
    public ActivityRecordCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("UserId must be positive");
        }
        if (activityType == null || activityType.trim().isEmpty()) {
            throw new IllegalArgumentException("ActivityType must not be null or empty");
        }
    }
    
    public static ActivityRecordCommand of(Long userId, Long chatRoomId, String activityType) {
        return new ActivityRecordCommand(userId, chatRoomId, activityType);
    }
    
    public static ActivityRecordCommand ofGlobal(Long userId, String activityType) {
        return new ActivityRecordCommand(userId, null, activityType);
    }
}