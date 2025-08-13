package com.puppy.talk.service.notification;

import com.puppy.talk.infrastructure.push.DeviceTokenRepository;
import com.puppy.talk.infrastructure.push.PushNotificationRepository;
import com.puppy.talk.infrastructure.push.PushNotificationSender;
import com.puppy.talk.model.push.DeviceToken;
import com.puppy.talk.model.push.NotificationType;
import com.puppy.talk.model.push.PushNotification;
import com.puppy.talk.model.push.PushNotificationStatus;
import com.puppy.talk.model.user.UserIdentity;
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
public class PushNotificationService {
    
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
        
        log.debug("Sending push notification to user={}, type={}", userId.id(), notificationType);
        
        // 사용자의 활성 디바이스 토큰들 조회
        List<DeviceToken> activeTokens = deviceTokenRepository.findActiveByUserId(userId);
        
        if (activeTokens.isEmpty()) {
            log.warn("No active device tokens found for user: {}", userId.id());
            return;
        }
        
        // 각 디바이스 토큰에 대해 푸시 알림 생성 및 전송
        for (DeviceToken deviceToken : activeTokens) {
            PushNotification notification = PushNotification.of(
                userId,
                deviceToken.token(),
                notificationType,
                title,
                message,
                data,
                scheduledAt
            );
            
            // 푸시 알림 저장
            PushNotification savedNotification = pushNotificationRepository.save(notification);
            
            // 즉시 전송인 경우 바로 전송 처리
            if (scheduledAt.isBefore(LocalDateTime.now().plusMinutes(1))) {
                processSingleNotification(savedNotification);
            }
        }
        
        log.info("Created push notifications for user={}, devices={}, type={}", 
            userId.id(), activeTokens.size(), notificationType);
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
        
        for (PushNotification notification : pendingNotifications) {
            processSingleNotification(notification);
        }
        
        log.info("Processed {} push notifications", pendingNotifications.size());
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
            com.puppy.talk.model.push.PushNotificationIdentity.of(notificationId)
        ).ifPresent(notification -> {
            PushNotification receivedNotification = notification.markAsReceived();
            pushNotificationRepository.save(receivedNotification);
            
            log.debug("Marked push notification as received: {}", notificationId);
        });
    }
    
    /**
     * 푸시 알림 통계 DTO
     */
    public record NotificationStatistics(
        long totalCount,
        long pendingCount,
        long sentCount,
        long failedCount,
        long receivedCount
    ) {}
}