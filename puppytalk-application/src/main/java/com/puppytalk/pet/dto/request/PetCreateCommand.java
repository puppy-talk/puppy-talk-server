package com.puppytalk.pet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PetCreateCommand(
    @NotNull(message = "소유자 ID는 필수입니다")
    Long ownerId,
    
    @NotBlank(message = "반려동물 이름은 필수입니다")
    @Size(max = 50, message = "반려동물 이름은 50자를 초과할 수 없습니다")
    String petName,
    
    @NotBlank(message = "반려동물 페르소나는 필수입니다") 
    String persona
) {
    public static PetCreateCommand of(Long ownerId, String petName, String petPersona) {
        return new PetCreateCommand(ownerId, petName, petPersona);
    }
}