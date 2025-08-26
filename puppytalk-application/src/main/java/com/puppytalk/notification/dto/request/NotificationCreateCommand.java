package com.puppytalk.notification.dto.request;

/**
 * 알림 생성 명령
 */
public record NotificationCreateCommand(
    Long userId,
    Long petId,     // null-able
    Long chatRoomId, // null-able
    String title,
    String content
) {
    
    public NotificationCreateCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("UserId must be positive");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be null or empty");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content must not be null or empty");
        }
    }
    
    /**
     * 비활성 사용자 알림 생성용
     */
    public static NotificationCreateCommand forInactivity(
        Long userId,
        Long petId,
        Long chatRoomId,
        String title,
        String content
    ) {
        return new NotificationCreateCommand(userId, petId, chatRoomId, title, content);
    }
    
    /**
     * 시스템 알림 생성용
     */
    public static NotificationCreateCommand forSystem(
        Long userId,
        String title,
        String content
    ) {
        return new NotificationCreateCommand(userId, null, null, title, content);
    }
}