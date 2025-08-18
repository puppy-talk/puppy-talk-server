package com.puppy.talk.domain.push.dto.response;

import com.puppy.talk.notification.PushNotificationService;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 푸시 알림 통계 응답 DTO
 */
@Schema(description = "푸시 알림 통계")
public record PushStatisticsResponse(
    
    @Schema(description = "전체 알림 수", example = "1000")
    long totalCount,
    
    @Schema(description = "대기 중인 알림 수", example = "5")
    long pendingCount,
    
    @Schema(description = "전송된 알림 수", example = "950")
    long sentCount,
    
    @Schema(description = "실패한 알림 수", example = "45")
    long failedCount,
    
    @Schema(description = "수신 확인된 알림 수", example = "800")
    long receivedCount,
    
    @Schema(description = "성공률 (%)", example = "95.0")
    double successRate,
    
    @Schema(description = "수신률 (%)", example = "84.2")
    double receiptRate
) {
    
    public static PushStatisticsResponse from(PushNotificationService.NotificationStatistics stats) {
        double successRate = 0.0;
        double receiptRate = 0.0;
        
        if (stats.totalCount() > 0) {
            successRate = (double) stats.sentCount() / stats.totalCount() * 100;
        }
        
        if (stats.sentCount() > 0) {
            receiptRate = (double) stats.receivedCount() / stats.sentCount() * 100;
        }
        
        return new PushStatisticsResponse(
            stats.totalCount(),
            stats.pendingCount(),
            stats.sentCount(),
            stats.failedCount(),
            stats.receivedCount(),
            Math.round(successRate * 100.0) / 100.0,
            Math.round(receiptRate * 100.0) / 100.0
        );
    }
}