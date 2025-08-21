package com.puppy.talk.domain.websocket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 채팅 메시지 요청 DTO
 */
@Schema(description = "WebSocket 채팅 메시지 전송 요청")
public record ChatMessageRequest(
    @Schema(description = "사용자 ID", example = "1", minimum = "1")
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    Long userId,
    
    @Schema(description = "메시지 내용", example = "안녕하세요!")
    @NotBlank(message = "Message content cannot be blank")
    @Size(max = 1000, message = "Message content cannot exceed 1000 characters")
    String content
) {}
