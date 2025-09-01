package com.puppytalk;

import com.puppytalk.notification.InactivityNotificationFacade;
import com.puppytalk.notification.NotificationFacade;
import com.puppytalk.pet.PetFacade;
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

    public NotificationScheduler(
        NotificationFacade notificationFacade,
        InactivityNotificationFacade inactivityNotificationFacade,
        PetFacade petFacade
    ) {
        this.notificationFacade = notificationFacade;
        this.inactivityNotificationFacade = inactivityNotificationFacade;
        this.petFacade = petFacade;
    }

    /**
     * 비활성 사용자 감지 및 알림 생성 (30분마다) <br> 2시간 동안 접속하지 않은 사용자를 탐색 -> 해당 사용자에게 메시지 전송
     */
    @Scheduled(fixedRate = INTERVAL_30_MINUTES)
    public void detectInactiveUsersAndCreateNotifications() {

        // 1. 2시간 동안 접속하지 않은 사용자 탐색
        List<Long> inactiveUserIds = notificationFacade.findInactiveUsersForNotification();

        if (inactiveUserIds.isEmpty()) {
            log.info("No inactive users found for notification");
            return;
        }

        log.info("Found {} inactive users for notification", inactiveUserIds.size());

        for (Long userId : inactiveUserIds) {
            try {
                // AI 서비스를 호출하여 개인화된 메시지를 생성
                createInactivityNotificationForUser(userId);
            } catch (Exception e) {
                log.error("Failed to create inactivity notification for user {}: {}", userId,
                    e.getMessage());
            }
        }
    }

    /**
     * 발송 대기 중인 알림 처리 (5분마다)
     */
    @Scheduled(fixedRate = INTERVAL_5_MINUTES)
    public void processPendingNotifications() {
        log.debug("Processing pending notifications");

        try {
            // NotificationFacade에 위임하여 처리
            notificationFacade.processPendingNotifications(100);
        } catch (Exception e) {
            log.error("Error during pending notification processing: {}", e.getMessage(), e);
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

    /**
     * AI 기반 개인화 비활성 알림 생성<br> 알림 생성 과정:<br>
     * 1. 사용자의 반려동물 조회<br>
     * 2. 채팅방 조회<br>
     * 3. 이전 대화 내역 분석<br>
     * 4. 반려동물 페르소나 + 이전 대화 내역 기반 AI 메시지 생성<br>
     * 5. 알림 생성<br>
     */
    private void createInactivityNotificationForUser(Long userId) {
        // 1. 사용자의 반려동물 조회
        Long petId = petFacade.findFirstPetByUserId(userId);

        if (petId == null) {
            return;
        }

        // 2. 알림 생성
        inactivityNotificationFacade.createInactivityNotification(userId, petId);
    }

}