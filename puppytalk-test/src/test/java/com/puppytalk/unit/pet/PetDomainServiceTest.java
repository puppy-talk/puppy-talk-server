package com.puppytalk.unit.pet;

import com.puppytalk.pet.Pet;
import com.puppytalk.pet.PetDomainService;
import com.puppytalk.pet.PetId;
import com.puppytalk.pet.PetNotFoundException;
import com.puppytalk.pet.PetRepository;
import com.puppytalk.user.UserId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PetDomainService 단위 테스트")
class PetDomainServiceTest {
    
    private PetDomainService petDomainService;
    private MockPetRepository mockRepository;
    
    @BeforeEach
    void setUp() {
        mockRepository = new MockPetRepository();
        petDomainService = new PetDomainService(mockRepository);
    }
    
    @DisplayName("반려동물 조회 - 성공")
    @Test
    void getPet_Success() {
        // given
        PetId petId = PetId.from(1L);
        UserId ownerId = UserId.from(1L);
        Pet expectedPet = Pet.of(
            petId,
            ownerId,
            "버디",
            "친근하고 활발한 강아지",
            LocalDateTime.now(),
            LocalDateTime.now(),
            false
        );
        
        mockRepository.setFindByIdAndOwnerIdResult(Optional.of(expectedPet));
        
        // when
        Pet result = petDomainService.getPet(petId, ownerId);
        
        // then
        assertEquals(expectedPet, result);
        assertTrue(mockRepository.isFindByIdAndOwnerIdCalled());
        assertEquals(petId, mockRepository.getLastFindByIdAndOwnerIdPetId());
        assertEquals(ownerId, mockRepository.getLastFindByIdAndOwnerIdOwnerId());
    }
    
    @DisplayName("반려동물 조회 - null PetId로 실패")
    @Test
    void getPet_NullPetId_ThrowsException() {
        // given
        PetId petId = null;
        UserId ownerId = UserId.from(1L);
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> petDomainService.getPet(petId, ownerId)
        );
        
