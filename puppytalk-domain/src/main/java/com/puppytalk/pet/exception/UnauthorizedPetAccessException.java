package com.puppytalk.pet.exception;

/**
 * 반려동물 접근 권한 예외
 * 
 * 사용자가 자신의 소유가 아닌 반려동물에 접근하려고 할 때 발생합니다.
 */
public class UnauthorizedPetAccessException extends RuntimeException {
    
    private final Long petId;
    private final Long userId;
    
    public UnauthorizedPetAccessException(Long petId, Long userId) {
        super(String.format("User %d is not authorized to access pet %d", userId, petId));
        this.petId = petId;
        this.userId = userId;
    }
    
    public Long getPetId() {
        return petId;
    }
    
    public Long getUserId() {
        return userId;
    }
}