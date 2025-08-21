package com.puppy.talk.notification;

import com.puppy.talk.notification.dto.NotificationStatistics;
import com.puppy.talk.push.DeviceTokenRepository;
import com.puppy.talk.push.PushNotificationIdentity;
import com.puppy.talk.push.PushNotificationRepository;
import com.puppy.talk.push.PushNotificationSender;
import com.puppy.talk.push.DeviceToken;
import com.puppy.talk.push.NotificationType;
import com.puppy.talk.push.PushNotification;
import com.puppy.talk.push.PushNotificationStatus;
import com.puppy.talk.user.UserIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 푸시 알림 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService implements PushNotificationLookUpService {
    
    private final PushNotificationRepository pushNotificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PushNotificationSender pushNotificationSender;
    
    /**
     * 사용자에게 즉시 푸시 알림을 전송합니다.
     */
    @Transactional
    public void sendNotification(
        UserIdentity userId,
        NotificationType notificationType,
        String title,
        String message,
        String data
    ) {
        sendNotification(userId, notificationType, title, message, data, LocalDateTime.now());
    }
    
    /**
     * 사용자에게 예약된 시간에 푸시 알림을 전송합니다.
     */
    @Transactional
    public void sendNotification(
        UserIdentity userId,
        NotificationType notificationType,
        String title,
        String message,
        String data,
        LocalDateTime scheduledAt
    ) {
        validateNotificationInput(userId, notificationType, title, message);
        
        log.debug("Sending push notification to user={}, type={}", userId.id(), notificationType);
        
        List<DeviceToken> activeTokens = getActiveTokensForUser(userId);
        if (activeTokens.isEmpty()) {
            return;
        }
        
        List<PushNotification> notifications = createNotificationsForTokens(
            userId, notificationType, title, message, data, scheduledAt, activeTokens
        );
        
        processImmediateNotifications(notifications, scheduledAt);
        
        log.info("Created {} push notifications for user={}, type={}", 
            notifications.size(), userId.id(), notificationType);
    }
    
    /**
     * 대기 중인 푸시 알림들을 처리합니다.
     * 스케줄러에서 주기적으로 호출됩니다.
     */
    @Transactional
    public void processPendingNotifications() {
        log.debug("Processing pending push notifications...");
        
        List<PushNotification> pendingNotifications = 
            pushNotificationRepository.findPendingNotifications();
            
        if (pendingNotifications.isEmpty()) {
            log.debug("No pending push notifications found");
            return;
        }
        
        log.info("Found {} pending push notifications to process", pendingNotifications.size());
        
        int successCount = processBatchNotifications(pendingNotifications);
        
        log.info("Successfully processed {}/{} push notifications", 
            successCount, pendingNotifications.size());
    }
    
    /**
     * 단일 푸시 알림을 처리합니다.
     */
    private void processSingleNotification(PushNotification notification) {
        log.debug("Processing push notification: {}", notification.identity().id());
        
        try {
            // 푸시 알림 전송
            pushNotificationSender.send(notification);
            
            // 전송 완료로 상태 변경
            PushNotification sentNotification = notification.markAsSent();
            pushNotificationRepository.save(sentNotification);
            
            log.info("Successfully sent push notification: {}", notification.identity().id());
            
        } catch (Exception e) {
            log.error("Failed to send push notification: {} - {}", 
                notification.identity().id(), e.getMessage(), e);
                
            // 전송 실패로 상태 변경
            PushNotification failedNotification = notification.markAsFailed(e.getMessage());
            pushNotificationRepository.save(failedNotification);
        }
    }
    
    /**
     * 사용자의 푸시 알림 히스토리를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<PushNotification> getNotificationHistory(UserIdentity userId, int limit) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        
        return pushNotificationRepository.findRecentByUserId(userId, limit);
    }
    
    /**
     * 푸시 알림 통계를 조회합니다.
     */
    @Transactional(readOnly = true)
    public NotificationStatistics getStatistics() {
        long totalCount = pushNotificationRepository.count();
        long pendingCount = pushNotificationRepository.countByStatus(PushNotificationStatus.PENDING);
        long sentCount = pushNotificationRepository.countByStatus(PushNotificationStatus.SENT);
        long failedCount = pushNotificationRepository.countByStatus(PushNotificationStatus.FAILED);
        long receivedCount = pushNotificationRepository.countByStatus(PushNotificationStatus.RECEIVED);
        
        return new NotificationStatistics(
            totalCount, 
            pendingCount, 
            sentCount, 
            failedCount, 
            receivedCount
        );
    }
    
    /**
     * 푸시 알림을 수신 확인 처리합니다.
     */
    @Transactional
    public void markAsReceived(Long notificationId) {
        if (notificationId == null) {
            throw new IllegalArgumentException("NotificationId cannot be null");
        }
        
        pushNotificationRepository.findByIdentity(
            PushNotificationIdentity.of(notificationId)
        ).ifPresentOrElse(
            this::updateNotificationAsReceived,
            () -> log.warn("Push notification not found for ID: {}", notificationId)
        );
    }
    
    /**
     * 입력값을 검증합니다.
     */
    private void validateNotificationInput(UserIdentity userId, NotificationType notificationType, 
                                         String title, String message) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (notificationType == null) {
            throw new IllegalArgumentException("NotificationType cannot be null");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
    }
    
    /**
     * 사용자의 활성 디바이스 토큰을 조회합니다.
     */
    private List<DeviceToken> getActiveTokensForUser(UserIdentity userId) {
        List<DeviceToken> activeTokens = deviceTokenRepository.findActiveByUserId(userId);
        
        if (activeTokens.isEmpty()) {
            log.warn("No active device tokens found for user: {}", userId.id());
        }
        
        return activeTokens;
    }
    
    /**
     * 디바이스 토큰들에 대한 푸시 알림을 생성합니다.
     */
    private List<PushNotification> createNotificationsForTokens(
            UserIdentity userId, NotificationType notificationType, String title, 
            String message, String data, LocalDateTime scheduledAt, List<DeviceToken> tokens) {
        
        return tokens.stream()
            .map(token -> {
                PushNotification notification = PushNotification.of(
                    userId, token.token(), notificationType, title, message, data, scheduledAt
                );
                return pushNotificationRepository.save(notification);
            })
            .toList();
    }
    
    /**
     * 즉시 전송 대상인 알림들을 처리합니다.
     */
    private void processImmediateNotifications(List<PushNotification> notifications, LocalDateTime scheduledAt) {
        LocalDateTime threshold = LocalDateTime.now().plusMinutes(1);
        
        if (scheduledAt.isBefore(threshold)) {
            notifications.forEach(this::processSingleNotification);
        }
    }
    
    /**
     * 배치로 알림들을 처리합니다.
     */
    private int processBatchNotifications(List<PushNotification> notifications) {
        int successCount = 0;
        
        for (PushNotification notification : notifications) {
            try {
                processSingleNotification(notification);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to process notification in batch: {}", 
                    notification.identity().id(), e);
            }
        }
        
        return successCount;
    }
    
    /**
     * 알림을 수신됨으로 업데이트합니다.
     */
    private void updateNotificationAsReceived(PushNotification notification) {
        PushNotification receivedNotification = notification.markAsReceived();
        pushNotificationRepository.save(receivedNotification);
        
        log.debug("Marked push notification as received: {}", notification.identity().id());
    }
    
}