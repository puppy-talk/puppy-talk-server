package com.puppytalk.pet;

/**
 * 반려동물 조회 커맨드
 */
public record PetQueryCommand(
    Long petId,
    Long ownerId
) {
    public PetQueryCommand {
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
    public static PetQueryCommand of(Long petId, Long ownerId) {
        return new PetQueryCommand(petId, ownerId);
    }
}