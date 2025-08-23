package com.puppytalk.notification;

import com.puppytalk.user.UserId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 알림 리포지토리 구현
 * 
 * Backend 관점: 고성능 알림 처리와 안정성
 */
@Repository
public class NotificationRepositoryImpl implements NotificationRepository {
    
    private final NotificationJpaRepository jpaRepository;
    
    public NotificationRepositoryImpl(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public NotificationId save(Notification notification) {
        try {
            NotificationJpaEntity entity = NotificationJpaEntity.fromDomain(notification);
            NotificationJpaEntity saved = jpaRepository.save(entity);
            return NotificationId.of(saved.getId());
        } catch (Exception e) {
            throw NotificationException.creationFailed(
                notification.userId(),
                notification.type(),
                "Failed to save notification: " + e.getMessage()
            );
        }
    }
    
    @Override
    public Optional<Notification> findById(NotificationId id) {
        return jpaRepository.findById(id.getValue())
            .map(NotificationJpaEntity::toDomain);
    }
    
    @Override
    public List<Notification> findPendingNotifications(LocalDateTime now, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return jpaRepository.findPendingNotifications(now, pageable)
            .stream()
            .map(NotificationJpaEntity::toDomain)
            .toList();
    }
    
    @Override
    public List<Notification> findRetryableFailedNotifications(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return jpaRepository.findRetryableFailedNotifications(pageable)
            .stream()
            .map(NotificationJpaEntity::toDomain)
            .toList();
    }
    
    @Override
    public List<Notification> findUnreadByUserId(UserId userId) {
        return jpaRepository.findUnreadByUserId(userId.getValue())
            .stream()
            .map(NotificationJpaEntity::toDomain)
            .toList();
    }
    
    @Override
    public List<Notification> findByUserIdOrderByCreatedAtDesc(UserId userId, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId.getValue(), pageable)
            .stream()
            .map(NotificationJpaEntity::toDomain)
            .toList();
    }
    
    @Override
    public long countUnreadByUserId(UserId userId) {
        return jpaRepository.countUnreadByUserId(userId.getValue());
    }
    
    @Override
    public List<Notification> findByTypeAndStatus(NotificationType type, NotificationStatus status, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return jpaRepository.findByTypeAndStatus(type, status, pageable)
            .stream()
            .map(NotificationJpaEntity::toDomain)
            .toList();
    }
    
    @Override
    public void updateStatus(NotificationId id, NotificationStatus status) {
        try {
            Optional<NotificationJpaEntity> entity = jpaRepository.findById(id.getValue());
            if (entity.isPresent()) {
                NotificationJpaEntity notification = entity.get();
                notification.updateStatus(status);
                jpaRepository.save(notification);
            } else {
                throw NotificationException.notificationNotFound(id);
            }
        } catch (Exception e) {
            throw NotificationException.sendingFailed(id, "Failed to update status", e);
        }
    }
    
    @Override
    public void updateStatusBatch(List<NotificationId> ids, NotificationStatus status) {
        try {
            List<Long> longIds = ids.stream()
                .map(NotificationId::getValue)
                .toList();
            jpaRepository.updateStatusBatch(longIds, status);
        } catch (Exception e) {
            throw NotificationException.batchProcessingFailed(
                "Failed to update status for batch", e
            );
        }
    }
    
    @Override
    public int deleteExpiredNotifications(LocalDateTime cutoffDate) {
        try {
            return jpaRepository.deleteExpiredNotifications(cutoffDate);
        } catch (Exception e) {
            throw NotificationException.batchProcessingFailed(
                "Failed to delete expired notifications", e
            );
        }
    }
    
    @Override
    public int deleteCompletedNotificationsOlderThan(LocalDateTime cutoffDate) {
        try {
            return jpaRepository.deleteCompletedNotificationsOlderThan(cutoffDate);
        } catch (Exception e) {
            throw NotificationException.batchProcessingFailed(
                "Failed to delete old notifications", e
            );
        }
    }
    
    @Override
    public NotificationStats getNotificationStats(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            long totalCount = jpaRepository.countNotificationsByDateRange(startDate, endDate);
            long sentCount = jpaRepository.countSentNotificationsByDateRange(startDate, endDate);
            long failedCount = jpaRepository.countFailedNotificationsByDateRange(startDate, endDate);
            long pendingCount = jpaRepository.countPendingNotificationsByDateRange(startDate, endDate);
            
            double successRate = totalCount > 0 ? (double) sentCount / totalCount * 100 : 0.0;
            
            return new NotificationStats(totalCount, sentCount, failedCount, pendingCount, successRate);
            
        } catch (Exception e) {
            throw NotificationException.schedulingFailed(
                "Failed to get notification statistics: " + e.getMessage()
            );
        }
    }
    
    @Override
    public long countSentNotificationsByUserAndDate(UserId userId, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        return jpaRepository.countSentNotificationsByUserAndDate(
            userId.getValue(), startOfDay, endOfDay
        );
    }
    
    @Override
    public boolean existsByUserIdAndTypeAndStatus(UserId userId, NotificationType type, NotificationStatus status) {
        return jpaRepository.existsByUserIdAndTypeAndStatus(userId.getValue(), type, status);
    }
}