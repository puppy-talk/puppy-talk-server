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
    public void save(Pet pet) {
        if (pet.getId().isStored()) {
            // 기존 반려동물 업데이트
            PetJpaEntity existingEntity = petJpaRepository.findById(pet.getId().value())
                .orElseThrow(() -> new IllegalStateException("반려동물을 찾을 수 없습니다: " + pet.getId().value()));
            
            existingEntity.update(pet);
            petJpaRepository.save(existingEntity);
        } else {
            // 새로운 반려동물 생성
            PetJpaEntity entity = PetJpaEntity.from(pet);
            petJpaRepository.save(entity);
        }
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
