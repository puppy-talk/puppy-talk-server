package com.puppytalk.pet.dto.request;

/**
 * 반려동물 목록 조회 쿼리
 */
public record PetListQuery(
    Long ownerId
) {
    public PetListQuery {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner ID는 필수이며 양수여야 합니다");
        }
    }
    
    /**
     * API 요청으로부터 쿼리 생성
     */
    public static PetListQuery of(Long ownerId) {
        return new PetListQuery(ownerId);
    }
}