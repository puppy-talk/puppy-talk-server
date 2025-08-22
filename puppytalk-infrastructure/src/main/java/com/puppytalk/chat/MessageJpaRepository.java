package com.puppytalk.chat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatJpaRepository extends JpaRepository<ChatJpaEntity, Long> {

}
