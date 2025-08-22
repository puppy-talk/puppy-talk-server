package com.puppytalk.chat;

import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository {
    
    /**
     * 채팅방 저장
     */
    ChatRoom save(ChatRoom chatRoom);
    
    /**
     * ID로 채팅방 조회
     */
    Optional<ChatRoom> findById(ChatRoomId id);
    
    /**
     * 사용자와 반려동물의 채팅방 조회
     */
    Optional<ChatRoom> findByUserIdAndPetId(UserId userId, PetId petId);
    
    /**
     * 사용자의 모든 채팅방 조회
     */
    List<ChatRoom> findByUserId(UserId userId);
    
    /**
     * 반려동물의 채팅방 조회
     */
    Optional<ChatRoom> findByPetId(PetId petId);
    
    /**
     * 채팅방 존재 여부 확인
     */
    boolean existsById(ChatRoomId id);
    
    /**
     * 사용자와 반려동물의 채팅방 존재 여부 확인
     */
    boolean existsByUserIdAndPetId(UserId userId, PetId petId);
    
    /**
     * 사용자의 채팅방 개수 조회
     */
    long countByUserId(UserId userId);
}