package com.puppytalk.pet;

import com.puppytalk.infrastructure.common.BaseEntity;
import com.puppytalk.user.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

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
    
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;
    
    protected PetJpaEntity() {}
    
    private PetJpaEntity(Long id, Long ownerId, String name, String persona, boolean isDeleted, 
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.persona = persona;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * model -> jpa entity
     */
    public static PetJpaEntity from(Pet pet) {
        return new PetJpaEntity(
            pet.getId() != null && pet.getId().value() != null ? pet.getId().value() : null,
            pet.getOwnerId().value(),
            pet.getName(),
            pet.getPersona(),
            pet.isDeleted(),
            pet.getCreatedAt(),
            LocalDateTime.now()
        );
    }
    
    /**
     * jpa entity -> model
     */
    public Pet toDomain() {
        return Pet.of(
            PetId.from(this.id),
            UserId.from(this.ownerId),
            this.name,
            this.persona,
            this.createdAt,
            this.isDeleted
        );
    }
    
    /**
     * updateFromDomain 패턴 - 도메인 객체로부터 일괄 업데이트
     * 개별 setter 사용을 방지하여 불변성 보장
     */
    public void update(Pet pet) {
        this.name = pet.getName();
        this.persona = pet.getPersona();
        this.isDeleted = pet.isDeleted();
        this.updatedAt = LocalDateTime.now();
    }
    
    // getter
    public Long getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getPersona() { return persona; }
    public boolean isDeleted() { return isDeleted; }
    
    @Override
    protected Object getId() {
        return id;
    }
    
    public Long getPetId() {
        return id;
    }
}
