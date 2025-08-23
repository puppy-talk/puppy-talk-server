package com.puppytalk.pet;

import com.puppytalk.infrastructure.common.BaseEntity;
import com.puppytalk.user.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "pets")
public class PetJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    @Column(name = "name", nullable = false, length = 20)
    private String name;
    
    @Column(name = "persona", nullable = false, length = 500)
    private String persona;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PetStatus status;
    
    protected PetJpaEntity() {}
    
    private PetJpaEntity(Long id, Long ownerId, String name, String persona, PetStatus status, 
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.persona = persona;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public static PetJpaEntity from(Pet pet) {
        return new PetJpaEntity(
            pet.id().isStored() ? pet.id().value() : null,
            pet.ownerId().value(),
            pet.name(),
            pet.persona(),
            pet.status(),
            pet.createdAt(),
            LocalDateTime.now()
        );
    }
    
    /**
     * JPA 엔티티를 도메인 객체로 변환
     */
    public Pet toDomain() {
        return Pet.of(
            PetId.of(this.id),
            UserId.of(this.ownerId),
            this.name,
            this.persona,
            this.createdAt,
            this.status
        );
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getPersona() { return persona; }
    public PetStatus getStatus() { return status; }
    
    public void update(Pet pet) {
        this.name = pet.name();
        this.persona = pet.persona();
        this.status = pet.status();
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PetJpaEntity other)) return false;
        return Objects.equals(id, other.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
