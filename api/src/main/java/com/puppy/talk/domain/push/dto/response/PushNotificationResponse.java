package com.puppy.talk.push.dto.response;

import com.puppy.talk.push.NotificationType;
import com.puppy.talk.push.PushNotification;
import com.puppy.talk.push.PushNotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 푸시 알림 응답 DTO
 */
@Schema(description = "푸시 알림 응답")
public record PushNotificationResponse(
    
    @Schema(description = "알림 ID", example = "1")
    Long id,
    
    @Schema(description = "알림 타입", example = "INACTIVITY_MESSAGE")
    NotificationType notificationType,
    
    @Schema(description = "제목", example = "멍멍이가 보고 싶어해요!")
    String title,
    
    @Schema(description = "메시지", example = "안녕! 오랜만이야~ 뭐하고 있었어?")
    String message,
    
    @Schema(description = "추가 데이터", example = "{\"petId\":1,\"petName\":\"멍멍이\"}")
    String data,
    
    @Schema(description = "상태", example = "SENT")
    PushNotificationStatus status,
    
    @Schema(description = "오류 메시지")
    String errorMessage,
    
    @Schema(description = "예약 시간", example = "2024-01-01T10:30:00")
    LocalDateTime scheduledAt,
    
    @Schema(description = "전송 시간", example = "2024-01-01T10:31:00")
    LocalDateTime sentAt,
    
    @Schema(description = "생성 시간", example = "2024-01-01T10:30:00")
    LocalDateTime createdAt
) {
    
    public static PushNotificationResponse from(PushNotification notification) {
        return new PushNotificationResponse(
            notification.identity() != null ? notification.identity().id() : null,
            notification.notificationType(),
            notification.title(),
            notification.message(),
            notification.data(),
            notification.status(),
            notification.errorMessage(),
            notification.scheduledAt(),
            notification.sentAt(),
            notification.createdAt()
        );
    }
}