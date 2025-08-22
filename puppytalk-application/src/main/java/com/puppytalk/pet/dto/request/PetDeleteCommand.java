package com.puppytalk.pet.dto.request;

/**
 * 반려동물 삭제 커맨드
 */
public record PetDeleteCommand(
    Long petId,
    Long ownerId
) {
    public PetDeleteCommand {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Pet ID는 필수이며 양수여야 합니다");
        }
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner ID는 필수이며 양수여야 합니다");
        }
    }
    
    /**
     * API 요청으로부터 커맨드 생성
     */
    public static PetDeleteCommand of(Long petId, Long ownerId) {
        return new PetDeleteCommand(petId, ownerId);
    }
}