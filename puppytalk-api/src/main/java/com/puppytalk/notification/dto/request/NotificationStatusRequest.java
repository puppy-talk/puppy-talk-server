package com.puppytalk.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 알림 상태 업데이트 요청
 */
@Schema(description = "알림 상태 업데이트 요청")
public record NotificationStatusRequest(
    
    @Schema(description = "상태", example = "SENT", 
           allowableValues = {"SENT", "READ", "FAILED"}, 
           required = true)
    @NotBlank(message = "상태는 필수입니다")
    String status,
    
    @Schema(description = "실패 사유 (FAILED 상태일 때만)", example = "네트워크 오류")
    String failureReason
    
) {}