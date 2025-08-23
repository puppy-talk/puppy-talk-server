package com.puppytalk.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 사용자 활동 응답 DTO
 */
@Schema(description = "사용자 활동 응답")
public record ActivityResponse(
    
    @Schema(description = "활동 ID", example = "1")
    Long activityId,
    
    @Schema(description = "사용자 ID", example = "1")
    Long userId,
    
    @Schema(description = "채팅방 ID", example = "1")
    Long chatRoomId,
    
    @Schema(description = "활동 타입", example = "CHAT")
    String activityType,
    
    @Schema(description = "활동 시각", example = "2023-12-01T15:30:00")
    LocalDateTime activityAt,
    
    @Schema(description = "생성 시각", example = "2023-12-01T15:30:00")
    LocalDateTime createdAt,
    
    @Schema(description = "활동 발견 여부", example = "true")
    Boolean found
) {
    
    /**
     * ActivityResult로부터 응답 DTO 생성
     */
    public static ActivityResponse from(ActivityResult result) {
        return new ActivityResponse(
            result.activityId(),
            result.userId(),
            result.chatRoomId(),
            result.activityType(),
            result.activityAt(),
            result.createdAt(),
            result.found()
        );
    }
}
