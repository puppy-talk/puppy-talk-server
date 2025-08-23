package com.puppytalk.activity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 활동 기록 요청
 */
@Schema(description = "활동 기록 요청")
public record ActivityRecordRequest(
    
    @Schema(description = "사용자 ID", example = "1", required = true)
    @NotNull(message = "사용자 ID는 필수입니다")
    @Positive(message = "사용자 ID는 양수여야 합니다")
    Long userId,
    
    @Schema(description = "채팅방 ID (전역 활동 시 null 가능)", example = "1")
    Long chatRoomId,
    
    @Schema(description = "활동 타입", example = "MESSAGE_SENT", 
           allowableValues = {"LOGIN", "LOGOUT", "MESSAGE_SENT", "MESSAGE_READ", "CHAT_OPENED"},
           required = true)
    @NotNull(message = "활동 타입은 필수입니다")
    String activityType
    
) {}