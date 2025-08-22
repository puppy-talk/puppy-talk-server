package com.puppytalk.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 메시지 전송 요청
 */
@Schema(description = "메시지 전송 요청")
public record MessageSendRequest(
    
    @Schema(description = "메시지 내용", example = "안녕하세요!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "메시지 내용은 필수입니다")
    @Size(max = 1000, message = "메시지 내용은 1000자를 초과할 수 없습니다")
    String content
) {
}