package com.puppytalk.activity;

import com.puppytalk.user.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 활동 리포지토리 인터페이스
 * 
 * Backend 관점: 성능 중심의 쿼리 인터페이스 설계
 */
public interface UserActivityRepository {
    
    /**
     * 사용자 활동 저장
     */
    ActivityId save(UserActivity activity);
    
    /**
     * ID로 활동 조회
     */
    Optional<UserActivity> findById(ActivityId id);
    
    /**
     * 사용자의 최근 활동 조회 (성능 최적화)
     */
    Optional<UserActivity> findLatestByUserId(UserId userId);
    
    /**
     * 특정 시점 이후 활동한 사용자 활동 목록 조회
     * 성능: 인덱스 최적화 필요 (user_id, activity_at DESC)
     */
    List<UserActivity> findByUserIdAndActivityAtAfter(UserId userId, LocalDateTime since);
    
    /**
     * 특정 활동 유형의 최근 활동 조회
     */
    Optional<UserActivity> findLatestByUserIdAndActivityType(UserId userId, ActivityType activityType);
    
    /**
     * 비활성 사용자 감지: 특정 시간 이전에 마지막 중요 활동을 한 사용자 ID 목록
     * 성능 중요: 대량 데이터 처리를 위한 최적화된 쿼리
     */
    List<UserId> findInactiveUserIds(LocalDateTime inactivityThreshold);
    
    /**
     * 사용자별 일일 활동 통계 (선택적 기능)
     */
    long countByUserIdAndDateRange(UserId userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 활동 존재 여부 확인 (경량 쿼리)
     */
    boolean existsById(ActivityId id);
    
    /**
     * 오래된 활동 데이터 정리 (성능 유지를 위한 배치 작업)
     */
    int deleteActivitiesOlderThan(LocalDateTime cutoffDate);
}