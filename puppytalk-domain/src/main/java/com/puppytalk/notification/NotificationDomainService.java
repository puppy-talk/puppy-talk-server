package com.puppytalk.notification;

import com.puppytalk.chat.ChatRoomId;
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

    private static final int DAILY_NOTIFICATION_LIMIT = 5;

    private final NotificationRepository notificationRepository;

    public NotificationDomainService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * 비활성 사용자 알림 생성
     * <p>
     * Backend 최적화: 중복 방지 및 발송 제한 확인
     */
    public NotificationId createInactivityNotification(
        UserId userId,
        PetId petId,
        ChatRoomId chatRoomId,
        String aiGeneratedTitle,
        String aiGeneratedContent
    ) {
        validateNotificationInput(userId, petId, aiGeneratedTitle, aiGeneratedContent);

        // 중복 알림 방지 - 이미 대기 중인 비활성 알림이 있는지 확인
        if (!isFirstInactivityNotification(userId)) {
            throw NotificationException.creationFailed("중복된 비활성화 알림이 이미 존재합니다");
        }

        // 일일 발송 제한 확인
        if (!isWithinDailyLimit(userId)) {
            throw NotificationException.dailyLimitExceeded();
        }

        LocalDateTime scheduledAt = LocalDateTime.now().plusMinutes(5); // 5분 후 발송

        Notification notification = Notification.createInactivityNotification(
            userId, petId, chatRoomId, aiGeneratedTitle, aiGeneratedContent, scheduledAt
        );

        return notificationRepository.save(notification);
    }

    /**
     * 시스템 알림 생성
     */
    public NotificationId createSystemNotification(UserId userId, String title, String content) {
        validateBasicNotificationInput(userId, title, content);

        Notification notification = Notification.createSystemNotification(userId, title, content);
        return notificationRepository.save(notification);
    }

    /**
     * 발송 대기 중인 알림 목록 조회 (스케줄러용)
     */
    public List<Notification> getPendingNotifications(int batchSize) {
        if (batchSize <= 0 || batchSize > 1000) {
            throw new IllegalArgumentException("Batch size must be between 1 and 1000");
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
     * 알림 발송 실패 처리 (재시도 로직 포함)
     */
    public void markAsFailed(NotificationId notificationId, String failureReason) {
        if (notificationId == null) {
            throw new IllegalArgumentException("NotificationId must not be null");
        }

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> NotificationException.notFound(notificationId));
            
        Notification updatedNotification = notification.incrementRetry(failureReason);
        notificationRepository.save(updatedNotification);
    }

    /**
     * 재시도 대상 실패 알림 조회
     */
    public List<Notification> getRetryableNotifications(int batchSize) {
        if (batchSize <= 0 || batchSize > 100) {
            throw new IllegalArgumentException("Retry batch size must be between 1 and 100");
        }

        return notificationRepository.findRetryableFailedNotifications(batchSize);
    }

    /**
     * 사용자 미읽은 알림 목록 조회
     */
    public List<Notification> getUnreadNotifications(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }

        return notificationRepository.findUnreadByUserId(userId);
    }

    /**
     * 사용자 미읽은 알림 개수 조회
     */
    public long getUnreadCount(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }

        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * 알림 대상자 필터링 (중복 방지, 일일 제한 확인)
     * <p>
     * Application layer에서 비활성 사용자 목록을 전달받아 필터링
     */
    public List<UserId> filterUsersForNotification(List<UserId> candidateUsers) {
        if (candidateUsers == null || candidateUsers.isEmpty()) {
            return List.of();
        }

        // 이미 알림을 받은 사용자는 제외 (중복 방지)
        return candidateUsers.stream()
            .filter(this::isFirstInactivityNotification)
            .filter(this::isWithinDailyLimit)
            .toList();

    }

    /**
     * 만료된 알림 정리 (성능 유지)
     */
    public int cleanupExpiredNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(24);
        return notificationRepository.deleteExpiredNotifications(cutoffDate);
    }

    /**
     * 완료된 오래된 알림 정리 (30일 기준)
     */
    public int cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
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

    // === Private Helper Methods ===

    private void validateNotificationInput(UserId userId, PetId petId, String title,
        String content) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        if (petId == null) {
            throw new IllegalArgumentException("PetId must not be null");
        }
        validateBasicNotificationInput(userId, title, content);
    }

    private void validateBasicNotificationInput(UserId userId, String title, String content) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be null or empty");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content must not be null or empty");
        }
    }

    private boolean isFirstInactivityNotification(UserId userId) {
        boolean alreadyExists =
            notificationRepository.existsByUserIdAndTypeAndStatus(
                userId,
                NotificationType.INACTIVITY_MESSAGE,
                NotificationStatus.CREATED
            )
                || notificationRepository.existsByUserIdAndTypeAndStatus(
                userId,
                NotificationType.INACTIVITY_MESSAGE,
                NotificationStatus.QUEUED
            );

        return !alreadyExists; // 기존에 없으면 "첫 알림"
    }

    private boolean isWithinDailyLimit(UserId userId) {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        long todayCount = notificationRepository.countSentNotificationsByUserAndDate(userId, today);

        return todayCount < DAILY_NOTIFICATION_LIMIT; // 제한 이하일 때만 true
    }

    private void updateNotificationStatus(NotificationId notificationId,
        NotificationStatus status) {
        if (notificationId == null) {
            throw new IllegalArgumentException("NotificationId must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("NotificationStatus must not be null");
        }

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> NotificationException.notFound(notificationId));
            
        Notification updatedNotification = notification.updateStatus(status);
        notificationRepository.save(updatedNotification);
    }
}