package com.puppytalk.pet.dto.response;

import com.puppytalk.pet.Pet;
import java.util.List;

/**
 * 반려동물 목록 결과 DTO
 */
public record PetsResult(
    List<PetResult> pets
) {
    
    /**
     * Pet 도메인 객체 목록으로부터 결과 DTO 생성
     */
    public static PetsResult from(List<Pet> pets) {
        List<PetResult> petResults = pets.stream()
                .map(PetResult::from)
                .toList();
        
        return new PetsResult(petResults);
    }
}