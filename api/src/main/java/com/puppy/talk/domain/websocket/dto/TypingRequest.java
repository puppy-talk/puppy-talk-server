package com.puppy.talk.domain.websocket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 타이핑 상태 요청 DTO
 */
@Schema(description = "WebSocket 타이핑 상태 알림 요청")
public record TypingRequest(
    @Schema(description = "사용자 ID", example = "1", minimum = "1")
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    Long userId,
    
    @Schema(description = "타이핑 상태", example = "true")
    boolean isTyping
) {}
