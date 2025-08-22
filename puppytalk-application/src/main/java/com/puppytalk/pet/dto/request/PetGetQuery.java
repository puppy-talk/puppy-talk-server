package com.puppytalk.pet.dto.request;

/**
 * 반려동물 단건 조회 쿼리
 */
public record PetGetQuery(
    Long petId,
    Long ownerId
) {
    public PetGetQuery {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Pet ID는 필수이며 양수여야 합니다");
        }
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner ID는 필수이며 양수여야 합니다");
        }
    }
    
    /**
     * API 요청으로부터 쿼리 생성
     */
    public static PetGetQuery of(Long petId, Long ownerId) {
        return new PetGetQuery(petId, ownerId);
    }
}