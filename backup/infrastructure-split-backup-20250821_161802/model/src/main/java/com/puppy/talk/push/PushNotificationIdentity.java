package com.puppy.talk.push;

/**
 * 푸시 알림 식별자
 */
public record PushNotificationIdentity(Long id) {
    
    public PushNotificationIdentity {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
    }
    
    public static PushNotificationIdentity of(Long id) {
        return new PushNotificationIdentity(id);
    }
}