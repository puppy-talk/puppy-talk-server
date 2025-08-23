package com.puppytalk.pet;

public record PetId(Long value) {
    
    public Long getValue() {
        return value;
    }
    
    public static PetId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("반려동물 ID는 양수여야 합니다");
        }
        return new PetId(value);
    }
    
    public static PetId from(Long petId) {
        return new PetId(petId);
    }

    public boolean isValid() {
        return value != null && value > 0;
    }
}