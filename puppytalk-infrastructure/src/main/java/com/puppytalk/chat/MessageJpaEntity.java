package com.puppytalk.chat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class MessageJpaEntity {

    @Id
    private Long id;
}
