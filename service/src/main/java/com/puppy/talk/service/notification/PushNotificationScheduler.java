package com.puppy.talk.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 푸시 알림 스케줄러
 * 대기 중인 푸시 알림들을 주기적으로 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationScheduler {
    
    private final PushNotificationService pushNotificationService;
    
    /**
     * 매분마다 대기 중인 푸시 알림들을 처리합니다.
     */
    @Scheduled(cron = "0 * * * * *")
    public void processPendingNotifications() {
        try {
            log.debug("Starting scheduled push notification processing");
            pushNotificationService.processPendingNotifications();
        } catch (Exception e) {
            log.error("Error occurred during scheduled push notification processing: {}", 
                e.getMessage(), e);
        }
    }
    
    /**
     * 매 30분마다 푸시 알림 통계를 로깅합니다.
     */
    @Scheduled(cron = "0 */30 * * * *")
    public void logStatistics() {
        try {
            PushNotificationService.NotificationStatistics stats = 
                pushNotificationService.getStatistics();
                
            log.info("Push Notification Statistics - Total: {}, Pending: {}, Sent: {}, Failed: {}, Received: {}",
                stats.totalCount(), stats.pendingCount(), stats.sentCount(), 
                stats.failedCount(), stats.receivedCount());
                
        } catch (Exception e) {
            log.error("Error occurred while logging push notification statistics: {}", 
                e.getMessage(), e);
        }
    }
}