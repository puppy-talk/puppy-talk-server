package com.puppytalk.notification;

/**
 * 알림 발송 서비스 인터페이스
 * 
 * FCM, SMS, Email 등 다양한 알림 채널을 추상화
 */
public interface NotificationService {
    
    /**
     * 푸시 알림 발송
     * 
     * @param userId 사용자 ID
     * @param title 알림 제목
     * @param message 알림 내용
     * @param notificationId 알림 ID (추적용)
     * @return 발송 성공 여부
     */
    boolean sendPushNotification(Long userId, String title, String message, Long notificationId);
    
    /**
     * 서비스 상태 확인
     * 
     * @return 서비스 사용 가능 여부
     */
    boolean isAvailable();
    
    /**
     * 서비스 타입 반환
     * 
     * @return 서비스 타입 (FCM, SMS, EMAIL 등)
     */
    String getServiceType();
}