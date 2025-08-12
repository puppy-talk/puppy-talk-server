package com.puppy.talk.model.pet;

import com.puppy.talk.model.user.UserIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pet 도메인 모델 테스트")
class PetTest {

    private final PetIdentity petId = PetIdentity.of(1L);
    private final UserIdentity userId = UserIdentity.of(1L);
    private final PersonaIdentity personaId = PersonaIdentity.of(1L);

    @Test
    @DisplayName("성공: 유효한 값들로 Pet 생성")
    void createPet_Success() {
        // When
        Pet pet = new Pet(
            petId,
            userId,
            personaId,
            "멍멍이",
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        );

        // Then
        assertThat(pet.identity()).isEqualTo(petId);
        assertThat(pet.userId()).isEqualTo(userId);
        assertThat(pet.personaId()).isEqualTo(personaId);
        assertThat(pet.name()).isEqualTo("멍멍이");
        assertThat(pet.breed()).isEqualTo("골든리트리버");
        assertThat(pet.age()).isEqualTo(3);
        assertThat(pet.profileImageUrl()).isEqualTo("http://example.com/image.jpg");
    }

    @Test
    @DisplayName("성공: null 값들을 허용하는 필드들 (breed, profileImageUrl)")
    void createPet_WithNullOptionalFields() {
        // When
        Pet pet = new Pet(
            petId,
            userId,
            personaId,
            "멍멍이",
            null,  // breed는 null 허용
            3,
            null   // profileImageUrl은 null 허용
        );

        // Then
        assertThat(pet.breed()).isNull();
        assertThat(pet.profileImageUrl()).isNull();
    }

    @Test
    @DisplayName("성공: 나이가 0인 경우")
    void createPet_ZeroAge() {
        // When
        Pet pet = new Pet(
            petId,
            userId,
            personaId,
            "갓난아기멍멍이",
            "푸들",
            0,
            null
        );

        // Then
        assertThat(pet.age()).isEqualTo(0);
    }

    @Test
    @DisplayName("성공: null identity로 Pet 생성 (새로운 펫인 경우)")
    void createPet_NullIdentity() {
        // When
        Pet pet = new Pet(
            null,  // identity가 null (새로운 펫)
            userId,
            personaId,
            "멍멍이",
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        );

        // Then
        assertThat(pet.identity()).isNull();
        assertThat(pet.name()).isEqualTo("멍멍이");
    }

    @Test
    @DisplayName("실패: null userId로 Pet 생성")
    void createPet_NullUserId() {
        // When & Then
        assertThatThrownBy(() -> new Pet(
            petId,
            null,  // userId가 null
            personaId,
            "멍멍이",
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("UserId cannot be null");
    }

    @Test
    @DisplayName("실패: null personaId로 Pet 생성")
    void createPet_NullPersonaId() {
        // When & Then
        assertThatThrownBy(() -> new Pet(
            petId,
            userId,
            null,  // personaId가 null
            "멍멍이",
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("PersonaId cannot be null");
    }

    @Test
    @DisplayName("실패: null name으로 Pet 생성")
    void createPet_NullName() {
        // When & Then
        assertThatThrownBy(() -> new Pet(
            petId,
            userId,
            personaId,
            null,  // name이 null
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Name cannot be null or empty");
    }

    @Test
    @DisplayName("실패: 빈 문자열 name으로 Pet 생성")
    void createPet_EmptyName() {
        // When & Then
        assertThatThrownBy(() -> new Pet(
            petId,
            userId,
            personaId,
            "",  // name이 빈 문자열
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Name cannot be null or empty");
    }

    @Test
    @DisplayName("실패: 공백만 있는 name으로 Pet 생성")
    void createPet_WhitespaceOnlyName() {
        // When & Then
        assertThatThrownBy(() -> new Pet(
            petId,
            userId,
            personaId,
            "   ",  // name이 공백만
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Name cannot be null or empty");
    }

    @Test
    @DisplayName("실패: 음수 나이로 Pet 생성")
    void createPet_NegativeAge() {
        // When & Then
        assertThatThrownBy(() -> new Pet(
            petId,
            userId,
            personaId,
            "멍멍이",
            "골든리트리버",
            -1,  // 음수 나이
            "http://example.com/image.jpg"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Age cannot be negative");
    }

    @Test
    @DisplayName("검증: Pet은 불변 객체임을 확인")
    void pet_IsImmutable() {
        // Given
        Pet pet1 = new Pet(petId, userId, personaId, "멍멍이", "골든리트리버", 3, "url");
        Pet pet2 = new Pet(petId, userId, personaId, "멍멍이", "골든리트리버", 3, "url");

        // Then
        assertThat(pet1).isEqualTo(pet2);
        assertThat(pet1.hashCode()).isEqualTo(pet2.hashCode());
    }
}
