package com.puppytalk.unit.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppytalk.pet.Pet;
import com.puppytalk.pet.PetDomainService;
import com.puppytalk.pet.PetId;
import com.puppytalk.pet.PetNotFoundException;
import com.puppytalk.pet.PetRepository;
import com.puppytalk.pet.PetStatus;
import com.puppytalk.user.UserId;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    void findPet_Success() {
        // given
        PetId petId = PetId.from(1L);
        UserId ownerId = UserId.from(1L);
        Pet expectedPet = Pet.of(
            petId,
            ownerId,
            "멍멍이",
            "활발하고 친근한 강아지",
            LocalDateTime.now(),
            PetStatus.ACTIVE
        );
        
        mockRepository.setFindByIdAndOwnerIdResult(Optional.of(expectedPet));
        
        // when
        Pet result = petDomainService.findPet(petId, ownerId);
        
        // then
        assertEquals(expectedPet, result);
        assertTrue(mockRepository.isFindByIdAndOwnerIdCalled());
        assertEquals(petId, mockRepository.getLastFindByIdAndOwnerIdPetId());
        assertEquals(ownerId, mockRepository.getLastFindByIdAndOwnerIdOwnerId());
    }
    
    @DisplayName("반려동물 조회 - null PetId로 실패")
    @Test
    void findPet_NullPetId_ThrowsException() {
        // given
        PetId petId = null;
        UserId ownerId = UserId.from(1L);
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> petDomainService.findPet(petId, ownerId)
        );
        
        assertEquals("PetId must not be null", exception.getMessage());
        assertFalse(mockRepository.isFindByIdAndOwnerIdCalled());
    }
    
    @DisplayName("반려동물 조회 - null OwnerId로 실패")
    @Test
    void findPet_NullOwnerId_ThrowsException() {
        // given
        PetId petId = PetId.from(1L);
        UserId ownerId = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> petDomainService.findPet(petId, ownerId)
        );
        
        assertEquals("OwnerId must not be null", exception.getMessage());
        assertFalse(mockRepository.isFindByIdAndOwnerIdCalled());
    }
    
    @DisplayName("반려동물 조회 - 존재하지 않는 반려동물로 실패")
    @Test
    void findPet_PetNotFound_ThrowsException() {
        // given
        PetId petId = PetId.from(1L);
        UserId ownerId = UserId.from(1L);
        
        mockRepository.setFindByIdAndOwnerIdResult(Optional.empty());
        
        // when & then
        PetNotFoundException exception = assertThrows(
            PetNotFoundException.class,
            () -> petDomainService.findPet(petId, ownerId)
        );
        
        assertTrue(exception.getMessage().contains("1"));
        assertTrue(mockRepository.isFindByIdAndOwnerIdCalled());
    }
    
    @DisplayName("반려동물 생성 - 성공")
    @Test
    void createPet_Success() {
        // given
        UserId ownerId = UserId.from(1L);
        String petName = "멍멍이";
        String petPersona = "활발하고 친근한 강아지";
        Pet expectedPet = Pet.of(
            PetId.from(1L),
            ownerId,
            petName.trim(),
            petPersona.trim(),
            LocalDateTime.now(),
            PetStatus.ACTIVE
        );
        
        mockRepository.setCreateResult(expectedPet);
        
        // when
        petDomainService.createPet(ownerId, petName, petPersona);
        
        // then
        assertTrue(mockRepository.isCreateCalled());
        Pet createdPet = mockRepository.getLastCreatedPet();
        assertNotNull(createdPet);
        assertEquals(ownerId, createdPet.ownerId());
        assertEquals(petName.trim(), createdPet.name());
        assertEquals(petPersona.trim(), createdPet.persona());
        assertEquals(PetStatus.ACTIVE, createdPet.status());
    }
    
    @DisplayName("반려동물 목록 조회 - 성공")
    @Test
    void findPetList_Success() {
        // given
        UserId ownerId = UserId.from(1L);
        List<Pet> expectedPets = Arrays.asList(
            Pet.of(PetId.from(1L), ownerId, "멍멍이", "활발한 강아지", LocalDateTime.now(), PetStatus.ACTIVE),
            Pet.of(PetId.from(2L), ownerId, "야옹이", "조용한 고양이", LocalDateTime.now(), PetStatus.ACTIVE)
        );
        
        mockRepository.setFindByOwnerIdResult(expectedPets);
        
        // when
        List<Pet> result = petDomainService.findPetList(ownerId);
        
        // then
        assertEquals(expectedPets, result);
        assertTrue(mockRepository.isFindByOwnerIdCalled());
        assertEquals(ownerId, mockRepository.getLastFindByOwnerIdParam());
    }
    
    @DisplayName("반려동물 목록 조회 - null OwnerId로 실패")
    @Test
    void findPetList_NullOwnerId_ThrowsException() {
        // given
        UserId ownerId = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> petDomainService.findPetList(ownerId)
        );
        
        assertEquals("OwnerId must not be null", exception.getMessage());
        assertFalse(mockRepository.isFindByOwnerIdCalled());
    }
    
    @DisplayName("반려동물 삭제 - 성공")
    @Test
    void deletePet_Success() {
        // given
        PetId petId = PetId.from(1L);
        UserId ownerId = UserId.from(1L);
        Pet pet = Pet.of(petId, ownerId, "멍멍이", "활발한 강아지", LocalDateTime.now(), PetStatus.ACTIVE);
        Pet deletedPet = pet.withDeletedStatus();
        
        mockRepository.setFindByIdAndOwnerIdResult(Optional.of(pet));
        mockRepository.setDeleteResult(deletedPet);
        
        // when
        petDomainService.deletePet(petId, ownerId);
        
        // then
        assertTrue(mockRepository.isFindByIdAndOwnerIdCalled());
        assertTrue(mockRepository.isDeleteCalled());
        
        Pet deletedPetFromRepo = mockRepository.getLastDeletedPet();
        assertNotNull(deletedPetFromRepo);
        assertEquals(pet.id(), deletedPetFromRepo.id());
        assertEquals(pet.ownerId(), deletedPetFromRepo.ownerId());
        assertEquals(pet.name(), deletedPetFromRepo.name());
        assertEquals(pet.persona(), deletedPetFromRepo.persona());
        assertEquals(PetStatus.DELETED, deletedPetFromRepo.status());
    }
    
    @DisplayName("반려동물 삭제 - 존재하지 않는 반려동물로 실패")
    @Test
    void deletePet_PetNotFound_ThrowsException() {
        // given
        PetId petId = PetId.from(1L);
        UserId ownerId = UserId.from(1L);
        
        mockRepository.setFindByIdAndOwnerIdResult(Optional.empty());
        
        // when & then
        PetNotFoundException exception = assertThrows(
            PetNotFoundException.class,
            () -> petDomainService.deletePet(petId, ownerId)
        );
        
        assertTrue(exception.getMessage().contains("1"));
        assertTrue(mockRepository.isFindByIdAndOwnerIdCalled());
        assertFalse(mockRepository.isDeleteCalled());
    }
    
    private static class MockPetRepository implements PetRepository {
        private boolean createCalled = false;
        private boolean deleteCalled = false;
        private boolean findByIdAndOwnerIdCalled = false;
        private boolean findByOwnerIdCalled = false;
        
        private Pet lastCreatedPet;
        private Pet lastDeletedPet;
        private PetId lastFindByIdAndOwnerIdPetId;
        private UserId lastFindByIdAndOwnerIdOwnerId;
        private UserId lastFindByOwnerIdParam;
        
        private Pet createResult;
        private Pet deleteResult;
        private Optional<Pet> findByIdAndOwnerIdResult = Optional.empty();
        private List<Pet> findByOwnerIdResult = List.of();
        
        @Override
        public Pet create(Pet pet) {
            createCalled = true;
            lastCreatedPet = pet;
            return createResult != null ? createResult : pet;
        }
        
        @Override
        public Pet delete(Pet pet) {
            deleteCalled = true;
            lastDeletedPet = pet;
            return deleteResult != null ? deleteResult : pet;
        }
        
        @Override
        public Optional<Pet> findByIdAndOwnerId(PetId petId, UserId ownerId) {
            findByIdAndOwnerIdCalled = true;
            lastFindByIdAndOwnerIdPetId = petId;
            lastFindByIdAndOwnerIdOwnerId = ownerId;
            return findByIdAndOwnerIdResult;
        }
        
        @Override
        public Optional<Pet> findById(PetId id) {
            return Optional.empty();
        }
        
        @Override
        public List<Pet> findByOwnerId(UserId ownerId) {
            findByOwnerIdCalled = true;
            lastFindByOwnerIdParam = ownerId;
            return findByOwnerIdResult;
        }
        
        @Override
        public List<Pet> findActiveByOwnerId(UserId ownerId) {
            return Arrays.asList();
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
        public void setCreateResult(Pet result) { this.createResult = result; }
        public void setDeleteResult(Pet result) { this.deleteResult = result; }
        public void setFindByIdAndOwnerIdResult(Optional<Pet> result) { this.findByIdAndOwnerIdResult = result; }
        public void setFindByOwnerIdResult(List<Pet> result) { this.findByOwnerIdResult = result; }
        
        public boolean isCreateCalled() { return createCalled; }
        public boolean isDeleteCalled() { return deleteCalled; }
        public boolean isFindByIdAndOwnerIdCalled() { return findByIdAndOwnerIdCalled; }
        public boolean isFindByOwnerIdCalled() { return findByOwnerIdCalled; }
        
        public Pet getLastCreatedPet() { return lastCreatedPet; }
        public Pet getLastDeletedPet() { return lastDeletedPet; }
        public PetId getLastFindByIdAndOwnerIdPetId() { return lastFindByIdAndOwnerIdPetId; }
        public UserId getLastFindByIdAndOwnerIdOwnerId() { return lastFindByIdAndOwnerIdOwnerId; }
        public UserId getLastFindByOwnerIdParam() { return lastFindByOwnerIdParam; }
    }
}