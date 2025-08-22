package com.puppytalk.pet.dto.response;

import com.puppytalk.pet.Pet;
import java.util.List;

public record PetListResult(
    List<PetResult> pets
) {
    
    public static PetListResult from(List<Pet> pets) {
        List<PetResult> petResults = pets.stream()
                .map(PetResult::from)
                .toList();
        
        return new PetListResult(petResults);
    }
}