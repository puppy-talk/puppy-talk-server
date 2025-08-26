package com.puppytalk.pet.exception;

import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;

public class UnauthorizedPetAccessException extends RuntimeException {
    
    public UnauthorizedPetAccessException(PetId petId, UserId userId) {
        super("반려동물 접근 권한이 없습니다. Pet: " + petId.getValue() + ", User: " + userId.getValue());
    }
}