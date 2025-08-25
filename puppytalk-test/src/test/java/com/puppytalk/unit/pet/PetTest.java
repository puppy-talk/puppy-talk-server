package com.puppytalk.unit.pet;

import com.puppytalk.pet.Pet;
import com.puppytalk.pet.PetId;
import com.puppytalk.pet.PetStatus;
import com.puppytalk.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pet 도메인 엔티티 테스트")
class PetTest {
    
    @DisplayName("반려동물 생성 - 성공")
    @Test
    void create_Success() {
        // given
        UserId ownerId = UserId.of(1L);
        String name = "멍멍이";
        String persona = "활발하고 친근한 강아지";
        
        // when
        Pet pet = Pet.create(ownerId, name, persona);
        
        // then
        assertNotNull(pet);
        assertNull(pet.id()); // 아직 저장되지 않음
        assertEquals(ownerId, pet.ownerId());
        assertEquals(name, pet.name());
        assertEquals(persona, pet.persona());
        assertNotNull(pet.createdAt());
        assertEquals(PetStatus.ACTIVE, pet.status());
        assertTrue(pet.canChat());
        assertTrue(pet.isOwnedBy(ownerId));
    }
    
    @DisplayName("반려동물 생성 - null 소유자 ID로 실패")
    @Test
    void create_NullOwnerId_ThrowsException() {
        // given
        UserId ownerId = null;
        String name = "멍멍이";
        String persona = "활발하고 친근한 강아지";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertEquals("소유자 ID는 필수입니다", exception.getMessage());
    }
    
    @DisplayName("반려동물 생성 - 저장되지 않은 소유자 ID로 실패")
    @Test
    void create_NotStoredOwnerId_ThrowsException() {
        // given
        UserId ownerId = UserId.create(); // 저장되지 않은 ID
        String name = "멍멍이";
        String persona = "활발하고 친근한 강아지";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertEquals("소유자 ID는 필수입니다", exception.getMessage());
    }
    
    @DisplayName("반려동물 생성 - null 이름으로 실패")
    @Test
    void create_NullName_ThrowsException() {
        // given
        UserId ownerId = UserId.of(1L);
        String name = null;
        String persona = "활발하고 친근한 강아지";
        
        // when & then
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertTrue(exception.getMessage().contains("name"));
    }
    
    @DisplayName("반려동물 생성 - 빈 이름으로 실패")
    @Test
    void create_EmptyName_ThrowsException() {
        // given
        UserId ownerId = UserId.of(1L);
        String name = "   ";
        String persona = "활발하고 친근한 강아지";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertEquals("반려동물 이름은 필수입니다", exception.getMessage());
    }
    
    @DisplayName("반려동물 생성 - 너무 긴 이름으로 실패")
    @Test
    void create_TooLongName_ThrowsException() {
        // given
        UserId ownerId = UserId.of(1L);
        String name = "a".repeat(21); // 20자 초과
        String persona = "활발하고 친근한 강아지";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertEquals("반려동물 이름은 20자를 초과할 수 없습니다", exception.getMessage());
    }
    
    @DisplayName("반려동물 생성 - null 페르소나로 실패")
    @Test
    void create_NullPersona_ThrowsException() {
        // given
        UserId ownerId = UserId.of(1L);
        String name = "멍멍이";
        String persona = null;
        
        // when & then
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertTrue(exception.getMessage().contains("persona"));
    }
    
    @DisplayName("반려동물 생성 - 빈 페르소나로 실패")
    @Test
    void create_EmptyPersona_ThrowsException() {
        // given
        UserId ownerId = UserId.of(1L);
        String name = "멍멍이";
        String persona = "   ";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertEquals("반려동물 페르소나는 필수입니다", exception.getMessage());
    }
    
    @DisplayName("반려동물 생성 - 너무 긴 페르소나로 실패")
    @Test
    void create_TooLongPersona_ThrowsException() {
        // given
        UserId ownerId = UserId.of(1L);
        String name = "멍멍이";
        String persona = "a".repeat(501); // 500자 초과
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertEquals("반려동물 페르소나는 500자를 초과할 수 없습니다", exception.getMessage());
    }
    
    @DisplayName("반려동물 생성 - 이름과 페르소나 공백 제거")
    @Test
    void create_TrimsWhitespace() {
        // given
        UserId ownerId = UserId.of(1L);
        String name = "  멍멍이  ";
        String persona = "  활발하고 친근한 강아지  ";
        
        // when
        Pet pet = Pet.create(ownerId, name, persona);
        
        // then
        assertEquals("멍멍이", pet.name());
        assertEquals("활발하고 친근한 강아지", pet.persona());
    }
    
    @DisplayName("저장된 반려동물 객체 생성 - 성공")
    @Test
    void of_Success() {
        // given
        PetId id = PetId.of(1L);
        UserId ownerId = UserId.of(1L);
        String name = "멍멍이";
        String persona = "활발하고 친근한 강아지";
        LocalDateTime createdAt = LocalDateTime.now();
        PetStatus status = PetStatus.ACTIVE;
        
        // when
        Pet pet = Pet.of(id, ownerId, name, persona, createdAt, status);
        
        // then
        assertNotNull(pet);
        assertEquals(id, pet.id());
        assertEquals(ownerId, pet.ownerId());
        assertEquals(name, pet.name());
        assertEquals(persona, pet.persona());
        assertEquals(createdAt, pet.createdAt());
        assertEquals(status, pet.status());
    }
    
