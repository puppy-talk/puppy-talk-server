package com.puppytalk.notification;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * FCM 알림 서비스 구현체
 */
@Component
@Profile("prod")
public class FcmNotificationService implements NotificationSender {

    @Override
    public boolean sendPushNotification(Long userId, String title, String content, Long notificationId) {
        // TODO: 실제 FCM 발송 구현
        return true; // 임시 구현
    }

    @Override
    public boolean isAvailable() {
        // TODO: 실제 FCM 서비스 가용성 확인
        return true; // 임시 구현
    }
}
