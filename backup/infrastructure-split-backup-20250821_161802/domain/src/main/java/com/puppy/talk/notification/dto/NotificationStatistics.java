package com.puppy.talk.notification.dto;

/**
 * 푸시 알림 통계 DTO
 */
public record NotificationStatistics(
    long totalCount,
    long pendingCount,
    long sentCount,
    long failedCount,
    long receivedCount
) {}
