package com.puppy.talk.service;

import com.puppy.talk.exception.pet.PersonaNotFoundException;
import com.puppy.talk.exception.user.UserNotFoundException;
import com.puppy.talk.infrastructure.chat.ChatRoomRepository;
import com.puppy.talk.infrastructure.pet.PersonaRepository;
import com.puppy.talk.infrastructure.pet.PetRepository;
import com.puppy.talk.infrastructure.user.UserRepository;
import com.puppy.talk.model.chat.ChatRoom;
import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.pet.Pet;
import com.puppy.talk.model.pet.PetIdentity;
import com.puppy.talk.model.pet.Persona;
import com.puppy.talk.model.pet.PersonaIdentity;
import com.puppy.talk.model.user.User;
import com.puppy.talk.model.user.UserIdentity;
import com.puppy.talk.service.dto.PetRegistrationResult;
import com.puppy.talk.service.pet.PetRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetRegistrationService 단위 테스트")
class PetRegistrationServiceTest {

    @Mock
    private PetRepository petRepository;
    
    @Mock
    private ChatRoomRepository chatRoomRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PersonaRepository personaRepository;
    
    @InjectMocks
    private PetRegistrationService petRegistrationService;
    
    private UserIdentity userId;
    private PersonaIdentity personaId;
    private User mockUser;
    private Persona mockPersona;
    private Pet mockPet;
    private ChatRoom mockChatRoom;
    
    @BeforeEach
    void setUp() {
        userId = UserIdentity.of(1L);
        personaId = PersonaIdentity.of(1L);
        
        mockUser = new User(userId, "testuser", "test@example.com", "hashedpassword");
        mockPersona = new Persona(personaId, "친근한 골든리트리버", "활발하고 친근한 성격", "골든리트리버", "You are a friendly Golden Retriever...");
        
        mockPet = new Pet(
            PetIdentity.of(1L),
            userId,
            personaId,
            "멍멍이",
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        );
        
        mockChatRoom = new ChatRoom(
            ChatRoomIdentity.of(1L),
            mockPet.identity(),
            "멍멍이와의 채팅방",
            null
        );
    }
    
    @Test
    @DisplayName("성공: 유효한 사용자와 페르소나로 펫 등록")
    void registerPet_Success() {
        // Given
        when(userRepository.findByIdentity(userId)).thenReturn(Optional.of(mockUser));
        when(personaRepository.findByIdentity(personaId)).thenReturn(Optional.of(mockPersona));
        when(petRepository.save(any(Pet.class))).thenReturn(mockPet);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        
        // When
        PetRegistrationResult result = petRegistrationService.registerPet(
            userId, personaId, "멍멍이", "골든리트리버", 3, "http://example.com/image.jpg"
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.pet()).isEqualTo(mockPet);
        assertThat(result.chatRoom()).isEqualTo(mockChatRoom);
        
        verify(userRepository).findByIdentity(userId);
        verify(personaRepository).findByIdentity(personaId);
        verify(petRepository).save(any(Pet.class));
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }
    
    @Test
    @DisplayName("실패: 존재하지 않는 사용자")
    void registerPet_UserNotFound() {
        // Given
        when(userRepository.findByIdentity(userId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> 
            petRegistrationService.registerPet(
                userId, personaId, "멍멍이", "골든리트리버", 3, "http://example.com/image.jpg"
            )
        ).isInstanceOf(UserNotFoundException.class);
        
        verify(userRepository).findByIdentity(userId);
        verify(personaRepository, never()).findByIdentity(any());
        verify(petRepository, never()).save(any());
        verify(chatRoomRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("실패: 존재하지 않는 페르소나")
    void registerPet_PersonaNotFound() {
        // Given
        when(userRepository.findByIdentity(userId)).thenReturn(Optional.of(mockUser));
        when(personaRepository.findByIdentity(personaId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> 
            petRegistrationService.registerPet(
                userId, personaId, "멍멍이", "골든리트리버", 3, "http://example.com/image.jpg"
            )
        ).isInstanceOf(PersonaNotFoundException.class);
        
        verify(userRepository).findByIdentity(userId);
        verify(personaRepository).findByIdentity(personaId);
        verify(petRepository, never()).save(any());
        verify(chatRoomRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("검증: 펫 저장 시 올바른 정보가 전달되는지 확인")
    void registerPet_PetSaveWithCorrectInfo() {
        // Given
        when(userRepository.findByIdentity(userId)).thenReturn(Optional.of(mockUser));
        when(personaRepository.findByIdentity(personaId)).thenReturn(Optional.of(mockPersona));
        when(petRepository.save(any(Pet.class))).thenReturn(mockPet);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        
        // When
        petRegistrationService.registerPet(
            userId, personaId, "멍멍이", "골든리트리버", 3, "http://example.com/image.jpg"
        );
        
        // Then
        verify(petRepository).save(argThat(pet -> 
            pet.userId().equals(userId) &&
            pet.personaId().equals(personaId) &&
            pet.name().equals("멍멍이") &&
            pet.breed().equals("골든리트리버") &&
            pet.age() == 3 &&
            pet.profileImageUrl().equals("http://example.com/image.jpg")
        ));
    }
    
    @Test
    @DisplayName("검증: 채팅방 생성 시 올바른 제목이 설정되는지 확인")
    void registerPet_ChatRoomTitleGeneration() {
        // Given
        when(userRepository.findByIdentity(userId)).thenReturn(Optional.of(mockUser));
        when(personaRepository.findByIdentity(personaId)).thenReturn(Optional.of(mockPersona));
        when(petRepository.save(any(Pet.class))).thenReturn(mockPet);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        
        // When
        petRegistrationService.registerPet(
            userId, personaId, "멍멍이", "골든리트리버", 3, "http://example.com/image.jpg"
        );
        
        // Then
        verify(chatRoomRepository).save(argThat(chatRoom -> 
            chatRoom.roomName().equals("멍멍이와의 채팅방") &&
            chatRoom.petId().equals(mockPet.identity())
        ));
    }
}
