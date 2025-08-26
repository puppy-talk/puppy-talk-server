package com.puppytalk.pet;

public class PetNotFoundException extends RuntimeException {
    
    public PetNotFoundException(PetId petId) {
        super("반려동물을 찾을 수 없습니다. ID: " + petId.getValue());
    }
}