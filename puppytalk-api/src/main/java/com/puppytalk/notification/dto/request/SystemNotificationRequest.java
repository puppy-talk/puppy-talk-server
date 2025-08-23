package com.puppytalk.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 시스템 알림 생성 요청
 */
@Schema(description = "시스템 알림 생성 요청")
public record SystemNotificationRequest(
    
    @Schema(description = "사용자 ID", example = "1", required = true)
    @NotNull(message = "사용자 ID는 필수입니다")
    @Positive(message = "사용자 ID는 양수여야 합니다")
    Long userId,
    
    @Schema(description = "알림 제목", example = "시스템 점검 안내", required = true)
    @NotBlank(message = "제목은 필수입니다")
    String title,
    
    @Schema(description = "알림 내용", example = "시스템 점검이 예정되어 있습니다.", required = true)
    @NotBlank(message = "내용은 필수입니다")
    String content
    
) {}