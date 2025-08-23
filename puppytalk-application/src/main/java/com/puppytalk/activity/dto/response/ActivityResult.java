package com.puppytalk.activity.dto.response;

import com.puppytalk.activity.UserActivity;

import java.time.LocalDateTime;

/**
 * 활동 조회 결과
 */
public record ActivityResult(
    Long activityId,
    Long userId,
    Long chatRoomId,
    String activityType,
    LocalDateTime activityAt,
    LocalDateTime createdAt,
    boolean found
) {
    
    public static ActivityResult from(UserActivity activity) {
        return new ActivityResult(
            activity.id() != null ? activity.id().getValue() : null,
            activity.userId().getValue(),
            activity.chatRoomId() != null ? activity.chatRoomId().getValue() : null,
            activity.activityType().name(),
            activity.activityAt(),
            activity.createdAt(),
            true
        );
    }
    
    public static ActivityResult created(Long activityId) {
        return new ActivityResult(
            activityId,
            null,
            null,
            null,
            null,
            LocalDateTime.now(),
            true
        );
    }
    
    public static ActivityResult notFound() {
        return new ActivityResult(
            null,
            null,
            null,
            null,
            null,
            null,
            false
        );
    }
}