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
@Transactional(readOnly = true)
public class PetFacade {
    
    private final PetDomainService petDomainService;
    
    public PetFacade(PetDomainService petDomainService) {
        this.petDomainService = petDomainService;
    }
    
    /**
     * 반려동물 생성
     */
    @Transactional
    public void createPet(PetCreateCommand command) {
        Assert.notNull(command, "PetCreateCommand must not be null");
        Assert.hasText(command.petName(), "PetName cannot be null or empty");
        Assert.hasText(command.persona(), "Persona cannot be null or empty");

        UserId ownerId = UserId.from(command.ownerId());
        
        petDomainService.createPet(ownerId, command.petName(), command.persona());
    }

    /**
     * 반려동물 목록 조회
     */
    public PetListResult getPetList(PetListQuery query) {
        Assert.notNull(query, "PetListQuery must not be null");

        UserId ownerId = UserId.from(query.ownerId());
        List<Pet> pets = petDomainService.findPetList(ownerId);
        return PetListResult.from(pets);
    }

    /**
     * 반려동물 상세 조회
     */
    public PetResult getPet(PetGetQuery query) {
        Assert.notNull(query, "PetGetQuery must not be null");
        Assert.notNull(query.ownerId(), "OwnerId must not be null");
        Assert.notNull(query.petId(), "PetId must not be null");

        PetId petId = PetId.from(query.petId());
        UserId ownerId = UserId.from(query.ownerId());

        Pet pet = petDomainService.findPet(petId, ownerId);
        return PetResult.from(pet);
    }

    /**
     * 반려동물 삭제
     */
    @Transactional
    public void deletePet(PetDeleteCommand command) {
        Assert.notNull(command, "PetDeleteCommand must not be null");
        Assert.notNull(command.ownerId(), "OwnerId must not be null");
        Assert.notNull(command.petId(), "PetId must not be null");

        petDomainService.deletePet(
            PetId.from(command.petId()),
            UserId.from(command.ownerId())
        );
    }
}