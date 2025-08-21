package com.puppy.talk.notification;

/**
 * 실시간 알림 전송 실패 시 발생하는 예외
 */
public class RealtimeNotificationException extends RuntimeException {
    
    public RealtimeNotificationException(String message) {
        super(message);
    }
    
    public RealtimeNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RealtimeNotificationException(Throwable cause) {
        super(cause);
    }
}