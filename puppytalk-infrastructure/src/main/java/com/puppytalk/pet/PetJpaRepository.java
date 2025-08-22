package com.puppytalk.pet;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PetJpaRepository extends JpaRepository<PetJpaEntity, Long> {

}
