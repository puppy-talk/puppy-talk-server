package com.puppy.talk.notification.dto;

/**
 * 비활성 알림 상태 통계 DTO
 */
public record InactivityNotificationStatistics(
    long totalCount,
    long pendingCount, 
    long sentCount,
    long disabledCount
) {}
