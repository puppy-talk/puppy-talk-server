package com.puppytalk.pet;

/**
 * 반려동물을 찾을 수 없을 때 발생하는 예외
 */
public class PetNotFoundException extends RuntimeException {
    
    private final PetId petId;
    
    public PetNotFoundException(PetId petId) {
        super("반려동물을 찾을 수 없습니다. ID: " + petId);
        this.petId = petId;
    }
    
    public PetNotFoundException(PetId petId, Throwable cause) {
        super("반려동물을 찾을 수 없습니다. ID: " + petId, cause);
        this.petId = petId;
    }
    
    public PetId getPetId() {
        return petId;
    }
}