package com.puppytalk.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 메시지 전송 요청
 * <p>
 * 메시지 내용에 대한 기본 검증을 수행합니다.
 * - 빈 문자열 및 공백 문자열 방지
 * - 길이 제한 (1-1000자)
 */
@Schema(description = "메시지 전송 요청")
public record MessageSendRequest(
    
    @Schema(description = "메시지 내용", example = "안녕하세요!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "멤시지 내용은 필수입니다")
    @Size(min = 1, max = 1000, message = "메시지 내용은 1-1000자 사이여야 합니다")
    String content
) {
    
}