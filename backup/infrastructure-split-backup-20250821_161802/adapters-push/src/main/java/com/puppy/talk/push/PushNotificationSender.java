package com.puppy.talk.push;

/**
 * 푸시 알림 전송 인터페이스
 */
public interface PushNotificationSender {
    
    /**
     * 푸시 알림을 전송합니다.
     */
    void send(PushNotification notification);
    
    /**
     * 여러 푸시 알림을 배치로 전송합니다.
     */
    void sendBatch(Iterable<PushNotification> notifications);
    
    /**
     * 푸시 알림 서비스가 사용 가능한지 확인합니다.
     */
    boolean isAvailable();
    
    /**
     * 푸시 알림 서비스의 이름을 반환합니다.
     */
    String getServiceName();
}