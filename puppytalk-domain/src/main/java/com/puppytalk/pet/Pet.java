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
    private final LocalDateTime createdAt;
    private PetStatus status;
    
    private Pet(PetId id, UserId ownerId, String name, 
                LocalDateTime createdAt, PetStatus status) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.createdAt = createdAt;
        this.status = status;
    }
    
    /**
     * 새로운 반려동물 생성 정적 팩토리 메서드
     */
    public static Pet create(
        UserId ownerId,
        String name
    ) {
        validateCreation(ownerId, name);
        
        return new Pet(
            PetId.newPet(),
            ownerId,
            name.trim(),
            LocalDateTime.now(),
            PetStatus.ACTIVE
        );
    }
    
    /**
     * 기존 반려동물 복원용 정적 팩토리 메서드 (Repository용)
     */
    public static Pet restore(PetId id, UserId ownerId, String name, 
                             LocalDateTime createdAt, PetStatus status) {
        validateRestore(id, ownerId, name, createdAt, status);
        
        return new Pet(id, ownerId, name, createdAt, status);
    }
    
    private static void validateCreation(UserId ownerId, String name) {
        if (ownerId == null || !ownerId.isStored()) {
            throw new IllegalArgumentException("소유자 ID는 필수입니다");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("반려동물 이름은 필수입니다");
        }
        if (name.trim().length() > 20) {
            throw new IllegalArgumentException("반려동물 이름은 20자를 초과할 수 없습니다");
        }
    }
    
    private static void validateRestore(PetId id, UserId ownerId, String name, 
                                       LocalDateTime createdAt, PetStatus status) {
        if (id == null || !id.isStored()) {
            throw new IllegalArgumentException("저장된 반려동물 ID가 필요합니다");
        }
        validateCreation(ownerId, name);
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시각은 필수입니다");
        }
        if (status == null) {
            throw new IllegalArgumentException("반려동물 상태는 필수입니다");
        }
    }
    
    
    /**
     * 반려동물 삭제 (소프트 삭제)
     * 이미 삭제된 반려동물은 다시 삭제할 수 없습니다.
     */
    public void delete() {
        if (this.status == PetStatus.DELETED) {
            throw new IllegalStateException("이미 삭제된 반려동물입니다");
        }
        this.status = PetStatus.DELETED;
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
    
    // Getters
    public PetId getId() { return id; }
    public UserId getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public PetStatus getStatus() { return status; }
    
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