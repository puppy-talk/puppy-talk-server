package com.puppy.talk.exception.push;

/**
 * 푸시 알림 처리 중 발생하는 예외
 */
public class PushNotificationException extends RuntimeException {
    
    public PushNotificationException(String message) {
        super(message);
    }
    
    public PushNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}