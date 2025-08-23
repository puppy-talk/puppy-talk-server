package com.puppytalk.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 비활성 사용자 알림 생성 요청
 */
@Schema(description = "비활성 사용자 알림 생성 요청")
public record InactivityNotificationRequest(
    
    @Schema(description = "사용자 ID", example = "1", required = true)
    @NotNull(message = "사용자 ID는 필수입니다")
    @Positive(message = "사용자 ID는 양수여야 합니다")
    Long userId,
    
    @Schema(description = "반려동물 ID", example = "1", required = true)
    @NotNull(message = "반려동물 ID는 필수입니다")
    @Positive(message = "반려동물 ID는 양수여야 합니다")
    Long petId,
    
    @Schema(description = "채팅방 ID", example = "1", required = true)
    @NotNull(message = "채팅방 ID는 필수입니다")
    @Positive(message = "채팅방 ID는 양수여야 합니다")
    Long chatRoomId,
    
    @Schema(description = "알림 제목", example = "멍멍이가 보고싶어해요!", required = true)
    @NotBlank(message = "제목은 필수입니다")
    String title,
    
    @Schema(description = "알림 내용", example = "오랜만이에요! 어떻게 지내셨나요?", required = true)
    @NotBlank(message = "내용은 필수입니다")
    String content
    
) {}