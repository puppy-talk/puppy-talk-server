package com.puppytalk.notification;

public class NotificationException extends RuntimeException {
    
    public NotificationException(String message) {
        super(message);
    }
    
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static NotificationException creationFailed(String reason) {
        return new NotificationException("알림 생성에 실패했습니다: " + reason);
    }
    
    public static NotificationException sendingFailed(String reason) {
        return new NotificationException("알림 전송에 실패했습니다: " + reason);
    }
    
    public static NotificationException notFound(NotificationId id) {
        return new NotificationException("알림을 찾을 수 없습니다: " + id.getValue());
    }
    
    public static NotificationException dailyLimitExceeded() {
        return new NotificationException("일일 알림 발송 제한을 초과했습니다");
    }
}