package com.puppy.talk.event;

import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.pet.PersonaIdentity;
import com.puppy.talk.user.UserIdentity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 펫 생성 이벤트
 * 
 * 새로운 펫이 생성되었을 때 발생하는 도메인 이벤트입니다.
 * 이 이벤트를 통해 채팅방 생성, 웰컴 메시지 전송 등의 부가 작업을 분리할 수 있습니다.
 */
public record PetCreatedEvent(
    String eventId,
    LocalDateTime occurredOn,
    PetIdentity petId,
    UserIdentity userId,
    PersonaIdentity personaId,
    String petName,
    String breed,
    int age
) implements DomainEvent {

    public PetCreatedEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredOn == null) {
            occurredOn = LocalDateTime.now();
        }
        if (petId == null) {
            throw new IllegalArgumentException("PetId cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (personaId == null) {
            throw new IllegalArgumentException("PersonaId cannot be null");
        }
        if (petName == null || petName.trim().isEmpty()) {
            throw new IllegalArgumentException("Pet name cannot be null or empty");
        }
    }

    /**
     * 새로운 펫 생성 이벤트를 생성합니다.
     */
    public static PetCreatedEvent of(
        PetIdentity petId,
        UserIdentity userId,
        PersonaIdentity personaId,
        String petName,
        String breed,
        int age
    ) {
        return new PetCreatedEvent(
            null, // eventId will be generated
            null, // occurredOn will be set to now
            petId,
            userId,
            personaId,
            petName,
            breed,
            age
        );
    }

    /**
     * 펫이 강아지인지 확인합니다.
     */
    public boolean isPuppy() {
        return age <= 1;
    }

    /**
     * 펫이 시니어인지 확인합니다.
     */
    public boolean isSenior() {
        return age >= 7;
    }
}