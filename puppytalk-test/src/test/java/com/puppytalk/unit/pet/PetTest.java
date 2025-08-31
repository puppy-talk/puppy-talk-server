package com.puppytalk.unit.pet;

import com.puppytalk.pet.Pet;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Pet 엔티티 단위 테스트")
class PetTest {
    
    @Nested
    @DisplayName("반려동물 생성 테스트")
    class CreatePetTest {
        
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
            assertThat(pet).isNotNull();
            assertThat(pet.id()).isNull(); // 아직 저장되지 않음
            assertThat(pet.ownerId()).isEqualTo(ownerId);
            assertThat(pet.name()).isEqualTo(name);
            assertThat(pet.persona()).isEqualTo(persona);
            assertThat(pet.isDeleted()).isFalse();
            assertThat(pet.getStatusName()).isEqualTo("ACTIVE");
            assertThat(pet.createdAt()).isNotNull();
            assertThat(pet.updatedAt()).isNotNull();
        }
        
        @DisplayName("Pet 생성 - 이름과 페르소나는 공백을 그대로 보존")
        @Test
        void create_PreservesWhitespace() {
            // given
            UserId ownerId = UserId.from(1L);
            String name = "  버디  ";
            String persona = "  친근하고 활발한 강아지  ";
            
            // when
            Pet pet = Pet.create(ownerId, name, persona);
            
            // then
            assertThat(pet.name()).isEqualTo("  버디  ");
            assertThat(pet.persona()).isEqualTo("  친근하고 활발한 강아지  ");
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
            assertThat(pet.id()).isEqualTo(petId);
            assertThat(pet.ownerId()).isEqualTo(ownerId);
            assertThat(pet.name()).isEqualTo(name);
            assertThat(pet.persona()).isEqualTo(persona);
            assertThat(pet.createdAt()).isEqualTo(createdAt);
            assertThat(pet.updatedAt()).isEqualTo(createdAt); // Pet.of에서 updatedAt은 createdAt과 동일
            assertThat(pet.isDeleted()).isEqualTo(isDeleted);
        }
    }
    
    @Nested
    @DisplayName("반려동물 생성 실패 테스트")
    class CreatePetFailureTest {
        
        @DisplayName("Pet 생성 - null OwnerId로 실패")
        @Test
        void create_NullOwnerId_ThrowsException() {
            // given
            UserId ownerId = null;
            String name = "버디";
            String persona = "친근하고 활발한 강아지";
            
            // when & then
            assertThatThrownBy(() -> Pet.create(ownerId, name, persona))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OwnerId");
        }
        
        @DisplayName("Pet 생성 - 잘못된 이름으로 실패")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n", "   "})
        void create_InvalidName_ThrowsException(String invalidName) {
            // given
            UserId ownerId = UserId.from(1L);
            String persona = "친근한 강아지";
            
            // when & then
            assertThatThrownBy(() -> Pet.create(ownerId, invalidName, persona))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name");
        }
        
        @DisplayName("Pet 생성 - 잘못된 페르소나로 실패")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n", "   "})
        void create_InvalidPersona_ThrowsException(String invalidPersona) {
            // given
            UserId ownerId = UserId.from(1L);
            String name = "버디";
            
            // when & then
            assertThatThrownBy(() -> Pet.create(ownerId, name, invalidPersona))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Persona");
        }
        
        @DisplayName("Pet 생성 - 이름 길이 초과로 실패")
        @Test
        void create_NameTooLong_ThrowsException() {
            // given
            UserId ownerId = UserId.from(1L);
            String name = "a".repeat(51);  // 50자 초과
            String persona = "친근하고 활발한 강아지";
            
            // when & then
            assertThatThrownBy(() -> Pet.create(ownerId, name, persona))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("length");
        }
    }
    
    @Nested
    @DisplayName("반려동물 상태 변경 테스트")
    class PetStatusChangeTest {
        
        @DisplayName("삭제 상태로 변경 - 성공")
        @Test
        void withDeletedStatus_Success() {
            // given
            UserId ownerId = UserId.from(1L);
            Pet pet = Pet.create(ownerId, "버디", "친근하고 활발한 강아지");
            
            // when
            Pet deletedPet = pet.withDeletedStatus();
            
            // then
            assertThat(deletedPet).isNotEqualTo(pet);  // 불변 객체이므로 새로운 인스턴스
            assertThat(deletedPet.id()).isEqualTo(pet.id());
            assertThat(deletedPet.ownerId()).isEqualTo(pet.ownerId());
            assertThat(deletedPet.name()).isEqualTo(pet.name());
            assertThat(deletedPet.persona()).isEqualTo(pet.persona());
            assertThat(deletedPet.createdAt()).isEqualTo(pet.createdAt());
            assertThat(deletedPet.isDeleted()).isTrue();
            assertThat(deletedPet.getStatusName()).isEqualTo("DELETED");
        }
        
        @DisplayName("이미 삭제된 Pet 재삭제 시도 - 실패")
        @Test
        void withDeletedStatus_AlreadyDeleted_ThrowsException() {
            // given
            UserId ownerId = UserId.from(1L);
            Pet pet = Pet.create(ownerId, "버디", "친근하고 활발한 강아지");
            Pet deletedPet = pet.withDeletedStatus();
            
            // when & then
            assertThatThrownBy(deletedPet::withDeletedStatus)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 삭제된");
        }
        
        @DisplayName("이름 변경 - 성공")
        @Test
        void withName_Success() {
            // given
            UserId ownerId = UserId.from(1L);
            Pet pet = Pet.create(ownerId, "버디", "친근하고 활발한 강아지");
            String newName = "멍멍이";
            
            // when
            Pet renamedPet = pet.withName(newName);
            
            // then
            assertThat(renamedPet).isNotEqualTo(pet);  // 불변 객체이므로 새로운 인스턴스
            assertThat(renamedPet.name()).isEqualTo(newName);
            assertThat(renamedPet.id()).isEqualTo(pet.id());
            assertThat(renamedPet.ownerId()).isEqualTo(pet.ownerId());
            assertThat(renamedPet.persona()).isEqualTo(pet.persona());
            assertThat(renamedPet.createdAt()).isEqualTo(pet.createdAt());
            assertThat(renamedPet.isDeleted()).isEqualTo(pet.isDeleted());
        }
    }
    
    @Nested
    @DisplayName("반려동물 상태 조회 테스트")
    class PetStatusTest {
        
        @DisplayName("ACTIVE 상태 확인")
        @Test
        void status_ActivePet_ReturnsActive() {
            // given
            UserId ownerId = UserId.from(1L);
            Pet pet = Pet.create(ownerId, "버디", "친근하고 활발한 강아지");
            
            // when & then
            assertThat(pet.getStatusName()).isEqualTo("ACTIVE");
            assertThat(pet.isDeleted()).isFalse();
        }
        
        @DisplayName("DELETED 상태 확인")
        @Test
        void status_DeletedPet_ReturnsDeleted() {
            // given
            UserId ownerId = UserId.from(1L);
            Pet pet = Pet.create(ownerId, "버디", "친근하고 활발한 강아지");
            Pet deletedPet = pet.withDeletedStatus();
            
            // when & then
            assertThat(deletedPet.getStatusName()).isEqualTo("DELETED");
            assertThat(deletedPet.isDeleted()).isTrue();
        }
        
        @DisplayName("소유자 확인 - 성공")
        @Test
        void isOwnedBy_CorrectOwner_ReturnsTrue() {
            // given
            UserId ownerId = UserId.from(1L);
            Pet pet = Pet.create(ownerId, "버디", "친근하고 활발한 강아지");
            
            // when & then
            assertThat(pet.isOwnedBy(ownerId)).isTrue();
        }
        
        @DisplayName("소유자 확인 - 다른 소유자")
        @Test
        void isOwnedBy_DifferentOwner_ReturnsFalse() {
            // given
            UserId ownerId = UserId.from(1L);
            UserId otherOwnerId = UserId.from(2L);
            Pet pet = Pet.create(ownerId, "버디", "친근하고 활발한 강아지");
            
            // when & then
            assertThat(pet.isOwnedBy(otherOwnerId)).isFalse();
        }
    }
}