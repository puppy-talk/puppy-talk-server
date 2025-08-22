package com.puppytalk.pet;

import com.puppytalk.pet.dto.request.PetCreateCommand;
import com.puppytalk.pet.dto.request.PetDeleteCommand;
import com.puppytalk.pet.dto.request.PetGetQuery;
import com.puppytalk.pet.dto.request.PetListQuery;
import com.puppytalk.pet.dto.response.PetListResult;
import com.puppytalk.pet.dto.response.PetResult;
import com.puppytalk.user.UserId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional
public class PetFacade {
    
    private final PetDomainService petDomainService;
    
    public PetFacade(PetDomainService petDomainService) {
        this.petDomainService = petDomainService;
    }
    
    /**
     * 반려동물 생성
     */
    public void createPet(PetCreateCommand command) {
        Assert.notNull(command, "PetCreateCommand must not be null");
        
        UserId ownerId = UserId.of(command.ownerId());
        
        petDomainService.createPet(ownerId, command.petName());
    }

    /**
     * 반려동물 목록 조회
     */
    @Transactional(readOnly = true)
    public PetListResult getPetList(PetListQuery query) {
        Assert.notNull(query, "PetListQuery must not be null");

        UserId ownerId = UserId.of(query.ownerId());
        List<Pet> pets = petDomainService.findPetList(ownerId);
        return PetListResult.from(pets);
    }

    /**
     * 반려동물 상세 조회
     */
    @Transactional(readOnly = true)
    public PetResult getPet(PetGetQuery query) {
        Assert.notNull(query, "PetGetQuery must not be null");
        
        PetId petId = PetId.of(query.petId());
        UserId ownerId = UserId.of(query.ownerId());

        Pet pet = petDomainService.findPet(petId, ownerId);
        return PetResult.from(pet);
    }

    /**
     * 반려동물 삭제
     */
    public void deletePet(PetDeleteCommand command) {
        Assert.notNull(command, "PetDeleteCommand must not be null");
        
        petDomainService.deletePet(
            PetId.of(command.petId()),
            UserId.of(command.ownerId())
        );
    }
}