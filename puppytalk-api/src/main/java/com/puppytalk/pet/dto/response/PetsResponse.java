package com.puppytalk.pet.dto.response;

import java.util.List;

/**
 * 반려동물 목록 응답 DTO
 */
public record PetsResponse(
    List<PetResponse> pets,
    int totalCount
) {
    
    /**
     * PetsResult로부터 응답 DTO 생성
     */
    public static PetsResponse from(PetsResult petsResult) {
        List<PetResponse> petResponses = petsResult.pets().stream()
                .map(PetResponse::from)
                .toList();
        
        return new PetsResponse(petResponses, petResponses.size());
    }
}