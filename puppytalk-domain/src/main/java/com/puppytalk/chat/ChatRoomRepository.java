package com.puppytalk.chat;

import com.puppytalk.pet.PetId;
import java.util.List;
import java.util.Optional;

/**
 * 채팅방 Repository 인터페이스
 */
public interface ChatRoomRepository {
    
    /**
     * 채팅방 저장
     */
    ChatRoom save(ChatRoom chatRoom);
    
    /**
     * ID로 채팅방 조회
     */
    Optional<ChatRoom> findById(ChatRoomId chatRoomId);
    
    /**
     * 사용자 ID로 채팅방 목록 조회
     */
    List<ChatRoom> findByUserId(Long userId);
    
    /**
     * 반려동물 ID로 채팅방 조회 (1:1 관계)
     */
    Optional<ChatRoom> findByPetId(PetId petId);
    
    /**
     * 사용자의 활성 채팅방 목록 조회
     */
    List<ChatRoom> findActiveByUserId(Long userId);
    
    /**
     * 특정 시간 이후로 활동이 없는 채팅방 조회 (알림용)
     */
    List<ChatRoom> findInactiveAfterMinutes(int minutes);
    
    /**
     * 채팅방 존재 여부 확인
     */
    boolean existsByPetId(PetId petId);
    
    /**
     * 채팅방 삭제
     */
    void deleteById(ChatRoomId chatRoomId);
}