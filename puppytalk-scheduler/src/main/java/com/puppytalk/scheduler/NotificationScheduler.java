package com.puppytalk.scheduler;

import com.puppytalk.notification.InactivityNotificationFacade;
import com.puppytalk.notification.NotificationFacade;
import com.puppytalk.notification.dto.request.NotificationStatusUpdateCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);
    private static final int INTERVAL_30_MINUTES = 1800000;
    private static final int INTERVAL_15_MINUTES = 900000;
    private static final int INTERVAL_5_MINUTES = 300000;

    private final NotificationFacade notificationFacade;
    private final InactivityNotificationFacade inactivityNotificationFacade;
    
    public NotificationScheduler(
            NotificationFacade notificationFacade,
            InactivityNotificationFacade inactivityNotificationFacade) {
        this.notificationFacade = notificationFacade;
        this.inactivityNotificationFacade = inactivityNotificationFacade;
    }
    
    /**
     * 비활성 사용자 감지 및 알림 생성 (30분마다)
     */
    @Scheduled(fixedRate = INTERVAL_30_MINUTES)
    public void detectInactiveUsersAndCreateNotifications() {
        log.info("Starting inactive user detection and notification creation");

        try {
            List<Long> inactiveUserIds = notificationFacade.findInactiveUsersForNotification();
            
            if (inactiveUserIds.isEmpty()) {
                log.info("No inactive users found for notification");
                return;
            }
            
            log.info("Found {} inactive users for notification", inactiveUserIds.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            for (Long userId : inactiveUserIds) {
                try {
                    // TODO: 실제 구현에서는 AI 서비스를 호출하여 개인화된 메시지를 생성
                    // 현재는 더미 데이터로 구현
                    createInactivityNotificationForUser(userId);
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("Failed to create inactivity notification for user {}: {}", userId, e.getMessage());
                    failureCount++;
                }
            }
            
            log.info("Inactivity notification creation completed. Success: {}, Failure: {}", 
                     successCount, failureCount);
            
        } catch (Exception e) {
            log.error("Error during inactive user detection: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 발송 대기 중인 알림 처리 (5분마다)
     */
    @Scheduled(fixedRate = INTERVAL_5_MINUTES)
    public void processPendingNotifications() {
        log.debug("Processing pending notifications");
        
        try {
            // 발송 대기 중인 알림 조회
            var pendingResult = notificationFacade.getPendingNotifications(100);
            
            if (pendingResult.totalCount() == 0) {
                log.debug("No pending notifications found");
                return;
            }
            
            log.info("Found {} pending notifications for processing", pendingResult.totalCount());
            
            // TODO: 실제 구현에서는 외부 알림 서비스(FCM, SMS 등)로 발송
            // 현재는 로깅으로 처리
            for (var notification : pendingResult.notifications()) {
                try {
                    simulateNotificationSending(notification);
                } catch (Exception e) {
                    log.error("Failed to send notification {}: {}", 
                             notification.notificationId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Error during pending notification processing: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 실패한 알림 재시도 (15분마다)
     */
    @Scheduled(fixedRate = INTERVAL_15_MINUTES)
    public void retryFailedNotifications() {
        log.debug("Processing failed notifications for retry");
        
        try {
            var retryResult = notificationFacade.getRetryableNotifications(50);
            
            if (retryResult.totalCount() == 0) {
                log.debug("No retryable notifications found");
                return;
            }
            
            log.info("Found {} retryable notifications", retryResult.totalCount());
            
            for (var notification : retryResult.notifications()) {
                try {
                    simulateNotificationSending(notification);
                    log.info("Successfully retried notification {}", notification.notificationId());
                } catch (Exception e) {
                    log.error("Retry failed for notification {}: {}", 
                             notification.notificationId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Error during failed notification retry: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 만료된/오래된 알림 정리 (매일 새벽 3시)
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupNotifications() {
        log.info("Starting notification cleanup");
        
        try {
            // 만료된 알림 정리
            int expiredCount = notificationFacade.cleanupExpiredNotifications();
            log.info("Cleaned up {} expired notifications", expiredCount);
            
            // 오래된 완료 알림 정리
            int oldCount = notificationFacade.cleanupOldNotifications();
            log.info("Cleaned up {} old completed notifications", oldCount);
            
            log.info("Notification cleanup completed. Total cleaned: {}", 
                     expiredCount + oldCount);
            
        } catch (Exception e) {
            log.error("Error during notification cleanup: {}", e.getMessage(), e);
        }
    }
    
    // === Private Helper Methods ===
    
    /**
     * AI 기반 개인화 비활성 알림 생성
     * 
     * Application 계층의 Facade를 통해 다음 단계를 수행:
     * 1. 사용자의 반려동물 정보 조회
     * 2. 채팅방 정보 조회  
     * 3. 이전 대화 내역 분석
     * 4. 반려동물 페르소나 기반 AI 메시지 생성
     * 5. 개인화된 알림 생성
     */
    private void createInactivityNotificationForUser(Long userId) {
        try {
            // 사용자의 활성 반려동물 조회 (첫 번째 반려동물 선택)
            Long petId = notificationFacade.findFirstActivePetByUserId(userId);
            
            if (petId == null) {
                log.warn("No active pets found for user {}. Skipping inactivity notification.", userId);
                return;
            }
            
            log.debug("Creating AI-powered inactivity notification for user {} with pet {}", 
                     userId, petId);
            
            // ✅ InactivityNotificationFacade를 통한 AI 기반 개인화 알림 생성
            Long notificationId = inactivityNotificationFacade.createInactivityNotification(userId, petId);
            
            if (notificationId != null) {
                log.info("Successfully created AI-powered inactivity notification (ID: {}) for user {} with pet {}", 
                        notificationId, userId, petId);
            } else {
                log.warn("Failed to create inactivity notification for user {} with pet {}", 
                        userId, petId);
                throw new RuntimeException("Failed to create inactivity notification");
            }
            
        } catch (Exception e) {
            log.error("Error creating AI-powered inactivity notification for user {}: {}", 
                     userId, e.getMessage(), e);
            throw e; // 상위 예외 처리 로직으로 전파
        }
    }
    
    private void simulateNotificationSending(
        com.puppytalk.notification.dto.response.NotificationResult notification
    ) {
        // TODO: 실제 구현에서는 외부 알림 서비스 호출
        // - FCM (Firebase Cloud Messaging) for push notifications
        // - SMS 게이트웨이 for text messages  
        // - Email 서비스 for email notifications
        // - WebSocket for real-time notifications
        
        log.info("Simulating notification send: ID={}, Title={}", 
                 notification.notificationId(), notification.title());
        
        // 성공으로 가정하고 상태 업데이트
        var statusCommand = NotificationStatusUpdateCommand.sent(notification.notificationId());
        
        notificationFacade.updateNotificationStatus(statusCommand);
    }
}