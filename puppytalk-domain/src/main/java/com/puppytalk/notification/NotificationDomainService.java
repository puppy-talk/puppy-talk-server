package com.puppytalk.notification;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.notification.exception.NotificationException;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 도메인 서비스
 * <p>
 * Backend 관점: 높은 처리량과 장애 내성을 갖춘 알림 비즈니스 로직
 */
public class NotificationDomainService {

    private static final int NOTIFICATION_DELAY_MINUTES = 5;
    private static final int MAX_BATCH_SIZE = 1000;
    private static final int NOTIFICATION_EXPIRY_HOURS = 24;
    private static final int OLD_NOTIFICATION_CLEANUP_DAYS = 30;

    private final NotificationRepository notificationRepository;

    public NotificationDomainService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * 비활성 사용자 알림 생성
     */
    public void createInactivityNotification(
        UserId userId,
        PetId petId,
        ChatRoomId chatRoomId,
        String aiGeneratedTitle,
        String aiGeneratedContent
    ) {

        // 중복 알림 방지
        if (existsByUserId(userId)) {
            throw NotificationException.creationFailed("중복된 비활성화 알림이 이미 존재합니다");
        }

        LocalDateTime scheduledAt = LocalDateTime.now().plusMinutes(NOTIFICATION_DELAY_MINUTES);

        Notification notification = Notification.createInactivityNotification(
            userId, petId, chatRoomId, aiGeneratedTitle, aiGeneratedContent, scheduledAt
        );

        notificationRepository.save(notification);
    }


    /**
     * 발송 대기 중인 알림 목록 조회 (스케줄러용)
     */
    public List<Notification> findPendingNotifications(int batchSize) {
        if (batchSize <= 0 || batchSize > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("Batch size must be between 1 and " + MAX_BATCH_SIZE);
        }

        LocalDateTime now = LocalDateTime.now();
        return notificationRepository.findPendingNotifications(now, batchSize);
    }

    /**
     * 알림 발송 완료 처리
     */
    public void markAsSent(NotificationId notificationId) {
        updateNotificationStatus(notificationId, NotificationStatus.SENT);
    }

    /**
     * 알림 읽음 처리
     */
    public void markAsRead(NotificationId notificationId) {
        updateNotificationStatus(notificationId, NotificationStatus.READ);
    }

    /**
     * 사용자 미읽은 알림 개수 조회
     */
    public long getUnreadCount(UserId userId) {

        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * 알림 대상자 필터링 (중복 방지, 일일 제한 확인)
     * <p>
     * Application layer에서 비활성 사용자 목록을 전달받아 필터링
     */
    public List<UserId> filterUsersForNotification(List<UserId> candidateUsers) {
        if (candidateUsers == null) {
            throw new IllegalArgumentException("후보 사용자 목록은 필수입니다");
        }
        
        if (candidateUsers.isEmpty()) {
            return List.of();
        }

        // 이미 알림을 받은 사용자는 제외 (중복 방지)
        return candidateUsers.stream()
            .filter(e -> !existsByUserId(UserId.from(e.value())))
            .toList();

    }

    /**
     * 만료된 알림 정리 (성능 유지)
     */
    public int cleanupExpiredNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(NOTIFICATION_EXPIRY_HOURS);
        return notificationRepository.deleteExpiredNotifications(cutoffDate);
    }

    /**
     * 완료된 오래된 알림 정리 (30일 기준)
     */
    public int cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(OLD_NOTIFICATION_CLEANUP_DAYS);
        return notificationRepository.deleteCompletedNotificationsOlderThan(cutoffDate);
    }

    /**
     * 알림 통계 조회
     */
    public NotificationRepository.NotificationStats getNotificationStats(
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Date range must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        return notificationRepository.getNotificationStats(startDate, endDate);
    }

    private boolean existsByUserId(UserId userId) {
        boolean existsByCreatedStatus = notificationRepository.existsByUserIdAndTypeAndStatus(
            userId,
            NotificationType.INACTIVITY_MESSAGE,
            NotificationStatus.CREATED
        );

        boolean existsByQueuedStatus = notificationRepository.existsByUserIdAndTypeAndStatus(
            userId,
            NotificationType.INACTIVITY_MESSAGE,
            NotificationStatus.QUEUED
        );

        return existsByCreatedStatus || existsByQueuedStatus;
    }

    private void updateNotificationStatus(NotificationId notificationId,
        NotificationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("NotificationStatus must not be null");
        }

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> NotificationException.notFound(notificationId));
            
        Notification updatedNotification = notification.updateStatus(status);
        notificationRepository.save(updatedNotification);
    }
}