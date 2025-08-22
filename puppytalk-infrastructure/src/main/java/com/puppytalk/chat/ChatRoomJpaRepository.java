package com.puppytalk.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoomJpaEntity, Long> {
    
    /**
     * 사용자와 반려동물의 채팅방 조회
     */
    Optional<ChatRoomJpaEntity> findByUserIdAndPetId(Long userId, Long petId);
    
    /**
     * 사용자의 모든 채팅방 조회 (최근 메시지 시간 기준 정렬)
     */
    @Query("SELECT c FROM ChatRoomJpaEntity c WHERE c.userId = :userId ORDER BY c.lastMessageAt DESC")
    List<ChatRoomJpaEntity> findByUserIdOrderByLastMessageAtDesc(@Param("userId") Long userId);
    
    /**
     * 반려동물의 채팅방 조회
     */
    Optional<ChatRoomJpaEntity> findByPetId(Long petId);
    
    /**
     * 사용자와 반려동물의 채팅방 존재 여부 확인
     */
    boolean existsByUserIdAndPetId(Long userId, Long petId);
    
    /**
     * 사용자의 채팅방 개수 조회
     */
    long countByUserId(Long userId);
}