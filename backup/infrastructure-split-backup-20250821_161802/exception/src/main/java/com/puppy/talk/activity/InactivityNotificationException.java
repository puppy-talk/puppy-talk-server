package com.puppy.talk.activity;

/**
 * 비활성 알림 처리 중 발생하는 예외
 */
public class InactivityNotificationException extends RuntimeException {
    
    public InactivityNotificationException(String message) {
        super(message);
    }
    
    public InactivityNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}