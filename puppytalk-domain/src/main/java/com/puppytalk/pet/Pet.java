package com.puppytalk.pet;

import com.puppytalk.support.validation.Preconditions;
import com.puppytalk.user.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public class Pet {
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_PERSONA_LENGTH = 500;
    
    private final PetId id;
    private final UserId ownerId;
    private final String name;
    private final String persona;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final PetStatus status;

    private Pet(PetId id, UserId ownerId, String name, String persona,
                 LocalDateTime createdAt, LocalDateTime updatedAt, PetStatus status) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.persona = persona;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    public static Pet create(UserId ownerId, String name, String persona) {
        Preconditions.requireValidId(ownerId, "OwnerId");
        Preconditions.requireNonBlank(name, "Name", MAX_NAME_LENGTH);
        Preconditions.requireNonBlank(persona, "Persona", MAX_PERSONA_LENGTH);

        LocalDateTime now = LocalDateTime.now();

        return new Pet(
            null,
            ownerId,
            name,
            persona,
            now,
            now,
            PetStatus.ACTIVE
        );
    }

    public static Pet of(PetId id, UserId ownerId, String name, String persona,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        Preconditions.requireValidId(id, "PetId");
        Preconditions.requireValidId(ownerId, "OwnerId");
        Preconditions.requireNonBlank(name, "Name", MAX_NAME_LENGTH);
        Preconditions.requireNonBlank(persona, "Persona", MAX_PERSONA_LENGTH);
        
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt must not be null");
        }

        String validName = name.trim();
        String validPersona = persona.trim();
        return new Pet(id, ownerId, validName, validPersona, createdAt, updatedAt, PetStatus.ACTIVE);
    }

    /**
     * 이름 변경
     */
    public Pet withName(String newName) {
        Preconditions.requireNonBlank(newName, "Name", MAX_NAME_LENGTH);
        String validName = newName.trim();
        return new Pet(this.id, this.ownerId, validName, this.persona, 
                       this.createdAt, LocalDateTime.now(), this.status);
    }

    /**
     * 페르소나 변경
     */
    public Pet withPersona(String newPersona) {
        Preconditions.requireNonBlank(newPersona, "Persona", MAX_PERSONA_LENGTH);
        String validName = newPersona.trim();
        return new Pet(this.id, this.ownerId, this.name, validName, 
                       this.createdAt, LocalDateTime.now(), this.status);
    }

    /**
     * 반려동물 삭제 (소프트 삭제)
     * 이미 삭제된 반려동물은 다시 삭제할 수 없습니다.
     */
    public Pet withDeletedStatus() {
        if (this.status == PetStatus.DELETED) {
            throw new IllegalStateException("이미 삭제된 반려동물입니다");
        }
        return new Pet(id, ownerId, name, persona, createdAt, LocalDateTime.now(), PetStatus.DELETED);
    }

    public boolean canChat() {
        return status.isActive();
    }

    public boolean isOwnedBy(UserId userId) {
        return Objects.equals(this.ownerId, userId);
    }

    public PetId id() { return id; }
    public UserId ownerId() { return ownerId; }
    public String name() { return name; }
    public String persona() { return persona; }
    public LocalDateTime createdAt() { return createdAt; }
    public PetStatus status() { return status; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Pet other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}