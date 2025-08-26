package com.puppytalk.pet;

import com.puppytalk.user.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 반려동물 저장소 JPA 구현체
 */
@Repository
@Transactional(readOnly = true)
public class PetRepositoryImpl implements PetRepository {

    private final PetJpaRepository petJpaRepository;

    public PetRepositoryImpl(PetJpaRepository petJpaRepository) {
        this.petJpaRepository = petJpaRepository;
    }

    @Override
    @Transactional
    public Pet create(Pet pet) {
        if (pet.id() != null && pet.id().isValid()) {
            throw new IllegalArgumentException("새로운 반려동물은 ID를 가질 수 없습니다");
        }
        
        PetJpaEntity entity = PetJpaEntity.from(pet);
        PetJpaEntity savedEntity = petJpaRepository.save(entity);
        
        return savedEntity.toDomain();
    }

    @Override
    @Transactional
    public Pet delete(Pet pet) {
        if (pet.id() == null || !pet.id().isValid()) {
            throw new IllegalArgumentException("삭제할 반려동물은 유효한 ID가 필요합니다");
        }
        
        PetJpaEntity petEntity = petJpaRepository.findById(pet.id().value())
            .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + pet.id()));

        petEntity.update(pet);
        PetJpaEntity savedEntity = petJpaRepository.save(petEntity);
        
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Pet> findById(PetId id) {
        return petJpaRepository.findById(id.value())
            .map(PetJpaEntity::toDomain);
    }

    @Override
    public Optional<Pet> findByIdAndOwnerId(PetId id, UserId ownerId) {
        return petJpaRepository.findByIdAndOwnerId(id.value(), ownerId.value())
            .map(PetJpaEntity::toDomain);
    }

    @Override
    public List<Pet> findByOwnerId(UserId ownerId) {
        return petJpaRepository.findByOwnerIdAndStatusNot(ownerId.value(), PetStatus.DELETED)
            .stream()
            .map(PetJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Pet> findActiveByOwnerId(UserId ownerId) {
        return petJpaRepository.findByOwnerIdAndStatus(ownerId.value(), PetStatus.ACTIVE)
            .stream()
            .map(PetJpaEntity::toDomain)
            .toList();
    }

    @Override
    public boolean existsById(PetId id) {
        return petJpaRepository.existsById(id.value());
    }

    @Override
    public long countByOwnerId(UserId ownerId) {
        return petJpaRepository.countByOwnerIdAndStatusNot(ownerId.value(), PetStatus.DELETED);
    }
}
