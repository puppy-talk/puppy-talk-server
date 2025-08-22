package com.puppytalk.pet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 반려동물 생성 요청 DTO
 */
@Schema(description = "반려동물 생성 요청")
public record PetCreateRequest(
    @Schema(description = "반려동물 소유자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "사용자 ID는 필수입니다")
    Long ownerId,
    
    @Schema(description = "반려동물 이름", example = "뽀삐", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "반려동물 이름은 필수입니다")
    @Size(max = 20, message = "반려동물 이름은 20자를 초과할 수 없습니다")
    String name,
    
    @Schema(description = "페르소나 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "페르소나 ID는 필수입니다")
    Long personaId
) {}