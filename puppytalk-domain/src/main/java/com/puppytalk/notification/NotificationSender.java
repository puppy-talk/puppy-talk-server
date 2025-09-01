package com.puppytalk.notification;

/**
 * 알림 발송을 담당하는 포트 인터페이스
 */
public interface NotificationSender {

    /**
     * 푸시 알림 발송
     * 
     * @param userId 사용자 ID
     * @param title 알림 제목
     * @param content 알림 내용
     * @param notificationId 알림 ID
     * @return 발송 성공 여부
     */
    boolean sendPushNotification(Long userId, String title, String content, Long notificationId);

    /**
     * 알림 서비스 가용성 확인
     * 
     * @return 서비스 사용 가능 여부
     */
    boolean isAvailable();
}