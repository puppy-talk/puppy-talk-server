package com.puppytalk.activity;

import com.puppytalk.user.UserId;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 활동 리포지토리 구현
 * 
 * Backend 관점: 높은 처리량과 안정성을 위한 최적화
 */
@Repository
public class UserActivityRepositoryImpl implements UserActivityRepository {
    
    private final UserActivityJpaRepository jpaRepository;
    
    public UserActivityRepositoryImpl(UserActivityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public ActivityId save(UserActivity activity) {
        try {
            UserActivityJpaEntity entity = UserActivityJpaEntity.fromDomain(activity);
            UserActivityJpaEntity saved = jpaRepository.save(entity);
            return ActivityId.from(saved.getId());
        } catch (Exception e) {
            throw ActivityTrackingException.trackingFailed(
                activity.userId(), 
                activity.activityType(), 
                "Failed to save activity: " + e.getMessage()
            );
        }
    }
    
    @Override
    public Optional<UserActivity> findById(ActivityId id) {
        return jpaRepository.findById(id.getValue())
            .map(UserActivityJpaEntity::toDomain);
    }
    
    @Override
    public Optional<UserActivity> findLatestByUserId(UserId userId) {
        return jpaRepository.findLatestByUserId(userId.getValue())
            .map(UserActivityJpaEntity::toDomain);
    }
    
    @Override
    public List<UserActivity> findByUserIdAndActivityAtAfter(UserId userId, LocalDateTime since) {
        return jpaRepository.findByUserIdAndActivityAtAfter(userId.getValue(), since)
            .stream()
            .map(UserActivityJpaEntity::toDomain)
            .toList();
    }
    
    @Override
    public Optional<UserActivity> findLatestByUserIdAndActivityType(UserId userId, ActivityType activityType) {
        return jpaRepository.findLatestByUserIdAndActivityType(userId.getValue(), activityType)
            .map(UserActivityJpaEntity::toDomain);
    }
    
    @Override
    public List<UserId> findInactiveUserIds(LocalDateTime inactivityThreshold) {
        try {
            return jpaRepository.findInactiveUserIds(inactivityThreshold)
                .stream()
                .map(UserId::of)
                .toList();
        } catch (Exception e) {
            throw ActivityTrackingException.inactiveUserDetectionFailed(
                "Database query failed: " + e.getMessage()
            );
        }
    }
    
    @Override
    public long countByUserIdAndDateRange(UserId userId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.countByUserIdAndDateRange(userId.getValue(), startDate, endDate);
    }
    
    @Override
    public boolean existsById(ActivityId id) {
        return jpaRepository.existsById(id.getValue());
    }
    
    @Override
    public int deleteActivitiesOlderThan(LocalDateTime cutoffDate) {
        try {
            return jpaRepository.deleteByCreatedAtBefore(cutoffDate);
        } catch (Exception e) {
            throw ActivityTrackingException.dataCorrupted(
                "Failed to cleanup old activities: " + e.getMessage()
            );
        }
    }
}