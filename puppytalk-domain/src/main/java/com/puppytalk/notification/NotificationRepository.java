package com.puppytalk.notification;

import com.puppytalk.user.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 알림 리포지토리 인터페이스
 * 
 * Backend 관점: 대용량 알림 처리를 위한 성능 최적화된 인터페이스
 */
public interface NotificationRepository {
    
    /**
     * 알림 저장
     */
    NotificationId save(Notification notification);
    
    /**
     * ID로 알림 조회
     */
    Optional<Notification> findById(NotificationId id);
    
    /**
     * 발송 대기 중인 알림 목록 조회 (스케줄링용)
     * 성능: 인덱스 최적화 필요 (scheduled_at ASC, status)
     */
    List<Notification> findPendingNotifications(LocalDateTime now, int limit);
    
    /**
     * 재시도 대상 실패 알림 조회
     */
    List<Notification> findRetryableFailedNotifications(int limit);
    
    /**
     * 사용자별 미읽은 알림 목록 조회
     */
    List<Notification> findUnreadByUserId(UserId userId);
    
    /**
     * 사용자별 알림 목록 조회 (페이징)
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(UserId userId, int offset, int limit);
    
    /**
     * 사용자별 미읽은 알림 개수
     */
    long countUnreadByUserId(UserId userId);
    
    /**
     * 특정 타입의 알림 목록 조회
     */
    List<Notification> findByTypeAndStatus(NotificationType type, NotificationStatus status, int limit);
    
    /**
     * 알림 상태 업데이트 (배치 처리용)
     */
    void updateStatus(NotificationId id, NotificationStatus status);
    
    /**
     * 여러 알림 상태 일괄 업데이트
     */
    void updateStatusBatch(List<NotificationId> ids, NotificationStatus status);
    
    /**
     * 만료된 알림 정리 (성능 유지)
     */
    int deleteExpiredNotifications(LocalDateTime cutoffDate);
    
    /**
     * 완료된 오래된 알림 정리 (보관 기간 지난 것들)
     */
    int deleteCompletedNotificationsOlderThan(LocalDateTime cutoffDate);
    
    /**
     * 알림 통계 조회 (대시보드용)
     */
    NotificationStats getNotificationStats(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 사용자별 일일 알림 발송 제한 확인
     */
    long countSentNotificationsByUserAndDate(UserId userId, LocalDateTime date);
    
    /**
     * 중복 알림 방지를 위한 존재 확인
     */
    boolean existsByUserIdAndTypeAndStatus(UserId userId, NotificationType type, NotificationStatus status);
    
    /**
     * 알림 통계 정보 (내부 클래스)
     */
    record NotificationStats(
        long totalCount,
        long sentCount,
        long failedCount,
        long pendingCount,
        double successRate
    ) {}
}