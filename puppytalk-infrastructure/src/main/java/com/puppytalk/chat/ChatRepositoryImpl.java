package com.puppytalk.pet;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PetRepositoryImpl implements PetRepository{

    PetJpaRepository petJpaRepository;

    public PetRepositoryImpl(PetJpaRepository petJpaRepository) {
        this.petJpaRepository = petJpaRepository;
    }

    @Override
    public void save(Pet pet) {

    }

    @Override
    public Optional<Pet> findById(PetId id) {
        return Optional.empty();
    }

    @Override
    public List<Pet> findByOwnerId(Long ownerId) {
        return List.of();
    }

    @Override
    public List<Pet> findActiveByOwnerId(Long ownerId) {
        return List.of();
    }

    @Override
    public boolean existsById(PetId id) {
        return false;
    }

    @Override
    public long countByOwnerId(Long ownerId) {
        return 0;
    }

    @Override
    public long countByPersonaId(PersonaId personaId) {
        return 0;
    }
}
