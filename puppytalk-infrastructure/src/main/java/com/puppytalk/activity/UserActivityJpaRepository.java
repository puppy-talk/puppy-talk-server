package com.puppytalk.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 활동 JPA 리포지토리
 * 
 * Backend 최적화: 네이티브 쿼리 활용으로 성능 극대화
 */
@Repository
public interface UserActivityJpaRepository extends JpaRepository<UserActivityJpaEntity, Long> {
    
    /**
     * 사용자의 최근 활동 조회 (성능 최적화)
     */
    @Query("SELECT ua FROM UserActivityJpaEntity ua " +
           "WHERE ua.userId = :userId " +
           "ORDER BY ua.activityAt DESC " +
           "LIMIT 1")
    Optional<UserActivityJpaEntity> findLatestByUserId(@Param("userId") Long userId);
    
    /**
     * 특정 시점 이후 사용자 활동 목록 조회
     */
    @Query("SELECT ua FROM UserActivityJpaEntity ua " +
           "WHERE ua.userId = :userId AND ua.activityAt > :since " +
           "ORDER BY ua.activityAt DESC")
    List<UserActivityJpaEntity> findByUserIdAndActivityAtAfter(
        @Param("userId") Long userId, 
        @Param("since") LocalDateTime since
    );
    
    /**
     * 사용자의 특정 활동 타입 최근 활동 조회
     */
    @Query("SELECT ua FROM UserActivityJpaEntity ua " +
           "WHERE ua.userId = :userId AND ua.activityType = :activityType " +
           "ORDER BY ua.activityAt DESC " +
           "LIMIT 1")
    Optional<UserActivityJpaEntity> findLatestByUserIdAndActivityType(
        @Param("userId") Long userId, 
        @Param("activityType") ActivityType activityType
    );
    
    /**
     * 비활성 사용자 감지 (성능 중요)
     * 
     * 중요 활동(LOGIN, MESSAGE_SENT, CHAT_OPENED)을 기준으로 마지막 활동이
     * threshold 이전인 사용자들의 ID를 반환
     */
    @Query(value = """
        SELECT DISTINCT ua.user_id 
        FROM user_activities ua
        WHERE ua.activity_type IN ('LOGIN', 'MESSAGE_SENT', 'CHAT_OPENED')
          AND ua.user_id NOT IN (
              SELECT DISTINCT ua2.user_id 
              FROM user_activities ua2 
              WHERE ua2.activity_type IN ('LOGIN', 'MESSAGE_SENT', 'CHAT_OPENED')
                AND ua2.activity_at > :threshold
          )
        """, nativeQuery = true)
    List<Long> findInactiveUserIds(@Param("threshold") LocalDateTime threshold);
    
    /**
     * 사용자별 기간 내 활동 횟수
     */
    @Query("SELECT COUNT(ua) FROM UserActivityJpaEntity ua " +
           "WHERE ua.userId = :userId " +
           "AND ua.activityAt BETWEEN :startDate AND :endDate")
    long countByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 오래된 활동 데이터 삭제 (배치 처리)
     */
    @Modifying
    @Query("DELETE FROM UserActivityJpaEntity ua WHERE ua.createdAt < :cutoffDate")
    int deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 특정 사용자의 활동 존재 여부 확인 (경량 쿼리)
     */
    @Query("SELECT COUNT(ua) > 0 FROM UserActivityJpaEntity ua " +
           "WHERE ua.userId = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
    
    /**
     * 활성 사용자 수 조회 (대시보드용)
     */
    @Query(value = """
        SELECT COUNT(DISTINCT ua.user_id)
        FROM user_activities ua
        WHERE ua.activity_type IN ('LOGIN', 'MESSAGE_SENT', 'CHAT_OPENED')
          AND ua.activity_at > :threshold
        """, nativeQuery = true)
    long countActiveUsers(@Param("threshold") LocalDateTime threshold);
}