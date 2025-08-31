package com.puppytalk.unit.pet;

import com.puppytalk.pet.Pet;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pet 엔티티 단위 테스트")
class PetTest {
    
    @DisplayName("Pet 생성 - 성공")
    @Test
    void create_Success() {
        // given
        UserId ownerId = UserId.from(1L);
        String name = "버디";
        String persona = "친근하고 활발한 강아지";
        
        // when
        Pet pet = Pet.create(ownerId, name, persona);
        
        // then
        assertNotNull(pet);
        assertNull(pet.id());  // 아직 저장되지 않음
        assertEquals(ownerId, pet.ownerId());
        assertEquals(name, pet.name());
        assertEquals(persona, pet.persona());
        assertFalse(pet.isDeleted());
        assertNotNull(pet.createdAt());
        assertNotNull(pet.updatedAt());
    }
    
    @DisplayName("Pet 생성 - null OwnerId로 실패")
    @Test
    void create_NullOwnerId_ThrowsException() {
        // given
        UserId ownerId = null;
        String name = "버디";
        String persona = "친근하고 활발한 강아지";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertEquals("OwnerId must be a valid stored ID", exception.getMessage());
    }
    
    @DisplayName("Pet 생성 - null 이름으로 실패")
    @Test
    void create_NullName_ThrowsException() {
        // given
        UserId ownerId = UserId.from(1L);
        String name = null;
        String persona = "친근하고 활발한 강아지";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertEquals("Name must not be null or blank", exception.getMessage());
    }
    
    @DisplayName("Pet 생성 - 빈 이름으로 실패")
    @Test
    void create_BlankName_ThrowsException() {
        // given
        UserId ownerId = UserId.from(1L);
        String name = "   ";
        String persona = "친근하고 활발한 강아지";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertEquals("Name must not be null or blank", exception.getMessage());
    }
    
    @DisplayName("Pet 생성 - null 페르소나로 실패")
    @Test
    void create_NullPersona_ThrowsException() {
        // given
        UserId ownerId = UserId.from(1L);
        String name = "버디";
        String persona = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertEquals("Persona must not be null or blank", exception.getMessage());
    }
    
    @DisplayName("Pet 생성 - 빈 페르소나로 실패")
    @Test
    void create_BlankPersona_ThrowsException() {
        // given
        UserId ownerId = UserId.from(1L);
        String name = "버디";
        String persona = "   ";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertEquals("Persona must not be null or blank", exception.getMessage());
    }
    
    @DisplayName("Pet 생성 - 이름 길이 초과로 실패")
    @Test
    void create_NameTooLong_ThrowsException() {
        // given
        UserId ownerId = UserId.from(1L);
        String name = "a".repeat(51);  // 50자 초과
        String persona = "친근하고 활발한 강아지";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Pet.create(ownerId, name, persona)
        );
        
        assertEquals("Name length must be ≤ 50, but was 51", exception.getMessage());
    }
    
    @DisplayName("Pet 생성 - 페르소나는 길이 제한 없음")
    @Test
    void create_LongPersona_Success() {
        // given
        UserId ownerId = UserId.from(1L);
        String name = "버디";
        String persona = "a".repeat(1000);  // 긴 페르소나도 허용
        
        // when
        Pet pet = Pet.create(ownerId, name, persona);
        
        // then
        assertNotNull(pet);
        assertEquals(persona, pet.persona());
    }
    
    @DisplayName("삭제 상태로 변경 - 성공")
    @Test
    void withDeletedStatus_Success() {
        // given
        UserId ownerId = UserId.from(1L);
        Pet pet = Pet.create(ownerId, "버디", "친근하고 활발한 강아지");
        
        // when
        Pet deletedPet = pet.withDeletedStatus();
        
        // then
        assertNotEquals(pet, deletedPet);  // 불변 객체이므로 새로운 인스턴스
        assertEquals(pet.id(), deletedPet.id());
        assertEquals(pet.ownerId(), deletedPet.ownerId());
        assertEquals(pet.name(), deletedPet.name());
        assertEquals(pet.persona(), deletedPet.persona());
        assertEquals(pet.createdAt(), deletedPet.createdAt());
        assertTrue(deletedPet.isDeleted());
        assertTrue(deletedPet.updatedAt().isAfter(pet.updatedAt()) || 
                  deletedPet.updatedAt().isEqual(pet.updatedAt()));
    }
    
    @DisplayName("삭제 여부 확인 - ACTIVE 상태")
    @Test
    void isDeleted_ActiveStatus_ReturnsFalse() {
        // given
        UserId ownerId = UserId.from(1L);
        Pet pet = Pet.create(ownerId, "버디", "친근하고 활발한 강아지");
        
        // when & then
        assertFalse(pet.isDeleted());
    }
    
    @DisplayName("삭제 여부 확인 - DELETED 상태")
    @Test
    void isDeleted_DeletedStatus_ReturnsTrue() {
        // given
        UserId ownerId = UserId.from(1L);
        Pet pet = Pet.create(ownerId, "버디", "친근하고 활발한 강아지");
        Pet deletedPet = pet.withDeletedStatus();
        
        // when & then
        assertTrue(deletedPet.isDeleted());
    }
    
    @DisplayName("Pet.of 생성자 - 성공")
    @Test
    void of_Success() {
        // given
        PetId petId = PetId.from(1L);
        UserId ownerId = UserId.from(1L);
        String name = "버디";
        String persona = "친근하고 활발한 강아지";
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        boolean isDeleted = false;
        
        // when
        Pet pet = Pet.of(petId, ownerId, name, persona, createdAt, isDeleted);
        
        // then
        assertEquals(petId, pet.id());
        assertEquals(ownerId, pet.ownerId());
        assertEquals(name, pet.name());
        assertEquals(persona, pet.persona());
        assertEquals(createdAt, pet.createdAt());
        assertEquals(createdAt, pet.updatedAt()); // Pet.of에서 updatedAt 파라미터는 createdAt으로 설정됨
        assertEquals(isDeleted, pet.isDeleted());
    }
    
    @DisplayName("이름과 페르소나는 공백을 그대로 보존")
    @Test
    void create_PreservesWhitespace() {
        // given
        UserId ownerId = UserId.from(1L);
        String name = "  버디  ";
        String persona = "  친근하고 활발한 강아지  ";
        
        // when
        Pet pet = Pet.create(ownerId, name, persona);
        
        // then
        assertEquals("  버디  ", pet.name());
        assertEquals("  친근하고 활발한 강아지  ", pet.persona());
    }
}