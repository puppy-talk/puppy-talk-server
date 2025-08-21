package com.puppy.talk.pet;

import com.puppy.talk.pet.dto.PetRegistrationResult;
import com.puppy.talk.pet.dto.PetCreateCommand;
import com.puppy.talk.pet.service.PetLookUpService;
import com.puppy.talk.pet.service.PersonaLookUpService;
import com.puppy.talk.chat.service.ChatRoomLookUpService;
import com.puppy.talk.user.UserLookUpService;
import com.puppy.talk.pet.PersonaIdentity;
import com.puppy.talk.pet.PersonaNotFoundException;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.pet.PetNotFoundException;
import com.puppy.talk.pet.Persona;
import com.puppy.talk.user.UserNotFoundException;
import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.user.UserIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Pet 도메인 Facade
 * 
 * Pet과 관련된 모든 비즈니스 로직을 통합 관리합니다.
 * - Pet 등록 (Pet + ChatRoom 자동 생성)
 * - Pet 조회 및 관리
 * - Persona 연동 조회
 * - 복합 도메인 간 상호작용 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PetFacade {

    private static final String CHAT_ROOM_NAME_SUFFIX = "와의 채팅방";

    private final PetLookUpService petLookUpService;
    private final PersonaLookUpService personaLookUpService;
    private final ChatRoomLookUpService chatRoomLookUpService;
    private final UserLookUpService userLookUpService;

    /**
     * 새로운 펫을 등록하고 전용 채팅방을 생성합니다.
     * 
     * @param command 펫 생성 명령
     * @return 등록된 펫과 생성된 채팅방 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     */
    public PetRegistrationResult createPet(PetCreateCommand command) {
        // Command validation using Spring Assert
        Assert.notNull(command, "PetCreateCommand cannot be null");
        Assert.notNull(command.userId(), "User ID cannot be null");
        Assert.notNull(command.personaId(), "Persona ID cannot be null");
        Assert.hasText(command.name(), "Pet name cannot be null or empty");

        // User existence validation
        try {
            userLookUpService.findUser(command.userId());
        } catch (UserNotFoundException e) {
            log.warn("Pet creation failed: User not found with ID: {}", command.userId().id());
            throw new IllegalArgumentException("User not found with ID: " + command.userId().id());
        }

        // Persona existence validation
        try {
            personaLookUpService.findPersona(command.personaId());
        } catch (PersonaNotFoundException e) {
            log.warn("Pet creation failed: Persona not found with ID: {}", command.personaId().id());
            throw new IllegalArgumentException("Persona not found with ID: " + command.personaId().id());
        }

        Pet savedPet = createAndSavePet(command);
        ChatRoom savedChatRoom = createAndSaveChatRoom(savedPet);

        log.info("Successfully created pet '{}' (ID: {}) with chat room (ID: {}) for user: {}", 
            savedPet.name(), savedPet.identity().id(), savedChatRoom.identity().id(), command.userId().id());

        return new PetRegistrationResult(savedPet, savedChatRoom);
    }

    // === Pet Query Operations ===

    /**
     * 펫 ID로 펫을 조회합니다.
     * 
     * @param petId 펫 식별자
     * @return 펫 정보
     */
    public Pet findPet(PetIdentity petId) {
        log.debug("Finding pet by ID: {}", petId.id());
        return petLookUpService.findPet(petId);
    }

    /**
     * 사용자의 모든 펫을 조회합니다.
     * 
     * @param userId 사용자 식별자
     * @return 사용자가 소유한 펫 목록
     */
    public List<Pet> findUserPets(UserIdentity userId) {
        log.debug("Finding pets for user: {}", userId.id());
        return petLookUpService.findPetsByUserId(userId);
    }

    /**
     * 모든 펫을 조회합니다.
     * 
     * @return 전체 펫 목록
     */
    public List<Pet> findAllPets() {
        log.debug("Finding all pets");
        return petLookUpService.findAllPets();
    }

    // === Pet Management ===

    /**
     * 펫을 삭제합니다.
     * 
     * @param petId 삭제할 펫 식별자
     */
    public void deletePet(PetIdentity petId) {
        log.info("Deleting pet: {}", petId.id());
        
        // 펫 존재 확인
        Pet pet = petLookUpService.findPet(petId);
        log.debug("Found pet to delete: {}", pet.name());
        
        // 펫 삭제 (관련 채팅방도 함께 삭제되어야 함 - 향후 구현)
        petLookUpService.deletePet(petId);
        
        log.info("Pet deleted successfully: {}", petId.id());
    }

    // === Persona Operations ===

    /**
     * 페르소나 ID로 페르소나를 조회합니다.
     * 
     * @param personaId 페르소나 식별자
     * @return 페르소나 정보
     */
    public Persona findPersona(PersonaIdentity personaId) {
        log.debug("Finding persona by ID: {}", personaId.id());
        return personaLookUpService.findPersona(personaId);
    }

    /**
     * 모든 페르소나를 조회합니다.
     * 
     * @return 전체 페르소나 목록
     */
    public List<Persona> findAllPersonas() {
        log.debug("Finding all personas");
        return personaLookUpService.findAllPersonas();
    }

    // === Combined Operations ===

    /**
     * 사용자의 펫과 해당 페르소나 정보를 함께 조회합니다.
     * 
     * @param userId 사용자 식별자
     * @return 펫과 페르소나가 결합된 정보 목록
     */
    public List<PetWithPersonaInfo> findUserPetsWithPersonas(UserIdentity userId) {
        log.debug("Finding pets with personas for user: {}", userId.id());
        
        List<Pet> pets = petLookUpService.findPetsByUserId(userId);
        
        return pets.stream()
            .map(pet -> {
                Persona persona = personaLookUpService.findPersona(pet.personaId());
                return new PetWithPersonaInfo(pet, persona);
            })
            .collect(Collectors.toList());
    }

    /**
     * 펫과 페르소나 정보를 결합한 DTO
     */
    public record PetWithPersonaInfo(
        Pet pet,
        Persona persona
    ) {}

    // === Helper Methods for Pet Registration ===
    
    private Pet createAndSavePet(PetCreateCommand command) {
        Pet pet = new Pet(
            null, // identity는 save 시 생성됨
            command.userId(),
            command.personaId(),
            command.name().trim(),
            command.breed(),
            command.age(),
            command.profileImageUrl()
        );

        return petLookUpService.createPet(pet);
    }
    
    private ChatRoom createAndSaveChatRoom(Pet pet) {
        String roomTitle = pet.generateChatRoomTitle(); // 도메인 엔티티의 비즈니스 로직 사용
        
        ChatRoom newChatRoom = ChatRoom.of(
            pet.identity(),
            roomTitle,
            null // timestamp는 ChatRoom.of에서 설정됨
        );

        return chatRoomLookUpService.createChatRoom(newChatRoom);
    }
}