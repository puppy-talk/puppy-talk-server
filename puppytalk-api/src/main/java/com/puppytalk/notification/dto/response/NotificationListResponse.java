package com.puppytalk.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 알림 목록 응답 DTO
 */
@Schema(description = "알림 목록 응답")
public record NotificationListResponse(
    
    @Schema(description = "알림 목록")
    List<NotificationResponse> notifications,
    
    @Schema(description = "총 알림 수", example = "10")
    int totalCount
) {
    
    /**
     * NotificationListResult로부터 응답 DTO 생성
     */
    public static NotificationListResponse from(NotificationListResult result) {
        List<NotificationResponse> notificationResponses = result.notifications().stream()
            .map(NotificationResponse::from)
            .toList();
        
        return new NotificationListResponse(
            notificationResponses,
            notificationResponses.size()
        );
    }
}
