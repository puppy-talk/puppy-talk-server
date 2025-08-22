package com.puppytalk.pet;

import com.puppytalk.pet.exception.UnauthorizedPetAccessException;
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
    
    private final PetRepository petRepository;
    private final PersonaRepository personaRepository;
    
    public PetFacade(
        PetRepository petRepository,
        PersonaRepository personaRepository
    ) {
        Assert.notNull(petRepository, "PetRepository must not be null");
        Assert.notNull(personaRepository, "PersonaRepository must not be null");
        
        this.petRepository = petRepository;
        this.personaRepository = personaRepository;
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
        
        Persona persona = personaRepository.findById(personaId)
            .orElseThrow(() -> new PersonaNotFoundException(personaId));
        
        Pet pet = Pet.create(command.ownerId(), command.petName(), persona);
        
        petRepository.save(pet);
    }

    /**
     * 사용자의 반려동물 목록 조회
     */
    @Transactional(readOnly = true)
    public PetsResult getUserPets(PetListQuery query) {
        Assert.notNull(query, "PetListQuery must not be null");
        Assert.notNull(query.ownerId(), "OwnerId must not be null");
        
        List<Pet> pets = petRepository.findByOwnerId(query.ownerId());
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

        Pet pet = findPetByPetIdAndOwnerId(petId, ownerId);
        return PetResult.from(pet);
    }

    /**
     * 반려동물 삭제
     */
    public void deletePet(PetDeleteCommand command) {
        Assert.notNull(command, "PetDeleteCommand must not be null");
        Assert.notNull(command.petId(), "PetId must not be null");
        Assert.notNull(command.ownerId(), "OwnerId must not be null");
        
        Pet pet = findPetByPetIdAndOwnerId(
            PetId.of(command.petId()),
            command.ownerId()
        );

        pet.delete();
        petRepository.save(pet);
    }
    
    private Pet findPetByPetIdAndOwnerId(PetId petId, Long ownerId) {
        Assert.notNull(petId, "PetId must not be null");
        Assert.notNull(ownerId, "OwnerId must not be null");
        
        Pet pet = petRepository.findById(petId)
            .orElseThrow(() -> new PetNotFoundException(petId));
        
        if (pet.isOwnedBy(ownerId)) {
            return pet;
        }

        throw new UnauthorizedPetAccessException(petId.value(), ownerId);
    }
}