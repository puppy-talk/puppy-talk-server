package com.puppy.talk.dto;

import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.pet.PersonaIdentity;
import com.puppy.talk.user.UserIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PetRegistrationResult DTO 테스트")
class PetRegistrationResultTest {

    @Test
    @DisplayName("성공: PetRegistrationResult 생성 및 값 확인")
    void createPetRegistrationResult_Success() {
        // Given
        Pet pet = new Pet(
            PetIdentity.of(1L),
            UserIdentity.of(1L),
            PersonaIdentity.of(1L),
            "멍멍이",
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        );
        
        ChatRoom chatRoom = new ChatRoom(
            ChatRoomIdentity.of(1L),
            pet.identity(),
            "멍멍이와의 채팅방",
            null
        );

        // When
        PetRegistrationResult result = new PetRegistrationResult(pet, chatRoom);

        // Then
        assertThat(result.pet()).isEqualTo(pet);
        assertThat(result.chatRoom()).isEqualTo(chatRoom);
    }

    @Test
    @DisplayName("검증: PetRegistrationResult는 불변 객체임을 확인")
    void petRegistrationResult_IsImmutable() {
        // Given
        Pet pet = new Pet(
            PetIdentity.of(1L),
            UserIdentity.of(1L),
            PersonaIdentity.of(1L),
            "멍멍이",
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        );
        
        ChatRoom chatRoom = new ChatRoom(
            ChatRoomIdentity.of(1L),
            pet.identity(),
            "멍멍이와의 채팅방",
            null
        );

        PetRegistrationResult result1 = new PetRegistrationResult(pet, chatRoom);
        PetRegistrationResult result2 = new PetRegistrationResult(pet, chatRoom);

        // Then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }

    @Test
    @DisplayName("검증: Pet과 ChatRoom이 올바르게 연결됨")
    void petRegistrationResult_PetChatRoomRelation() {
        // Given
        Pet pet = new Pet(
            PetIdentity.of(1L),
            UserIdentity.of(1L),
            PersonaIdentity.of(1L),
            "멍멍이",
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        );
        
        ChatRoom chatRoom = new ChatRoom(
            ChatRoomIdentity.of(1L),
            pet.identity(),  // Pet의 identity와 연결
            "멍멍이와의 채팅방",
            null
        );

        // When
        PetRegistrationResult result = new PetRegistrationResult(pet, chatRoom);

        // Then
        assertThat(result.pet().identity()).isEqualTo(result.chatRoom().petId());
        assertThat(result.chatRoom().roomName()).contains(result.pet().name());
    }
}
