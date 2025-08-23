package com.puppytalk.notification.dto.response;

import com.puppytalk.notification.Notification;

import java.util.List;

/**
 * 알림 목록 조회 결과
 */
public record NotificationListResult(
    List<NotificationResult> notifications,
    int totalCount
) {
    
    public static NotificationListResult from(List<Notification> notifications) {
        List<NotificationResult> results = notifications.stream()
                .map(NotificationResult::from)
                .toList();
                
        return new NotificationListResult(results, results.size());
    }
    
    public static NotificationListResult empty() {
        return new NotificationListResult(List.of(), 0);
    }
}