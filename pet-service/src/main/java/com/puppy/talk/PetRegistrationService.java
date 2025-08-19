package com.puppy.talk.pet;

import com.puppy.talk.dto.PetRegistrationResult;
import com.puppy.talk.chat.ChatRoomRepository;
import com.puppy.talk.user.UserRepository;
import com.puppy.talk.user.UserNotFoundException;
import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.pet.command.PetCreateCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 펫 등록 서비스
 * 
 * 펫 생성과 동시에 전용 채팅방을 생성하는 비즈니스 로직을 처리합니다.
 * 1Pet = 1Persona = 1ChatRoom 규칙을 강제합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PetRegistrationService {

    private static final String CHAT_ROOM_NAME_SUFFIX = "와의 채팅방";

    private final PetRepository petRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;

    /**
     * 새로운 펫을 등록하고 전용 채팅방을 생성합니다.
     * 
     * @param command 펫 생성 명령
     * @return 등록된 펫과 생성된 채팅방 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     */
    @Transactional
    public PetRegistrationResult createPet(PetCreateCommand command) {
        validateCommand(command);
        validateUserExists(command.userId());
        validatePersonaExists(command.personaId());

        Pet savedPet = createAndSavePet(command);
        ChatRoom savedChatRoom = createAndSaveChatRoom(savedPet);

        log.info("Successfully created pet '{}' (ID: {}) with chat room (ID: {}) for user: {}", 
            savedPet.name(), savedPet.identity().id(), savedChatRoom.identity().id(), command.userId().id());

        return new PetRegistrationResult(savedPet, savedChatRoom);
    }

    // === Helper Methods for Pet Registration ===
    
    private void validateCommand(PetCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("PetCreateCommand cannot be null");
        }
        if (command.userId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (command.personaId() == null) {
            throw new IllegalArgumentException("Persona ID cannot be null");
        }
        if (command.name() == null || command.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Pet name cannot be null or empty");
        }
    }
    
    private void validateUserExists(UserIdentity userId) {
        if (userRepository.findByIdentity(userId).isEmpty()) {
            log.warn("Pet creation failed: User not found with ID: {}", userId.id());
            throw new UserNotFoundException(userId);
        }
    }
    
    private void validatePersonaExists(PersonaIdentity personaId) {
        if (personaRepository.findByIdentity(personaId).isEmpty()) {
            log.warn("Pet creation failed: Persona not found with ID: {}", personaId.id());
            throw new PersonaNotFoundException(personaId);
        }
    }
    
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

        return petRepository.save(pet);
    }
    
    private ChatRoom createAndSaveChatRoom(Pet pet) {
        String roomTitle = pet.generateChatRoomTitle(); // 도메인 엔티티의 비즈니스 로직 사용
        
        ChatRoom newChatRoom = ChatRoom.of(
            pet.identity(),
            roomTitle,
            null // timestamp는 ChatRoom.of에서 설정됨
        );

        return chatRoomRepository.save(newChatRoom);
    }
}