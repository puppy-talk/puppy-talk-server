package com.puppytalk.pet;

import com.puppytalk.user.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
/**
 * 반려동물 저장소 JPA 구현체
 */
@Repository
public class PetRepositoryImpl implements PetRepository {

    private final PetJpaRepository petJpaRepository;

    public PetRepositoryImpl(PetJpaRepository petJpaRepository) {
        if (petJpaRepository == null) {
            throw new IllegalArgumentException("PetJpaRepository must not be null");
        }
        this.petJpaRepository = petJpaRepository;
    }

    @Override
    public Pet create(Pet pet) {
        Assert.notNull(pet, "Pet must not be null");
        Assert.isTrue(pet.id() == null || !pet.id().isStored(), "New pet must not have stored ID");
        
        PetJpaEntity entity = PetJpaEntity.from(pet);
        PetJpaEntity savedEntity = petJpaRepository.save(entity);
        
        return savedEntity.toDomain();
    }

    @Override
    public Pet delete(Pet pet) {
        Assert.notNull(pet, "Pet must not be null");
        Assert.notNull(pet.id(), "Pet ID must not be null for deletion");
        Assert.isTrue(pet.id().isStored(), "Pet ID must be stored for deletion");
        
        PetJpaEntity petEntity = petJpaRepository.findById(pet.id().getValue())
            .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + pet.id()));

        petEntity.update(pet);
        PetJpaEntity savedEntity = petJpaRepository.save(petEntity);
        
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Pet> findById(PetId id) {
        Assert.notNull(id, "PetId must not be null");
        
        if (!id.isStored()) {
            return Optional.empty();
        }
        
        return petJpaRepository.findById(id.getValue())
            .map(PetJpaEntity::toDomain);
    }

    @Override
    public Optional<Pet> findByIdAndOwnerId(PetId id, UserId ownerId) {
        Assert.notNull(id, "PetId must not be null");
        Assert.notNull(ownerId, "OwnerId must not be null");
        
        if (!id.isStored() || !ownerId.isStored()) {
            return Optional.empty();
        }
        
        return petJpaRepository.findByIdAndOwnerId(id.getValue(), ownerId.getValue())
            .map(PetJpaEntity::toDomain);
    }

    @Override
    public List<Pet> findByOwnerId(UserId ownerId) {
        Assert.notNull(ownerId, "OwnerId must not be null");
        
        if (!ownerId.isStored()) {
            return List.of();
        }
        
        return petJpaRepository.findByOwnerIdAndIsDeleted(ownerId.getValue(), false)
            .stream()
            .map(PetJpaEntity::toDomain)
            .toList();
    }


    @Override
    public boolean existsById(PetId id) {
        Assert.notNull(id, "PetId must not be null");
        
        if (!id.isStored()) {
            return false;
        }
        
        return petJpaRepository.existsById(id.getValue());
    }

    @Override
    public long countByOwnerId(UserId ownerId) {
        Assert.notNull(ownerId, "OwnerId must not be null");
        
        if (!ownerId.isStored()) {
            return 0;
        }
        
        return petJpaRepository.countByOwnerIdAndIsDeleted(ownerId.getValue(), false);
    }
}
