package com.puppytalk.ai.client.dto;

import java.util.List;

public record PetPersona(
    PersonaType type,
    String name,
    String breed,
    Integer age,
    List<String> personalityTraits,
    String customInstructions
) {
    public PetPersona {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        if (name == null || name.trim().isEmpty() || name.length() > 50) {
            throw new IllegalArgumentException("Name must be non-empty and at most 50 characters");
        }
        if (breed != null && breed.length() > 50) {
            throw new IllegalArgumentException("Breed must be at most 50 characters");
        }
        if (age != null && (age < 0 || age > 30)) {
            throw new IllegalArgumentException("Age must be between 0 and 30");
        }
        if (personalityTraits != null && personalityTraits.size() > 5) {
            throw new IllegalArgumentException("PersonalityTraits must not exceed 5 items");
        }
        if (customInstructions != null && customInstructions.length() > 500) {
            throw new IllegalArgumentException("CustomInstructions must be at most 500 characters");
        }
    }
}