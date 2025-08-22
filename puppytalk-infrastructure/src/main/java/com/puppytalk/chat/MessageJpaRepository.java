package com.puppytalk.chat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, Long> {

}
