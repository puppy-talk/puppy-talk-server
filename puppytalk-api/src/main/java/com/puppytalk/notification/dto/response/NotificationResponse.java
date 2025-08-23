package com.puppytalk.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
@Schema(description = "알림 응답")
public record NotificationResponse(
    
    @Schema(description = "알림 ID", example = "1")
    Long notificationId,
    
    @Schema(description = "사용자 ID", example = "1")
    Long userId,
    
    @Schema(description = "반려동물 ID", example = "1")
    Long petId,
    
    @Schema(description = "채팅방 ID", example = "1")
    Long chatRoomId,
    
    @Schema(description = "알림 타입", example = "INACTIVITY")
    String type,
    
    @Schema(description = "알림 제목", example = "반려동물이 당신을 기다리고 있어요")
    String title,
    
    @Schema(description = "알림 내용", example = "오랫동안 채팅하지 않았어요. 반려동물과 대화해보세요!")
    String content,
    
    @Schema(description = "알림 상태", example = "PENDING")
    String status,
    
    @Schema(description = "예약 발송 시각", example = "2023-12-01T15:30:00")
    LocalDateTime scheduledAt,
    
    @Schema(description = "발송 시각", example = "2023-12-01T15:30:00")
    LocalDateTime sentAt,
    
    @Schema(description = "읽음 시각", example = "2023-12-01T15:30:00")
    LocalDateTime readAt,
    
    @Schema(description = "생성 시각", example = "2023-12-01T15:30:00")
    LocalDateTime createdAt,
    
    @Schema(description = "재시도 횟수", example = "0")
    int retryCount,
    
    @Schema(description = "실패 사유", example = "네트워크 오류")
    String failureReason
) {
    
    /**
     * NotificationResult로부터 응답 DTO 생성
     */
    public static NotificationResponse from(NotificationResult result) {
        return new NotificationResponse(
            result.notificationId(),
            result.userId(),
            result.petId(),
            result.chatRoomId(),
            result.type(),
            result.title(),
            result.content(),
            result.status(),
            result.scheduledAt(),
            result.sentAt(),
            result.readAt(),
            result.createdAt(),
            result.retryCount(),
            result.failureReason()
        );
    }
}
