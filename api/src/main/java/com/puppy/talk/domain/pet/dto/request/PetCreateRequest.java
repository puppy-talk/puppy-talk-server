package com.puppy.talk.pet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public record PetCreateRequest(
    @NotNull @Positive
    Long userId,
    @NotNull @Positive
    Long personaId,
    @NotBlank
    String name,
    String breed,
    @NotNull @Min(0)
    Integer age,
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