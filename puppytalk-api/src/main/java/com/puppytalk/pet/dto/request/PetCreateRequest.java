package com.puppytalk.pet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 반려동물 생성 요청 DTO
 */
public record PetCreateRequest(
    @NotNull(message = "사용자 ID는 필수입니다")
    Long ownerId,
    
    @NotBlank(message = "반려동물 이름은 필수입니다")
    @Size(max = 20, message = "반려동물 이름은 20자를 초과할 수 없습니다")
    String name,
    
    @NotNull(message = "페르소나 ID는 필수입니다")
    Long personaId
) {}