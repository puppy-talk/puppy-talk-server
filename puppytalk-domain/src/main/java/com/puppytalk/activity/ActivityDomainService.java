package com.puppytalk.activity;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.user.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 활동 도메인 서비스
 * 
 * Backend 관점: 높은 처리량과 안정성을 보장하는 비즈니스 로직
 */
public class ActivityDomainService {
    
    private final UserActivityRepository userActivityRepository;
    
    public ActivityDomainService(UserActivityRepository userActivityRepository) {
        this.userActivityRepository = userActivityRepository;
    }
    
    /**
     * 사용자 활동 기록 (채팅방 관련)
     */
    public ActivityId recordActivity(UserId userId, ChatRoomId chatRoomId, ActivityType activityType) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        if (activityType == null) {
            throw new IllegalArgumentException("ActivityType must not be null");
        }
        
        try {
            LocalDateTime now = LocalDateTime.now();
            UserActivity activity = UserActivity.createActivity(userId, chatRoomId, activityType, now);
            
            return userActivityRepository.save(activity);
        } catch (Exception e) {
            throw ActivityTrackingException.trackingFailed(userId, activityType, e.getMessage());
        }
    }
    
    /**
     * 전역 사용자 활동 기록 (LOGIN/LOGOUT)
     */
    public ActivityId recordGlobalActivity(UserId userId, ActivityType activityType) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        if (activityType == null) {
            throw new IllegalArgumentException("ActivityType must not be null");
        }
        if (!activityType.equals(ActivityType.LOGIN) && !activityType.equals(ActivityType.LOGOUT)) {
            throw new IllegalArgumentException("Only LOGIN and LOGOUT are allowed for global activities");
        }
        
        try {
            LocalDateTime now = LocalDateTime.now();
            UserActivity activity = UserActivity.createGlobalActivity(userId, activityType, now);
            
            return userActivityRepository.save(activity);
        } catch (Exception e) {
            throw ActivityTrackingException.trackingFailed(userId, activityType, e.getMessage());
        }
    }
    
    /**
     * 사용자의 최근 활동 조회
     */
    public Optional<UserActivity> getLatestActivity(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        
        return userActivityRepository.findLatestByUserId(userId);
    }
    
    /**
     * 비활성 사용자 감지 (2시간 기준)
     * 
     * Backend 최적화: 대량 사용자 처리를 위한 성능 최적화
     */
    public List<UserId> findInactiveUsers() {
        return findInactiveUsers(2); // 기본 2시간
    }
    
    /**
     * 비활성 사용자 감지 (시간 커스텀)
     */
    public List<UserId> findInactiveUsers(int inactiveHours) {
        if (inactiveHours <= 0) {
            throw new IllegalArgumentException("Inactive hours must be positive");
        }
        
        try {
            LocalDateTime threshold = LocalDateTime.now().minusHours(inactiveHours);
            return userActivityRepository.findInactiveUserIds(threshold);
        } catch (Exception e) {
            throw ActivityTrackingException.inactiveUserDetectionFailed(e.getMessage());
        }
    }
    
    /**
     * 사용자 활성 상태 확인
     */
    public boolean isUserActive(UserId userId, int inactiveHours) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        
        Optional<UserActivity> latestActivity = getLatestActivity(userId);
        if (latestActivity.isEmpty()) {
            return false; // 활동 기록이 없으면 비활성으로 간주
        }
        
        LocalDateTime threshold = LocalDateTime.now().minusHours(inactiveHours);
        return latestActivity.get().isAfter(threshold);
    }
    
    /**
     * 사용자의 특정 기간 내 활동 횟수 조회
     */
    public long getActivityCount(UserId userId, LocalDateTime startDate, LocalDateTime endDate) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Date range must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        
        return userActivityRepository.countByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    /**
     * 오래된 활동 데이터 정리 (성능 유지)
     * 
     * Backend 관점: 데이터 증가로 인한 성능 저하 방지
     */
    public int cleanupOldActivities(int daysToKeep) {
        if (daysToKeep <= 0) {
            throw new IllegalArgumentException("Days to keep must be positive");
        }
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        return userActivityRepository.deleteActivitiesOlderThan(cutoffDate);
    }
    
    /**
     * 사용자의 최근 중요 활동 조회 (비활성 판단에 사용)
     */
    public Optional<UserActivity> getLatestCriticalActivity(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        
        // 중요 활동들을 순차적으로 확인
        for (ActivityType type : ActivityType.values()) {
            if (type.isCriticalActivity()) {
                Optional<UserActivity> activity = userActivityRepository
                    .findLatestByUserIdAndActivityType(userId, type);
                if (activity.isPresent()) {
                    return activity;
                }
            }
        }
        
        return Optional.empty();
    }
}