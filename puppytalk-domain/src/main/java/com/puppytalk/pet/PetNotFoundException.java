package com.puppytalk.pet;

import com.puppytalk.user.UserId;

/**
 * 반려동물을 찾을 수 없는 경우의 예외
 */
public class PetNotFoundException extends RuntimeException {
    
    public PetNotFoundException(PetId petId) {
        super("반려동물을 찾을 수 없습니다. ID: " + petId.value());
    }

    public PetNotFoundException(UserId ownerId) {
        super("사용자의 반려동물을 찾을 수 없습니다. OwnerID: " + ownerId.value());
    }
    
}