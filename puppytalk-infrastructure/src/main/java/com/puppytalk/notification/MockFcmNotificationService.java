package com.puppytalk.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * FCM 알림 서비스 Mock 구현체
 * 
 * 실제 FCM 연동 전까지 사용하는 Mock 구현
 * - 성공/실패를 시뮬레이션
 * - 로깅을 통한 발송 추적
 * - 개발/테스트 환경에서 사용
 */
@Service
@Profile({"local", "test", "docker"})
public class MockFcmNotificationService implements NotificationSender {
    
    private static final Logger log = LoggerFactory.getLogger(MockFcmNotificationService.class);
    
    // Mock을 위한 실패율 시뮬레이션 (10% 실패)
    private static final double FAILURE_RATE = 0.1;
    
    @Override
    public boolean sendPushNotification(Long userId, String title, String content, Long notificationId) {
        log.info("📱 FCM Mock - Sending push notification:");
        log.info("   User: {}", userId);
        log.info("   Title: {}", title);
        log.info("   Message: {}", content);
        log.info("   NotificationId: {}", notificationId);
        
        try {
            // 실제 FCM 호출 시뮬레이션 (대기 시간)
            Thread.sleep(100);
            
            // 10% 확률로 실패 시뮬레이션
            if (Math.random() < FAILURE_RATE) {
                log.warn("❌ FCM Mock - Push notification failed for user: {} (simulated failure)", userId);
                return false;
            }
            
            log.info("✅ FCM Mock - Push notification sent successfully to user: {}", userId);
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ FCM Mock - Push notification interrupted for user: {}", userId);
            return false;
        }
    }
    
    @Override
    public boolean isAvailable() {
        // Mock은 항상 사용 가능
        return true;
    }
    
}
