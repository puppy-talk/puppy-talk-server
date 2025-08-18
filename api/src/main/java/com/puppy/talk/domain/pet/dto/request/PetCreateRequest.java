package com.puppy.talk.domain.pet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "반려동물 등록 요청")
public record PetCreateRequest(
    @Schema(description = "사용자 ID", example = "1", minimum = "1")
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    Long userId,
    
    @Schema(description = "페르소나 ID", example = "1", minimum = "1")
    @NotNull(message = "Persona ID is required")
    @Positive(message = "Persona ID must be positive")
    Long personaId,
    
    @Schema(description = "반려동물 이름", example = "멀티")
    @NotBlank(message = "Pet name is required")
    @Size(min = 1, max = 20, message = "Pet name must be between 1 and 20 characters")
    String name,
    
    @Schema(description = "반려동물 품종", example = "골든 리트리버")
    @Size(max = 50, message = "Breed cannot exceed 50 characters")
    String breed,
    
    @Schema(description = "반려동물 나이 (세)", example = "3", minimum = "0", maximum = "30")
    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be non-negative")
    @Max(value = 30, message = "Age cannot exceed 30")
    Integer age,
    
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    @Pattern(regexp = "^https?://.*\\\\.(jpg|jpeg|png|gif)$", message = "Profile image URL must be a valid image URL")
    String profileImageUrl
) {

    public PetCreateRequest {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("UserId must be positive");
        }
        if (personaId == null || personaId <= 0) {
            throw new IllegalArgumentException("PersonaId must be positive");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        name = name.trim();
        if (breed != null) {
            breed = breed.trim();
        }
        if (age == null || age < 0) {
            throw new IllegalArgumentException("Age must be non-negative");
        }
        if (profileImageUrl != null) {
            profileImageUrl = profileImageUrl.trim();
        }
    }
}