    @DisplayName("저장된 반려동물 객체 생성 - null ID로 실패")
    @Test
    void of_NullId_ThrowsException() {
        // given
        PetId id = null;
        UserId ownerId = UserId.of(1L);
        String name = "멍멍이";
        String persona = "활발하고 친근한 강아지";
        LocalDateTime createdAt = LocalDateTime.now();
        PetStatus status = PetStatus.ACTIVE;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Pet.of(id, ownerId, name, persona, createdAt, status)
        );
        
        assertEquals("저장된 반려동물 ID가 필요합니다", exception.getMessage());
    }
    
    @DisplayName("반려동물 삭제 - 성공")
    @Test
    void withDeletedStatus_Success() {
        // given
        Pet pet = Pet.of(
            PetId.of(1L),
            UserId.of(1L),
            "멍멍이",
            "활발하고 친근한 강아지",
            LocalDateTime.now(),
            PetStatus.ACTIVE
        );
        
        // when
        Pet deletedPet = pet.withDeletedStatus();
        
        // then
        assertNotNull(deletedPet);
        assertEquals(pet.id(), deletedPet.id());
        assertEquals(pet.ownerId(), deletedPet.ownerId());
        assertEquals(pet.name(), deletedPet.name());
        assertEquals(pet.persona(), deletedPet.persona());
        assertEquals(pet.createdAt(), deletedPet.createdAt());
        assertEquals(PetStatus.DELETED, deletedPet.status());
        assertFalse(deletedPet.canChat());
    }
    
    @DisplayName("반려동물 삭제 - 이미 삭제된 반려동물로 실패")
    @Test
    void withDeletedStatus_AlreadyDeleted_ThrowsException() {
        // given
        Pet deletedPet = Pet.of(
            PetId.of(1L),
            UserId.of(1L),
            "멍멍이",
            "활발하고 친근한 강아지",
            LocalDateTime.now(),
            PetStatus.DELETED
        );
        
        // when & then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> deletedPet.withDeletedStatus()
        );
        
        assertEquals("이미 삭제된 반려동물입니다", exception.getMessage());
    }
    
    @DisplayName("채팅 가능 여부 - 활성 상태일 때")
    @Test
    void canChat_ActiveStatus_ReturnsTrue() {
        // given
        Pet pet = Pet.of(
            PetId.of(1L),
            UserId.of(1L),
            "멍멍이",
            "활발하고 친근한 강아지",
            LocalDateTime.now(),
            PetStatus.ACTIVE
        );
        
        // when & then
        assertTrue(pet.canChat());
    }
    
    @DisplayName("채팅 가능 여부 - 삭제된 상태일 때")
    @Test
    void canChat_DeletedStatus_ReturnsFalse() {
        // given
        Pet pet = Pet.of(
            PetId.of(1L),
            UserId.of(1L),
            "멍멍이",
            "활발하고 친근한 강아지",
            LocalDateTime.now(),
            PetStatus.DELETED
        );
        
        // when & then
        assertFalse(pet.canChat());
    }
    
    @DisplayName("소유권 확인 - 올바른 소유자")
    @Test
    void isOwnedBy_CorrectOwner_ReturnsTrue() {
        // given
        UserId ownerId = UserId.of(1L);
        Pet pet = Pet.of(
            PetId.of(1L),
            ownerId,
            "멍멍이",
            "활발하고 친근한 강아지",
            LocalDateTime.now(),
            PetStatus.ACTIVE
        );
        
        // when & then
        assertTrue(pet.isOwnedBy(ownerId));
    }
    
    @DisplayName("소유권 확인 - 잘못된 소유자")
    @Test
    void isOwnedBy_WrongOwner_ReturnsFalse() {
        // given
        UserId ownerId = UserId.of(1L);
        UserId otherUserId = UserId.of(2L);
        Pet pet = Pet.of(
            PetId.of(1L),
            ownerId,
            "멍멍이",
            "활발하고 친근한 강아지",
            LocalDateTime.now(),
            PetStatus.ACTIVE
        );
        
        // when & then
        assertFalse(pet.isOwnedBy(otherUserId));
    }
    
    @DisplayName("equals 메서드 - 동일한 ID면 같은 객체")
    @Test
    void equals_SameId_ReturnsTrue() {
        // given
        PetId id = PetId.of(1L);
        Pet pet1 = Pet.of(id, UserId.of(1L), "pet1", "persona1", LocalDateTime.now(), PetStatus.ACTIVE);
        Pet pet2 = Pet.of(id, UserId.of(2L), "pet2", "persona2", LocalDateTime.now(), PetStatus.ACTIVE);
        
        // when & then
        assertEquals(pet1, pet2);
    }
    
    @DisplayName("equals 메서드 - 다른 ID면 다른 객체")
    @Test
    void equals_DifferentId_ReturnsFalse() {
        // given
        Pet pet1 = Pet.of(PetId.of(1L), UserId.of(1L), "pet1", "persona1", LocalDateTime.now(), PetStatus.ACTIVE);
        Pet pet2 = Pet.of(PetId.of(2L), UserId.of(1L), "pet2", "persona2", LocalDateTime.now(), PetStatus.ACTIVE);
        
        // when & then
        assertNotEquals(pet1, pet2);
    }
    
    @DisplayName("hashCode 메서드 - 동일한 ID면 같은 해시코드")
    @Test
    void hashCode_SameId_ReturnsSameHashCode() {
        // given
        PetId id = PetId.of(1L);
        Pet pet1 = Pet.of(id, UserId.of(1L), "pet1", "persona1", LocalDateTime.now(), PetStatus.ACTIVE);
        Pet pet2 = Pet.of(id, UserId.of(2L), "pet2", "persona2", LocalDateTime.now(), PetStatus.ACTIVE);
        
        // when & then
        assertEquals(pet1.hashCode(), pet2.hashCode());
    }
}