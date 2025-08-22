package com.puppytalk.pet;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PetJpaEntity {

    @Id
    private Long id;
}
