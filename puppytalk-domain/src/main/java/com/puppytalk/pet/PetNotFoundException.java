package com.puppytalk.pet;

import com.puppytalk.support.exception.DomainException;
import com.puppytalk.user.UserId;

/**
 * 반려동물을 찾을 수 없는 경우의 예외
 */
public class PetNotFoundException extends DomainException {
    
    public PetNotFoundException(PetId petId) {
        super("반려동물을 찾을 수 없습니다. ID: " + petId.getValue());
    }

    public PetNotFoundException(UserId ownerId) {
        super("사용자의 반려동물을 찾을 수 없습니다. OwnerID: " + ownerId.getValue());
    }
    
    @Override
    public String getDomainCategory() {
        return "PET_NOT_FOUND";
    }
}