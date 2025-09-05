package com.puppytalk;

import com.puppytalk.notification.InactivityNotificationFacade;
import com.puppytalk.notification.NotificationFacade;
import com.puppytalk.pet.PetFacade;
import com.puppytalk.scheduler.LogFormats;
import com.puppytalk.user.UserFacade;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);
    private static final int INTERVAL_30_MINUTES = 1800000;
    private static final int INTERVAL_5_MINUTES = 300000;

    private final NotificationFacade notificationFacade;
    private final InactivityNotificationFacade inactivityNotificationFacade;
    private final PetFacade petFacade;
    private final UserFacade userFacade;

    public NotificationScheduler(
        NotificationFacade notificationFacade,
        InactivityNotificationFacade inactivityNotificationFacade,
        PetFacade petFacade,
        UserFacade userFacade
    ) {
        this.notificationFacade = notificationFacade;
        this.inactivityNotificationFacade = inactivityNotificationFacade;
        this.petFacade = petFacade;
        this.userFacade = userFacade;
    }

    /**
     * 비활성 사용자 감지 및 알림 생성 (30분마다) <br> 2시간 동안 접속하지 않은 사용자를 탐색 -> 해당 사용자에게 메시지 전송
     */
    @Scheduled(fixedRate = INTERVAL_30_MINUTES)
    public void detectInactiveUsersAndCreateNotifications() {
        long startTime = System.currentTimeMillis();
        log.info(LogFormats.SCHEDULER_START, "detectInactiveUsersAndCreateNotifications", LocalDateTime.now());

        try {
            // 1. 2시간 동안 접속하지 않은 사용자 탐색
            List<Long> inactiveUserIds = notificationFacade.findInactiveUsersForNotification();
            
            log.info(LogFormats.INACTIVE_USER_DETECTION_START, 2, inactiveUserIds.size());

            if (inactiveUserIds.isEmpty()) {
                long duration = System.currentTimeMillis() - startTime;
                log.info(LogFormats.INACTIVE_USER_DETECTION_COMPLETE, 0, 0, duration);
                return;
            }

            int createdNotifications = 0;
            for (Long userId : inactiveUserIds) {
                try {
                    // AI 서비스를 호출하여 개인화된 메시지를 생성
                    createInactivityNotificationForUser(userId);
                    createdNotifications++;
                } catch (Exception e) {
                    log.error("Failed to create inactivity notification for user {}: {}", userId, e.getMessage(), e);
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info(LogFormats.INACTIVE_USER_DETECTION_COMPLETE, inactiveUserIds.size(), createdNotifications, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error(LogFormats.SCHEDULER_ERROR, "detectInactiveUsersAndCreateNotifications", e.getMessage(), duration, e);
        }
    }

    /**
     * 발송 대기 중인 알림 처리 (5분마다)
     */
    @Scheduled(fixedRate = INTERVAL_5_MINUTES)
    public void processPendingNotifications() {
        long startTime = System.currentTimeMillis();
        log.info(LogFormats.NOTIFICATION_SCHEDULER_START, "processPendingNotifications", 100);

        try {
            // NotificationFacade에 위임하여 처리
            notificationFacade.processPendingNotifications(100);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info(LogFormats.NOTIFICATION_BATCH_PROCESSED, "processPendingNotifications", 100, 0);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error(LogFormats.SCHEDULER_ERROR, "processPendingNotifications", e.getMessage(), duration, e);
        }
    }


    /**
     * 만료된/오래된 알림 정리 (매일 새벽 3시)
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupNotifications() {
        long startTime = System.currentTimeMillis();
        log.info(LogFormats.SCHEDULER_START, "cleanupNotifications", LocalDateTime.now());

        try {
            // 만료된 알림 정리
            int expiredCount = notificationFacade.cleanupExpiredNotifications();

            // 오래된 완료 알림 정리
            int oldCount = notificationFacade.cleanupOldNotifications();

            long duration = System.currentTimeMillis() - startTime;
            log.info(LogFormats.NOTIFICATION_CLEANUP_COMPLETE, expiredCount, oldCount, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error(LogFormats.SCHEDULER_ERROR, "cleanupNotifications", e.getMessage(), duration, e);
        }
    }
    
    /**
     * 휴면 사용자 배치 처리 (매일 새벽 2시)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void processDormantUsers() {
        long startTime = System.currentTimeMillis();
        log.info(LogFormats.SCHEDULER_START, "processDormantUsers", LocalDateTime.now());

        try {
            int processedCount = userFacade.processDormantUsers();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info(LogFormats.SCHEDULER_COMPLETE, "processDormantUsers", duration, processedCount);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error(LogFormats.SCHEDULER_ERROR, "processDormantUsers", e.getMessage(), duration, e);
        }
    }

    /**
     * AI 기반 개인화 비활성 알림 생성<br> 알림 생성 과정:<br>
     * 1. 사용자의 반려동물 조회<br>
     * 2. 채팅방 조회<br>
     * 3. 이전 대화 내역 분석<br>
     * 4. 반려동물 페르소나 + 이전 대화 내역 기반 AI 메시지 생성<br>
     * 5. 알림 생성<br>
     */
    private void createInactivityNotificationForUser(Long userId) {
        try {
            // 1. 사용자의 반려동물 조회
            Long petId = petFacade.findFirstPetByUserId(userId);

            if (petId == null) {
                log.warn("No pet found for user: userId={}", userId);
                return;
            }

            // 2. 알림 생성
            inactivityNotificationFacade.createInactivityNotification(userId, petId);
            log.info(LogFormats.INACTIVE_NOTIFICATION_CREATED, userId, petId, "INACTIVITY");
            
        } catch (Exception e) {
            log.error("Failed to create inactivity notification: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

}