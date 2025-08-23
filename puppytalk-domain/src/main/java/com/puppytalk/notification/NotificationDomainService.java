package com.puppytalk.notification;

import com.puppytalk.activity.ActivityDomainService;
import com.puppytalk.activity.UserActivity;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 알림 도메인 서비스
 * 
 * Backend 관점: 높은 처리량과 장애 내성을 갖춘 알림 비즈니스 로직
 */
public class NotificationDomainService {
    
    private static final int DAILY_NOTIFICATION_LIMIT = 5;
    private static final int INACTIVITY_THRESHOLD_HOURS = 2;
    
    private final NotificationRepository notificationRepository;
    private final ActivityDomainService activityDomainService;
    
    public NotificationDomainService(
        NotificationRepository notificationRepository,
        ActivityDomainService activityDomainService
    ) {
        this.notificationRepository = notificationRepository;
        this.activityDomainService = activityDomainService;
    }
    
    /**
     * 비활성 사용자 알림 생성
     * 
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
        if (isDuplicateInactivityNotification(userId)) {
            throw NotificationException.creationFailed(userId, NotificationType.INACTIVITY_MESSAGE, 
                "Duplicate inactivity notification already exists");
        }
        
        // 일일 발송 제한 확인
        if (isDailyLimitExceeded(userId)) {
            throw NotificationException.dailyLimitExceeded(userId, DAILY_NOTIFICATION_LIMIT);
        }
        
        try {
            LocalDateTime scheduledAt = LocalDateTime.now().plusMinutes(5); // 5분 후 발송
            
            Notification notification = Notification.createInactivityNotification(
                userId, petId, chatRoomId, aiGeneratedTitle, aiGeneratedContent, scheduledAt
            );
            
            return notificationRepository.save(notification);
            
        } catch (Exception e) {
            throw NotificationException.creationFailed(userId, NotificationType.INACTIVITY_MESSAGE, e.getMessage());
        }
    }
    
    /**
     * 시스템 알림 생성
     */
    public NotificationId createSystemNotification(UserId userId, String title, String content) {
        validateBasicNotificationInput(userId, title, content);
        
        try {
            Notification notification = Notification.createSystemNotification(userId, title, content);
            return notificationRepository.save(notification);
            
        } catch (Exception e) {
            throw NotificationException.creationFailed(userId, NotificationType.SYSTEM_NOTIFICATION, e.getMessage());
        }
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
        
        try {
            Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
            if (optionalNotification.isEmpty()) {
                throw NotificationException.notificationNotFound(notificationId);
            }
            
            Notification notification = optionalNotification.get();
            Notification updatedNotification = notification.incrementRetry(failureReason);
            
            notificationRepository.save(updatedNotification);
            
        } catch (Exception e) {
            throw NotificationException.sendingFailed(notificationId, "Failed to mark as failed", e);
        }
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
     * 비활성 사용자 일괄 감지 및 알림 생성 후보 반환
     * 
     * Backend 최적화: 대량 사용자 처리를 위한 효율적 설계
     */
    public List<UserId> findInactiveUsersForNotification() {
        try {
            List<UserId> inactiveUsers = activityDomainService.findInactiveUsers(INACTIVITY_THRESHOLD_HOURS);
            
            // 이미 알림을 받은 사용자는 제외 (중복 방지)
            return inactiveUsers.stream()
                .filter(userId -> !isDuplicateInactivityNotification(userId))
                .filter(userId -> !isDailyLimitExceeded(userId))
                .toList();
                
        } catch (Exception e) {
            throw NotificationException.schedulingFailed("Failed to find inactive users: " + e.getMessage());
        }
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
    
    /**
     * 사용자가 특정 시간 동안 비활성 상태인지 확인
     */
    public boolean isUserInactive(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        
        return !activityDomainService.isUserActive(userId, INACTIVITY_THRESHOLD_HOURS);
    }
    
    // === Private Helper Methods ===
    
    private void validateNotificationInput(UserId userId, PetId petId, String title, String content) {
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
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title must not be null or empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content must not be null or empty");
        }
    }
    
    private boolean isDuplicateInactivityNotification(UserId userId) {
        return notificationRepository.existsByUserIdAndTypeAndStatus(
            userId, 
            NotificationType.INACTIVITY_MESSAGE, 
            NotificationStatus.CREATED
        ) || notificationRepository.existsByUserIdAndTypeAndStatus(
            userId, 
            NotificationType.INACTIVITY_MESSAGE, 
            NotificationStatus.QUEUED
        );
    }
    
    private boolean isDailyLimitExceeded(UserId userId) {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        long todayCount = notificationRepository.countSentNotificationsByUserAndDate(userId, today);
        return todayCount >= DAILY_NOTIFICATION_LIMIT;
    }
    
    private void updateNotificationStatus(NotificationId notificationId, NotificationStatus status) {
        if (notificationId == null) {
            throw new IllegalArgumentException("NotificationId must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("NotificationStatus must not be null");
        }
        
        try {
            Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
            if (optionalNotification.isEmpty()) {
                throw NotificationException.notificationNotFound(notificationId);
            }
            
            Notification notification = optionalNotification.get();
            Notification updatedNotification = notification.updateStatus(status);
            
            notificationRepository.save(updatedNotification);
            
        } catch (Exception e) {
            throw NotificationException.sendingFailed(notificationId, 
                "Failed to update status to " + status, e);
        }
    }
}