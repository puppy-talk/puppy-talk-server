package com.puppytalk.chat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ChatJpaEntity {

    @Id
    private Long id;
}
