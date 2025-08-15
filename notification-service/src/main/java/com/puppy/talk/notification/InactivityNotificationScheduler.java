package com.puppy.talk.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 비활성 알림 처리 스케줄러
 * 매분마다 알림 대상이 된 비활성 알림들을 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "inactivity.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class InactivityNotificationScheduler {

    private final InactivityNotificationService inactivityNotificationService;

    /**
     * 매분마다 실행되어 알림 대상 비활성 알림들을 처리합니다.
     * cron 표현식: 매분 0초에 실행
     */
    @Scheduled(cron = "0 * * * * *")
    public void processInactivityNotifications() {
        log.debug("Starting scheduled inactivity notification processing...");
        
        try {
            inactivityNotificationService.processEligibleNotifications();
        } catch (Exception e) {
            log.error("Scheduled inactivity notification processing failed", e);
        }
    }

    /**
     * 5분마다 실행되어 알림 처리 상태를 로깅합니다.
     * cron 표현식: 매 5분의 0초에 실행 (00:00, 00:05, 00:10, ...)
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void logNotificationStatistics() {
        try {
            InactivityNotificationService.NotificationStatistics stats = 
                inactivityNotificationService.getStatistics();
                
            log.info("Inactivity notification statistics - Total: {}, Pending: {}, Sent: {}, Disabled: {}", 
                stats.totalCount(), stats.pendingCount(), stats.sentCount(), stats.disabledCount());
                
        } catch (Exception e) {
            log.error("Failed to log notification statistics", e);
        }
    }
}