package com.puppy.talk.model.activity;

/**
 * 비활성 알림 식별자
 */
public record InactivityNotificationIdentity(Long id) {
    
    public InactivityNotificationIdentity {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("InactivityNotification identity must be positive");
        }
    }
    
    public static InactivityNotificationIdentity of(Long id) {
        return new InactivityNotificationIdentity(id);
    }
}