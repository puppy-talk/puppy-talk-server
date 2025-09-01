package com.puppytalk.pet;

import com.puppytalk.support.validation.Preconditions;
import com.puppytalk.user.UserId;
import java.time.LocalDateTime;
import java.util.Objects;

public class Pet {

    public static final int MAX_NAME_LENGTH = 50;

    private final PetId id;
    private final UserId ownerId;
    private final String name;
    private final String persona;
    private final LocalDateTime createdAt;
    private final boolean isDeleted;

    private Pet(PetId id, UserId ownerId, String name, String persona, boolean isDeleted,
        LocalDateTime createdAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.persona = persona;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
    }

    public static Pet create(
        UserId ownerId,
        String name,
        String persona
    ) {
        Preconditions.requireValidId(ownerId, "OwnerId");
        Preconditions.requireNonBlank(name, "Name", MAX_NAME_LENGTH);
        Preconditions.requireNonBlank(persona, "Persona");

        return new Pet(
            null,
            ownerId,
            name,
            persona,
            false,
            LocalDateTime.now()
        );
    }

    public static Pet of(PetId id, UserId ownerId, String name, String persona,
        LocalDateTime createdAt, boolean isDeleted) {
        Preconditions.requireValidId(id, "PetId");
        Preconditions.requireValidId(ownerId, "OwnerId");
        Preconditions.requireNonBlank(name, "Name", MAX_NAME_LENGTH);
        Preconditions.requireNonBlank(persona, "Persona");

        return new Pet(id, ownerId, name, persona, isDeleted, createdAt);
    }

    /**
     * 이름 변경
     */
    public Pet withName(String newName) {
        Preconditions.requireNonBlank(newName, "Name", MAX_NAME_LENGTH);

        return new Pet(this.id, this.ownerId, newName, this.persona, this.isDeleted, this.createdAt);
    }


    /**
     * 반려동물 삭제 (소프트 삭제) 이미 삭제된 반려동물은 다시 삭제할 수 없습니다.
     */
    public Pet withDeletedStatus() {
        if (isDeleted) {
            throw new IllegalStateException("이미 삭제된 반려동물입니다");
        }

        return new Pet(id, ownerId, name, persona, true, createdAt);
    }

    public boolean isOwnedBy(UserId userId) {
        return Objects.equals(this.getOwnerId(), userId);
    }


    public PetId getId() {
        return id;
    }

    public UserId getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getPersona() {
        return persona;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public LocalDateTime getUpdatedAt() {
        return createdAt; // 현재 구조에서는 updatedAt이 없으므로 createdAt 반환
    }

    public String getStatusName() {
        return isDeleted ? "DELETED" : "ACTIVE";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pet pet = (Pet) o;
        return isDeleted() == pet.isDeleted() && Objects.equals(getId(), pet.getId())
            && Objects.equals(getOwnerId(), pet.getOwnerId()) && Objects.equals(getName(),
            pet.getName()) && Objects.equals(getPersona(), pet.getPersona()) && Objects.equals(
            getCreatedAt(), pet.getCreatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getOwnerId(), getName(), getPersona(), getCreatedAt(), isDeleted());
    }
}