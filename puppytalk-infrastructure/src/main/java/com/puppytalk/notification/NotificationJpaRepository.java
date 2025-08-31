package com.puppytalk.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 JPA 리포지토리
 * 
 * Backend 최적화: 대용량 알림 처리를 위한 성능 쿼리
 */
@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {
    
    /**
     * 발송 대기 중인 알림 목록 조회 (스케줄링용)
     */
    @Query("SELECT n FROM NotificationJpaEntity n " +
           "WHERE n.status IN ('CREATED', 'QUEUED') " +
           "AND n.scheduledAt <= :now " +
           "ORDER BY n.scheduledAt ASC")
    List<NotificationJpaEntity> findPendingNotifications(
        @Param("now") LocalDateTime now, 
        org.springframework.data.domain.Pageable pageable
    );
    
    
    
    /**
     * 사용자별 알림 목록 조회 (페이징)
     */
    @Query("SELECT n FROM NotificationJpaEntity n " +
           "WHERE n.userId = :userId " +
           "ORDER BY n.createdAt DESC")
    List<NotificationJpaEntity> findByUserIdOrderByCreatedAtDesc(
        @Param("userId") Long userId,
        org.springframework.data.domain.Pageable pageable
    );
    
    /**
     * 사용자별 미읽은 알림 개수
     */
    @Query("SELECT COUNT(n) FROM NotificationJpaEntity n " +
           "WHERE n.userId = :userId " +
           "AND n.status NOT IN ('READ')")
    long countUnreadByUserId(@Param("userId") Long userId);
    
    /**
     * 특정 타입과 상태의 알림 목록 조회
     */
    @Query("SELECT n FROM NotificationJpaEntity n " +
           "WHERE n.type = :type AND n.status = :status " +
           "ORDER BY n.createdAt DESC")
    List<NotificationJpaEntity> findByTypeAndStatus(
        @Param("type") NotificationType type,
        @Param("status") NotificationStatus status,
        org.springframework.data.domain.Pageable pageable
    );
    
    /**
     * 여러 알림 상태 일괄 업데이트
     */
    @Modifying
    @Query("UPDATE NotificationJpaEntity n " +
           "SET n.status = :status, n.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE n.id IN :ids")
    void updateStatusBatch(
        @Param("ids") List<Long> ids, 
        @Param("status") NotificationStatus status
    );
    
    /**
     * 만료된 알림 삭제
     */
    @Modifying
    @Query("DELETE FROM NotificationJpaEntity n " +
           "WHERE n.scheduledAt < :cutoffDate " +
           "AND n.status IN ('CREATED', 'QUEUED', 'FAILED')")
    int deleteExpiredNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 완료된 오래된 알림 삭제
     */
    @Modifying
    @Query("DELETE FROM NotificationJpaEntity n " +
           "WHERE n.createdAt < :cutoffDate " +
           "AND n.status IN ('SENT', 'READ', 'CANCELLED')")
    int deleteCompletedNotificationsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 알림 통계 조회 - 전체 건수
     */
    @Query("SELECT COUNT(n) FROM NotificationJpaEntity n " +
           "WHERE n.createdAt BETWEEN :startDate AND :endDate")
    long countNotificationsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 알림 통계 조회 - 발송 성공 건수
     */
    @Query("SELECT COUNT(n) FROM NotificationJpaEntity n " +
           "WHERE n.createdAt BETWEEN :startDate AND :endDate " +
           "AND n.status IN ('SENT', 'READ')")
    long countSentNotificationsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 알림 통계 조회 - 발송 실패 건수
     */
    @Query("SELECT COUNT(n) FROM NotificationJpaEntity n " +
           "WHERE n.createdAt BETWEEN :startDate AND :endDate " +
           "AND n.status = 'FAILED'")
    long countFailedNotificationsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 알림 통계 조회 - 대기 중 건수
     */
    @Query("SELECT COUNT(n) FROM NotificationJpaEntity n " +
           "WHERE n.createdAt BETWEEN :startDate AND :endDate " +
           "AND n.status IN ('CREATED', 'QUEUED', 'SENDING')")
    long countPendingNotificationsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 사용자별 일일 알림 발송 횟수
     */
    @Query("SELECT COUNT(n) FROM NotificationJpaEntity n " +
           "WHERE n.userId = :userId " +
           "AND n.status IN ('SENT', 'READ') " +
           "AND n.sentAt >= :startOfDay AND n.sentAt < :endOfDay")
    long countSentNotificationsByUserAndDate(
        @Param("userId") Long userId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );
    
    /**
     * 중복 알림 방지를 위한 존재 확인
     */
    @Query("SELECT COUNT(n) > 0 FROM NotificationJpaEntity n " +
           "WHERE n.userId = :userId " +
           "AND n.type = :type " +
           "AND n.status = :status")
    boolean existsByUserIdAndTypeAndStatus(
        @Param("userId") Long userId,
        @Param("type") NotificationType type,
        @Param("status") NotificationStatus status
    );
}