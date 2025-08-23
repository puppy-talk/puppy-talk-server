package com.puppytalk.notification.dto.response;

import com.puppytalk.notification.NotificationRepository;

/**
 * 알림 통계 결과
 */
public record NotificationStatsResult(
    long totalCount,
    long sentCount,
    long failedCount,
    long pendingCount,
    double successRate
) {
    
    public static NotificationStatsResult from(NotificationRepository.NotificationStats stats) {
        return new NotificationStatsResult(
            stats.totalCount(),
            stats.sentCount(),
            stats.failedCount(),
            stats.pendingCount(),
            stats.successRate()
        );
    }
    
    public static NotificationStatsResult empty() {
        return new NotificationStatsResult(0, 0, 0, 0, 0.0);
    }
}