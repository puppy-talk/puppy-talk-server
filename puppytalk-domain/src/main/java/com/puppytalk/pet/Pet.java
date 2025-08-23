package com.puppytalk.pet;

import com.puppytalk.user.UserId;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 반려동물 엔티티
 * 
 * 사용자가 생성하는 반려동물입니다.
 */
public class Pet {
    private final PetId id;
    private final UserId ownerId;
    private final String name;
    private final String persona;
    private final LocalDateTime createdAt;
    private final PetStatus status;

    private Pet(PetId id, UserId ownerId, String name, String persona,
                LocalDateTime createdAt, PetStatus status) {
        if (ownerId == null || !ownerId.isStored()) {
            throw new IllegalArgumentException("소유자 ID는 필수입니다");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("반려동물 이름은 필수입니다");
        }
        if (name.trim().length() > 20) {
            throw new IllegalArgumentException("반려동물 이름은 20자를 초과할 수 없습니다");
        }
        if (persona == null || persona.trim().isEmpty()) {
            throw new IllegalArgumentException("반려동물 페르소나는 필수입니다");
        }
        if (persona.trim().length() > 500) {
            throw new IllegalArgumentException("반려동물 페르소나는 500자를 초과할 수 없습니다");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시각은 필수입니다");
        }
        if (status == null) {
            throw new IllegalArgumentException("반려동물 상태는 필수입니다");
        }

        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.persona = persona;
        this.createdAt = createdAt;
        this.status = status;
    }

    public static Pet create(UserId ownerId, String name, String persona) {
        return new Pet(
            null,
            ownerId,
            name.trim(),
            persona.trim(),
            LocalDateTime.now(),
            PetStatus.ACTIVE
        );
    }

    public static Pet of(PetId id, UserId ownerId, String name, String persona,
                         LocalDateTime createdAt, PetStatus status) {
        if (id == null || !id.isStored()) {
            throw new IllegalArgumentException("저장된 반려동물 ID가 필요합니다");
        }

        return new Pet(id, ownerId, name, persona, createdAt, status);
    }

    /**
     * 반려동물 삭제 (소프트 삭제)
     * 이미 삭제된 반려동물은 다시 삭제할 수 없습니다.
     */
    public Pet withDeletedStatus() {
        if (this.status == PetStatus.DELETED) {
            throw new IllegalStateException("이미 삭제된 반려동물입니다");
        }
        return new Pet(id, ownerId, name, persona, createdAt, PetStatus.DELETED);
    }

    /**
     * 반려동물이 채팅 가능한 상태인지 확인
     */
    public boolean canChat() {
        return status.isActive();
    }

    /**
     * 특정 사용자의 반려동물인지 확인
     */
    public boolean isOwnedBy(UserId userId) {
        return Objects.equals(this.ownerId, userId);
    }

    // getter
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