package com.puppytalk.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 채팅방 생성 요청
 */
@Schema(description = "채팅방 생성 요청")
public record ChatRoomCreateRequest(
    
    @Schema(description = "사용자 ID (인증된 사용자로 대체됨)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Long userId,
    
    @Schema(description = "반려동물 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "반려동물 ID는 필수입니다")
    @Positive(message = "반려동물 ID는 양수여야 합니다")
    Long petId
) {
}