        assertEquals("PetId must be a valid stored ID", exception.getMessage());
        assertFalse(mockRepository.isFindByIdAndOwnerIdCalled());
    }
    
    @DisplayName("반려동물 조회 - null OwnerId로 실패")
    @Test
    void getPet_NullOwnerId_ThrowsException() {
        // given
        PetId petId = PetId.from(1L);
        UserId ownerId = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> petDomainService.getPet(petId, ownerId)
        );
        
        assertEquals("OwnerId must be a valid stored ID", exception.getMessage());
        assertFalse(mockRepository.isFindByIdAndOwnerIdCalled());
    }
    
    @DisplayName("반려동물 조회 - 존재하지 않는 반려동물로 실패")
    @Test
    void getPet_PetNotFound_ThrowsException() {
        // given
        PetId petId = PetId.from(1L);
        UserId ownerId = UserId.from(1L);
        mockRepository.setFindByIdAndOwnerIdResult(Optional.empty());
        
        // when & then
        PetNotFoundException exception = assertThrows(
            PetNotFoundException.class,
            () -> petDomainService.getPet(petId, ownerId)
        );
        
        assertTrue(exception.getMessage().contains("1"));
        assertTrue(mockRepository.isFindByIdAndOwnerIdCalled());
    }
    
    @DisplayName("반려동물 생성 - 성공")
    @Test
    void createPet_Success() {
        // given
        UserId ownerId = UserId.from(1L);
        String petName = "버디";
        String persona = "친근하고 활발한 강아지";
        
        // when
        petDomainService.createPet(ownerId, petName, persona);
        
        // then
        assertTrue(mockRepository.isCreateCalled());
        Pet createdPet = mockRepository.getLastCreatedPet();
        assertNotNull(createdPet);
        assertEquals(ownerId, createdPet.ownerId());
        assertEquals(petName.trim(), createdPet.name());
        assertEquals(persona.trim(), createdPet.persona());
        assertFalse(createdPet.isDeleted());
    }
    
    @DisplayName("반려동물 생성 - null OwnerId로 실패")
    @Test
    void createPet_NullOwnerId_ThrowsException() {
        // given
        UserId ownerId = null;
        String petName = "버디";
        String persona = "친근하고 활발한 강아지";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> petDomainService.createPet(ownerId, petName, persona)
        );
        
        assertEquals("OwnerId must be a valid stored ID", exception.getMessage());
        assertFalse(mockRepository.isCreateCalled());
    }
    
    @DisplayName("반려동물 생성 - 빈 이름으로 실패")
    @Test
    void createPet_BlankPetName_ThrowsException() {
        // given
        UserId ownerId = UserId.from(1L);
        String petName = "   ";
        String persona = "친근하고 활발한 강아지";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> petDomainService.createPet(ownerId, petName, persona)
        );
        
        assertEquals("PetName must not be null or blank", exception.getMessage());
        assertFalse(mockRepository.isCreateCalled());
    }
    
    @DisplayName("반려동물 생성 - 빈 페르소나로 실패")
    @Test
    void createPet_BlankPersona_ThrowsException() {
        // given
        UserId ownerId = UserId.from(1L);
        String petName = "버디";
        String persona = "   ";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> petDomainService.createPet(ownerId, petName, persona)
        );
        
        assertEquals("Persona must not be null or blank", exception.getMessage());
        assertFalse(mockRepository.isCreateCalled());
    }
    
    @DisplayName("소유자의 반려동물 목록 조회 - 성공")
    @Test
    void findPetList_Success() {
        // given
        UserId ownerId = UserId.from(1L);
        List<Pet> expectedPets = Arrays.asList(
            Pet.of(PetId.from(1L), ownerId, "버디", "친근한 강아지", LocalDateTime.now(), false),
            Pet.of(PetId.from(2L), ownerId, "미미", "귀여운 고양이", LocalDateTime.now(), false)
        );
        
        mockRepository.setFindByOwnerIdResult(expectedPets);
        
        // when
        List<Pet> result = petDomainService.findPetList(ownerId);
        
        // then
        assertEquals(expectedPets, result);
        assertTrue(mockRepository.isFindByOwnerIdCalled());
        assertEquals(ownerId, mockRepository.getLastFindByOwnerIdParam());
    }
    
    @DisplayName("반려동물 삭제 - 성공")
    @Test
    void deletePet_Success() {
        // given
        PetId petId = PetId.from(1L);
        UserId ownerId = UserId.from(1L);
        Pet pet = Pet.of(petId, ownerId, "버디", "친근한 강아지", LocalDateTime.now(), false);
        
        mockRepository.setFindByIdAndOwnerIdResult(Optional.of(pet));
        
        // when
        petDomainService.deletePet(petId, ownerId);
        
        // then
        assertTrue(mockRepository.isFindByIdAndOwnerIdCalled());
        assertTrue(mockRepository.isDeleteCalled());
        
        Pet deletedPet = mockRepository.getLastDeletedPet();
        assertNotNull(deletedPet);
        assertEquals(petId, deletedPet.id());
        assertEquals(ownerId, deletedPet.ownerId());
    }
    
    @DisplayName("생성자 - null 레포지토리로 실패")
    @Test
    void constructor_NullRepository_ThrowsException() {
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PetDomainService(null)
        );
        
        assertEquals("PetRepository must not be null", exception.getMessage());
    }
    
    /**
     * Mock PetRepository 구현체
     */
    private static class MockPetRepository implements PetRepository {
        private boolean createCalled = false;
        private boolean findByIdAndOwnerIdCalled = false;
        private boolean findByOwnerIdCalled = false;
        private boolean deleteCalled = false;
        
        private Pet lastCreatedPet;
        private Pet lastDeletedPet;
        private PetId lastFindByIdAndOwnerIdPetId;
        private UserId lastFindByIdAndOwnerIdOwnerId;
        private UserId lastFindByOwnerIdParam;
        
        private Optional<Pet> findByIdAndOwnerIdResult = Optional.empty();
        private List<Pet> findByOwnerIdResult = Arrays.asList();
        
        @Override
        public Pet create(Pet pet) {
            createCalled = true;
            lastCreatedPet = pet;
            return pet;
        }
        
        @Override
        public Optional<Pet> findByIdAndOwnerId(PetId petId, UserId ownerId) {
            findByIdAndOwnerIdCalled = true;
            lastFindByIdAndOwnerIdPetId = petId;
            lastFindByIdAndOwnerIdOwnerId = ownerId;
            return findByIdAndOwnerIdResult;
        }
        
        @Override
        public List<Pet> findByOwnerId(UserId ownerId) {
            findByOwnerIdCalled = true;
            lastFindByOwnerIdParam = ownerId;
            return findByOwnerIdResult;
        }
        
        @Override
        public Pet delete(Pet pet) {
            deleteCalled = true;
            lastDeletedPet = pet;
            return pet.withDeletedStatus(); // 삭제된 상태의 Pet 반환
        }
        
        @Override
        public Optional<Pet> findById(PetId id) {
            return Optional.empty();
        }
        
        
        @Override
        public boolean existsById(PetId id) {
            return false;
        }
        
        @Override
        public long countByOwnerId(UserId ownerId) {
            return 0;
        }
        
        // Test helper methods
        public void setFindByIdAndOwnerIdResult(Optional<Pet> result) {
            this.findByIdAndOwnerIdResult = result;
        }
        
        public void setFindByOwnerIdResult(List<Pet> result) {
            this.findByOwnerIdResult = result;
        }
        
        public boolean isCreateCalled() { return createCalled; }
        public boolean isFindByIdAndOwnerIdCalled() { return findByIdAndOwnerIdCalled; }
        public boolean isFindByOwnerIdCalled() { return findByOwnerIdCalled; }
        public boolean isDeleteCalled() { return deleteCalled; }
        
        public Pet getLastCreatedPet() { return lastCreatedPet; }
        public Pet getLastDeletedPet() { return lastDeletedPet; }
        public PetId getLastFindByIdAndOwnerIdPetId() { return lastFindByIdAndOwnerIdPetId; }
        public UserId getLastFindByIdAndOwnerIdOwnerId() { return lastFindByIdAndOwnerIdOwnerId; }
        public UserId getLastFindByOwnerIdParam() { return lastFindByOwnerIdParam; }
    }
}