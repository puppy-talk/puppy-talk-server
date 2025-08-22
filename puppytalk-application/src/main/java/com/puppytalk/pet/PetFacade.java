package com.puppytalk.pet;

import com.puppytalk.pet.dto.request.PetCreateCommand;
import com.puppytalk.pet.dto.request.PetDeleteCommand;
import com.puppytalk.pet.dto.request.PetListQuery;
import com.puppytalk.pet.dto.request.PetGetQuery;
import com.puppytalk.pet.dto.response.PetResult;
import com.puppytalk.pet.dto.response.PetsResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.util.Assert;

@Service
@Transactional
public class PetFacade {
    
    private final PersonaDomainService personaDomainService;
    private final PetDomainService petDomainService;
    
    public PetFacade(
        PersonaDomainService personaDomainService,
        PetDomainService petDomainService
    ) {
        Assert.notNull(personaDomainService, "PersonaDomainService must not be null");
        Assert.notNull(petDomainService, "PetDomainService must not be null");
        
        this.personaDomainService = personaDomainService;
        this.petDomainService = petDomainService;
    }
    
    /**
     * 반려동물 생성
     */
    public void createPet(PetCreateCommand command) {
        Assert.notNull(command, "PetCreateCommand must not be null");
        Assert.notNull(command.personaId(), "PersonaId must not be null");
        Assert.notNull(command.ownerId(), "OwnerId must not be null");
        Assert.hasText(command.petName(), "Pet name must not be null or empty");
        
        PersonaId personaId = PersonaId.of(command.personaId());
        
        Persona persona = personaDomainService.findPersonaById(personaId);
        
        petDomainService.createPet(command.ownerId(), command.petName(), persona);
    }

    /**
     * 사용자의 반려동물 목록 조회
     */
    @Transactional(readOnly = true)
    public PetsResult getUserPets(PetListQuery query) {
        Assert.notNull(query, "PetListQuery must not be null");
        Assert.notNull(query.ownerId(), "OwnerId must not be null");
        
        List<Pet> pets = petDomainService.findPetsByOwnerId(query.ownerId());
        return PetsResult.from(pets);
    }

    /**
     * 반려동물 상세 조회
     */
    @Transactional(readOnly = true)
    public PetResult getPet(PetGetQuery query) {
        Assert.notNull(query, "PetGetQuery must not be null");
        Assert.notNull(query.petId(), "PetId must not be null");
        Assert.notNull(query.ownerId(), "OwnerId must not be null");
        
        PetId petId = PetId.of(query.petId());
        Long ownerId = query.ownerId();

        Pet pet = petDomainService.findPetWithOwnershipValidation(petId, ownerId);
        return PetResult.from(pet);
    }

    /**
     * 반려동물 삭제
     */
    public void deletePet(PetDeleteCommand command) {
        Assert.notNull(command, "PetDeleteCommand must not be null");
        Assert.notNull(command.petId(), "PetId must not be null");
        Assert.notNull(command.ownerId(), "OwnerId must not be null");
        
        petDomainService.deletePet(
            PetId.of(command.petId()),
            command.ownerId()
        );
    }
}