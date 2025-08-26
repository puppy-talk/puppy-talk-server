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
     * 비활성 사용자 감지: 특정 시간 이전에 마지막 중요 활동을 한 사용자 ID 목록
     * 성능 중요: 대량 데이터 처리를 위한 최적화된 쿼리
     */
    List<UserId> findInactiveUserIds(LocalDateTime inactivityThreshold);
}