package com.puppytalk.pet;

import com.puppytalk.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Pet 도메인 모델 테스트")
class PetTest {

    @Nested
    @DisplayName("반려동물 생성 테스트")
    class CreatePetTest {

        @Test
        @DisplayName("유효한 정보로 반려동물 생성에 성공한다")
        void createPet_WithValidInfo_Success() {
            // given
            UserId ownerId = UserId.from(1L);
            String name = "멍멍이";
            String persona = "활발하고 장난스러운 강아지";

            // when
            Pet pet = Pet.create(ownerId, name, persona);

            // then
            assertThat(pet.getOwnerId()).isEqualTo(ownerId);
            assertThat(pet.getName()).isEqualTo(name);
            assertThat(pet.getPersona()).isEqualTo(persona);
            assertThat(pet.getId()).isNull(); // 새로 생성된 반려동물은 ID가 null
            assertThat(pet.isDeleted()).isFalse();
            assertThat(pet.getCreatedAt()).isNotNull();
            assertThat(pet.getStatusName()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("ownerId가 null이면 예외가 발생한다")
        void createPet_WithNullOwnerId_ThrowsException() {
            // given
            String name = "멍멍이";
            String persona = "활발한 강아지";

            // when & then
            assertThatThrownBy(() -> Pet.create(null, name, persona))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OwnerId");
        }

        @Test
        @DisplayName("name이 null이면 예외가 발생한다")
        void createPet_WithNullName_ThrowsException() {
            // given
            UserId ownerId = UserId.from(1L);
            String persona = "활발한 강아지";

            // when & then
            assertThatThrownBy(() -> Pet.create(ownerId, null, persona))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name");
        }

        @Test
        @DisplayName("name이 빈 문자열이면 예외가 발생한다")
        void createPet_WithEmptyName_ThrowsException() {
            // given
            UserId ownerId = UserId.from(1L);
            String persona = "활발한 강아지";

            // when & then
            assertThatThrownBy(() -> Pet.create(ownerId, "", persona))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name");
        }

        @Test
        @DisplayName("name이 최대 길이를 초과하면 예외가 발생한다")
        void createPet_WithTooLongName_ThrowsException() {
            // given
            UserId ownerId = UserId.from(1L);
            String longName = "가".repeat(Pet.MAX_NAME_LENGTH + 1);
            String persona = "활발한 강아지";

            // when & then
            assertThatThrownBy(() -> Pet.create(ownerId, longName, persona))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name");
        }

        @Test
        @DisplayName("persona가 null이면 예외가 발생한다")
        void createPet_WithNullPersona_ThrowsException() {
            // given
            UserId ownerId = UserId.from(1L);
            String name = "멍멍이";

            // when & then
            assertThatThrownBy(() -> Pet.create(ownerId, name, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Persona");
        }

        @Test
        @DisplayName("persona가 빈 문자열이면 예외가 발생한다")
        void createPet_WithEmptyPersona_ThrowsException() {
            // given
            UserId ownerId = UserId.from(1L);
            String name = "멍멍이";

            // when & then
            assertThatThrownBy(() -> Pet.create(ownerId, name, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Persona");
        }
    }

    @Nested
    @DisplayName("기존 반려동물 데이터로부터 객체 생성 테스트")
    class OfPetTest {

        @Test
        @DisplayName("유효한 데이터로 반려동물 객체 생성에 성공한다")
        void ofPet_WithValidData_Success() {
            // given
            PetId petId = PetId.from(1L);
            UserId ownerId = UserId.from(1L);
            String name = "멍멍이";
            String persona = "활발한 강아지";
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
            boolean isDeleted = false;

            // when
            Pet pet = Pet.of(petId, ownerId, name, persona, createdAt, isDeleted);

            // then
            assertThat(pet.getId()).isEqualTo(petId);
            assertThat(pet.getOwnerId()).isEqualTo(ownerId);
            assertThat(pet.getName()).isEqualTo(name);
            assertThat(pet.getPersona()).isEqualTo(persona);
            assertThat(pet.getCreatedAt()).isEqualTo(createdAt);
            assertThat(pet.isDeleted()).isEqualTo(isDeleted);
        }

        @Test
        @DisplayName("petId가 null이면 예외가 발생한다")
        void ofPet_WithNullPetId_ThrowsException() {
            // given
            UserId ownerId = UserId.from(1L);
            String name = "멍멍이";
            String persona = "활발한 강아지";
            LocalDateTime createdAt = LocalDateTime.now();

            // when & then
            assertThatThrownBy(() -> Pet.of(null, ownerId, name, persona, createdAt, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PetId");
        }
    }

    @Nested
    @DisplayName("반려동물 정보 업데이트 테스트")
    class UpdatePetTest {

        private Pet createTestPet() {
            PetId petId = PetId.from(1L);
            UserId ownerId = UserId.from(1L);
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
            return Pet.of(petId, ownerId, "멍멍이", "활발한 강아지", createdAt, false);
        }

        @Test
        @DisplayName("이름 변경에 성공한다")
        void withName_WithValidName_Success() {
            // given
            Pet originalPet = createTestPet();
            String newName = "새로운이름";

            // when
            Pet updatedPet = originalPet.withName(newName);

            // then
            assertThat(updatedPet.getName()).isEqualTo(newName);
            // 다른 필드들은 변경되지 않음
            assertThat(updatedPet.getId()).isEqualTo(originalPet.getId());
            assertThat(updatedPet.getOwnerId()).isEqualTo(originalPet.getOwnerId());
            assertThat(updatedPet.getPersona()).isEqualTo(originalPet.getPersona());
            assertThat(updatedPet.getCreatedAt()).isEqualTo(originalPet.getCreatedAt());
            assertThat(updatedPet.isDeleted()).isEqualTo(originalPet.isDeleted());
        }

        @Test
        @DisplayName("잘못된 이름으로 변경 시 예외가 발생한다")
        void withName_WithInvalidName_ThrowsException() {
            // given
            Pet pet = createTestPet();

            // when & then
            assertThatThrownBy(() -> pet.withName(null))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> pet.withName(""))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> pet.withName("가".repeat(Pet.MAX_NAME_LENGTH + 1)))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("삭제 상태로 변경에 성공한다")
        void withDeletedStatus_Success() {
            // given
            Pet originalPet = createTestPet();

            // when
            Pet deletedPet = originalPet.withDeletedStatus();

            // then
            assertThat(deletedPet.isDeleted()).isTrue();
            assertThat(deletedPet.getStatusName()).isEqualTo("DELETED");
            // 다른 필드들은 변경되지 않음
            assertThat(deletedPet.getId()).isEqualTo(originalPet.getId());
            assertThat(deletedPet.getOwnerId()).isEqualTo(originalPet.getOwnerId());
            assertThat(deletedPet.getName()).isEqualTo(originalPet.getName());
            assertThat(deletedPet.getPersona()).isEqualTo(originalPet.getPersona());
        }

        @Test
        @DisplayName("이미 삭제된 반려동물을 다시 삭제하면 예외가 발생한다")
        void withDeletedStatus_AlreadyDeleted_ThrowsException() {
            // given
            Pet deletedPet = createTestPet().withDeletedStatus();

            // when & then
            assertThatThrownBy(() -> deletedPet.withDeletedStatus())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 삭제된 반려동물입니다");
        }
    }

    @Nested
    @DisplayName("소유자 확인 테스트")
    class OwnershipTest {

        @Test
        @DisplayName("같은 소유자 ID이면 true를 반환한다")
        void isOwnedBy_WithSameOwnerId_ReturnsTrue() {
            // given
            UserId ownerId = UserId.from(1L);
            Pet pet = Pet.create(ownerId, "멍멍이", "활발한 강아지");

            // when
            boolean result = pet.isOwnedBy(ownerId);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("다른 소유자 ID이면 false를 반환한다")
        void isOwnedBy_WithDifferentOwnerId_ReturnsFalse() {
            // given
            UserId ownerId = UserId.from(1L);
            UserId otherUserId = UserId.from(2L);
            Pet pet = Pet.create(ownerId, "멍멍이", "활발한 강아지");

            // when
            boolean result = pet.isOwnedBy(otherUserId);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null 사용자 ID와 비교하면 false를 반환한다")
        void isOwnedBy_WithNullUserId_ReturnsFalse() {
            // given
            UserId ownerId = UserId.from(1L);
            Pet pet = Pet.create(ownerId, "멍멍이", "활발한 강아지");

            // when
            boolean result = pet.isOwnedBy(null);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 및 해시코드 테스트")
    class EqualsAndHashCodeTest {

        @Test
        @DisplayName("모든 필드가 같은 반려동물은 동등하다")
        void equals_WithSameFields_ReturnsTrue() {
            // given
            PetId petId = PetId.from(1L);
            UserId ownerId = UserId.from(1L);
            String name = "멍멍이";
            String persona = "활발한 강아지";
            LocalDateTime createdAt = LocalDateTime.now();

            Pet pet1 = Pet.of(petId, ownerId, name, persona, createdAt, false);
            Pet pet2 = Pet.of(petId, ownerId, name, persona, createdAt, false);

            // when & then
            assertThat(pet1).isEqualTo(pet2);
            assertThat(pet1.hashCode()).isEqualTo(pet2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 반려동물은 동등하지 않다")
        void equals_WithDifferentId_ReturnsFalse() {
            // given
            UserId ownerId = UserId.from(1L);
            String name = "멍멍이";
            String persona = "활발한 강아지";
            LocalDateTime createdAt = LocalDateTime.now();

            Pet pet1 = Pet.of(PetId.from(1L), ownerId, name, persona, createdAt, false);
            Pet pet2 = Pet.of(PetId.from(2L), ownerId, name, persona, createdAt, false);

            // when & then
            assertThat(pet1).isNotEqualTo(pet2);
        }

        @Test
        @DisplayName("같은 객체 참조는 동등하다")
        void equals_WithSameReference_ReturnsTrue() {
            // given
            Pet pet = Pet.create(UserId.from(1L), "멍멍이", "활발한 강아지");

            // when & then
            assertThat(pet).isEqualTo(pet);
        }

        @Test
        @DisplayName("null과 비교하면 동등하지 않다")
        void equals_WithNull_ReturnsFalse() {
            // given
            Pet pet = Pet.create(UserId.from(1L), "멍멍이", "활발한 강아지");

            // when & then
            assertThat(pet).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 클래스와 비교하면 동등하지 않다")
        void equals_WithDifferentClass_ReturnsFalse() {
            // given
            Pet pet = Pet.create(UserId.from(1L), "멍멍이", "활발한 강아지");
            String otherObject = "다른 객체";

            // when & then
            assertThat(pet).isNotEqualTo(otherObject);
        }
    }

    @Nested
    @DisplayName("기타 메서드 테스트")
    class MiscellaneousTest {

        @Test
        @DisplayName("getUpdatedAt은 현재 구조에서 createdAt을 반환한다")
        void getUpdatedAt_ReturnsCreatedAt() {
            // given
            LocalDateTime createdAt = LocalDateTime.now();
            Pet pet = Pet.of(PetId.from(1L), UserId.from(1L), "멍멍이", "활발한 강아지", createdAt, false);

            // when
            LocalDateTime updatedAt = pet.getUpdatedAt();

            // then
            assertThat(updatedAt).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("활성 상태의 반려동물의 상태명은 ACTIVE이다")
        void getStatusName_ForActivePet_ReturnsActive() {
            // given
            Pet pet = Pet.create(UserId.from(1L), "멍멍이", "활발한 강아지");

            // when
            String statusName = pet.getStatusName();

            // then
            assertThat(statusName).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("삭제된 반려동물의 상태명은 DELETED이다")
        void getStatusName_ForDeletedPet_ReturnsDeleted() {
            // given
            Pet pet = Pet.create(UserId.from(1L), "멍멍이", "활발한 강아지")
                .withDeletedStatus();

            // when
            String statusName = pet.getStatusName();

            // then
            assertThat(statusName).isEqualTo("DELETED");
        }
    }
}