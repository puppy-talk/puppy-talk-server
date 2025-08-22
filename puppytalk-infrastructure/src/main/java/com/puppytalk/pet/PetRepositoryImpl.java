package com.puppytalk.pet;

import com.puppytalk.user.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 반려동물 저장소 구현체
 * TODO: 실제 JPA 구현으로 교체 필요
 */
@Repository
public class PetRepositoryImpl implements PetRepository{

    private final PetJpaRepository petJpaRepository;

    public PetRepositoryImpl(PetJpaRepository petJpaRepository) {
        this.petJpaRepository = petJpaRepository;
    }

    @Override
    public void save(Pet pet) {
        // TODO: 실제 JPA 저장 로직 구현
    }

    @Override
    public Optional<Pet> findById(PetId id) {
        // TODO: 실제 JPA 조회 로직 구현
        return Optional.empty();
    }

    @Override
    public Optional<Pet> findByIdAndOwnerId(PetId id, UserId ownerId) {
        // TODO: 실제 JPA 조회 로직 구현 (소유권 필터링 포함)
        return Optional.empty();
    }

    @Override
    public List<Pet> findByOwnerId(UserId ownerId) {
        // TODO: 실제 JPA 조회 로직 구현
        return List.of();
    }

    @Override
    public List<Pet> findActiveByOwnerId(UserId ownerId) {
        // TODO: 실제 JPA 조회 로직 구현 (활성 상태 필터링 포함)
        return List.of();
    }

    @Override
    public boolean existsById(PetId id) {
        // TODO: 실제 JPA 존재 확인 로직 구현
        return false;
    }

    @Override
    public long countByOwnerId(UserId ownerId) {
        // TODO: 실제 JPA 개수 조회 로직 구현
        return 0;
    }

}
