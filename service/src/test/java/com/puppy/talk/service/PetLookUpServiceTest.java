package com.puppy.talk.service;

import com.puppy.talk.exception.pet.PetNotFoundException;
import com.puppy.talk.infrastructure.pet.PetRepository;
import com.puppy.talk.model.pet.Pet;
import com.puppy.talk.model.pet.PetIdentity;
import com.puppy.talk.model.pet.PersonaIdentity;
import com.puppy.talk.model.user.UserIdentity;
import com.puppy.talk.service.pet.PetLookUpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetLookUpService 단위 테스트")
class PetLookUpServiceTest {

    @Mock
    private PetRepository petRepository;
    
    @InjectMocks
    private PetLookUpServiceImpl petLookUpService;
    
    private PetIdentity petId;
    private UserIdentity userId;
    private PersonaIdentity personaId;
    private Pet mockPet;
    
    @BeforeEach
    void setUp() {
        petId = PetIdentity.of(1L);
        userId = UserIdentity.of(1L);
        personaId = PersonaIdentity.of(1L);
        
        mockPet = new Pet(
            petId,
            userId,
            personaId,
            "멍멍이",
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        );
    }
    
    @Test
    @DisplayName("성공: ID로 펫 조회")
    void findPet_Success() {
        // Given
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        
        // When
        Pet result = petLookUpService.findPet(petId);
        
        // Then
        assertThat(result).isEqualTo(mockPet);
        verify(petRepository).findByIdentity(petId);
    }
    
    @Test
    @DisplayName("실패: 존재하지 않는 펫 ID로 조회")
    void findPet_NotFound() {
        // Given
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> petLookUpService.findPet(petId))
            .isInstanceOf(PetNotFoundException.class);
        
        verify(petRepository).findByIdentity(petId);
    }
    
    @Test
    @DisplayName("실패: null ID로 펫 조회")
    void findPet_NullIdentity() {
        // When & Then
        assertThatThrownBy(() -> petLookUpService.findPet(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Identity cannot be null");
        
        verify(petRepository, never()).findByIdentity(any());
    }
    
    @Test
    @DisplayName("성공: 모든 펫 조회")
    void findAllPets_Success() {
        // Given
        Pet pet2 = new Pet(
            PetIdentity.of(2L),
            UserIdentity.of(2L),
            personaId,
            "야옹이",
            "페르시안",
            2,
            "http://example.com/cat.jpg"
        );
        List<Pet> mockPets = Arrays.asList(mockPet, pet2);
        when(petRepository.findAll()).thenReturn(mockPets);
        
        // When
        List<Pet> result = petLookUpService.findAllPets();
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(mockPet, pet2);
        verify(petRepository).findAll();
    }
    
    @Test
    @DisplayName("성공: 사용자 ID로 펫 조회")
    void findPetsByUserId_Success() {
        // Given
        Pet pet2 = new Pet(
            PetIdentity.of(2L),
            userId,
            PersonaIdentity.of(2L),
            "왈왈이",
            "비글",
            1,
            "http://example.com/beagle.jpg"
        );
        List<Pet> mockPets = Arrays.asList(mockPet, pet2);
        when(petRepository.findByUserId(userId)).thenReturn(mockPets);
        
        // When
        List<Pet> result = petLookUpService.findPetsByUserId(userId);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(mockPet, pet2);
        verify(petRepository).findByUserId(userId);
    }
    
    @Test
    @DisplayName("실패: null 사용자 ID로 펫 조회")
    void findPetsByUserId_NullUserId() {
        // When & Then
        assertThatThrownBy(() -> petLookUpService.findPetsByUserId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("UserId cannot be null");
        
        verify(petRepository, never()).findByUserId(any());
    }
    
    @Test
    @DisplayName("성공: 펫 생성")
    void createPet_Success() {
        // Given
        Pet newPet = new Pet(
            null,
            userId,
            personaId,
            "새로운멍멍이",
            "시베리안허스키",
            4,
            "http://example.com/husky.jpg"
        );
        when(petRepository.save(newPet)).thenReturn(mockPet);
        
        // When
        Pet result = petLookUpService.createPet(newPet);
        
        // Then
        assertThat(result).isEqualTo(mockPet);
        verify(petRepository).save(newPet);
    }
    
    @Test
    @DisplayName("실패: null 펫으로 생성 시도")
    void createPet_NullPet() {
        // When & Then
        assertThatThrownBy(() -> petLookUpService.createPet(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Pet cannot be null");
        
        verify(petRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("성공: 펫 삭제")
    void deletePet_Success() {
        // Given
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        
        // When
        petLookUpService.deletePet(petId);
        
        // Then
        verify(petRepository).findByIdentity(petId);
        verify(petRepository).deleteByIdentity(petId);
    }
    
    @Test
    @DisplayName("실패: 존재하지 않는 펫 삭제 시도")
    void deletePet_NotFound() {
        // Given
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> petLookUpService.deletePet(petId))
            .isInstanceOf(PetNotFoundException.class);
        
        verify(petRepository).findByIdentity(petId);
        verify(petRepository, never()).deleteByIdentity(any());
    }
    
    @Test
    @DisplayName("실패: null ID로 펫 삭제 시도")
    void deletePet_NullIdentity() {
        // When & Then
        assertThatThrownBy(() -> petLookUpService.deletePet(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Identity cannot be null");
        
        verify(petRepository, never()).findByIdentity(any());
        verify(petRepository, never()).deleteByIdentity(any());
    }
}
