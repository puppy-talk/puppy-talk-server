package com.puppytalk.chat;

import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class ChatRoomRepositoryImpl implements ChatRoomRepository {
    
    private final ChatRoomJpaRepository jpaRepository;
    
    public ChatRoomRepositoryImpl(ChatRoomJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public ChatRoom create(ChatRoom chatRoom) {
        if (chatRoom.id() != null && chatRoom.id().isValid()) {
            throw new IllegalArgumentException("새로운 채팅방은 ID를 가질 수 없습니다");
        }
        
        ChatRoomJpaEntity entity = toJpaEntity(chatRoom);
        ChatRoomJpaEntity savedEntity = jpaRepository.save(entity);
        
        return ChatRoomJpaEntity.toModel(savedEntity);
    }
    
    @Override
    public ChatRoom update(ChatRoom chatRoom) {
        if (chatRoom.id() == null || !chatRoom.id().isValid()) {
            throw new IllegalArgumentException("기존 채팅방은 유효한 ID가 필요합니다");
        }
        
        // 기존 엔티티를 조회 후 수정
        ChatRoomJpaEntity entity = jpaRepository.findById(chatRoom.id().getValue())
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + chatRoom.id()));
        
        entity.setUserId(chatRoom.userId().value());
        entity.setPetId(chatRoom.petId().value());
        entity.setLastMessageAt(chatRoom.lastMessageAt());
        
        ChatRoomJpaEntity savedEntity = jpaRepository.save(entity);
        
        return ChatRoomJpaEntity.toModel(savedEntity);
    }
    
    @Override
    public Optional<ChatRoom> findById(ChatRoomId id) {
        if (id == null || !id.isValid()) {
            return Optional.empty();
        }
        
        return jpaRepository.findById(id.getValue())
                .map(ChatRoomJpaEntity::toModel);
    }
    
    @Override
    public Optional<ChatRoom> findByUserIdAndPetId(UserId userId, PetId petId) {
        if (userId == null || !userId.isStored() || petId == null || !petId.isValid()) {
            return Optional.empty();
        }
        
        return jpaRepository.findByUserIdAndPetId(userId.value(), petId.value())
                .map(ChatRoomJpaEntity::toModel);
    }
    
    @Override
    public List<ChatRoom> findByUserId(UserId userId) {
        if (userId == null || !userId.isStored()) {
            return List.of();
        }
        
        return jpaRepository.findByUserIdOrderByLastMessageAtDesc(userId.value())
                .stream()
                .map(ChatRoomJpaEntity::toModel)
                .toList();
    }
    
    @Override
    public Optional<ChatRoom> findByPetId(PetId petId) {
        if (petId == null || !petId.isValid()) {
            return Optional.empty();
        }
        
        return jpaRepository.findByPetId(petId.value())
                .map(ChatRoomJpaEntity::toModel);
    }
    
    @Override
    public boolean existsById(ChatRoomId id) {
        if (id == null || !id.isValid()) {
            return false;
        }
        
        return jpaRepository.existsById(id.getValue());
    }
    
    @Override
    public boolean existsByUserIdAndPetId(UserId userId, PetId petId) {
        if (userId == null || !userId.isStored() || petId == null || !petId.isValid()) {
            return false;
        }
        
        return jpaRepository.existsByUserIdAndPetId(userId.value(), petId.value());
    }
    
    @Override
    public long countByUserId(UserId userId) {
        if (userId == null || !userId.isStored()) {
            return 0;
        }
        
        return jpaRepository.countByUserId(userId.value());
    }
    
    private ChatRoomJpaEntity toJpaEntity(ChatRoom chatRoom) {
        // 새로운 엔티티 생성 시에는 ID를 설정하지 않음 (DB에서 자동 생성)
        return new ChatRoomJpaEntity(
            chatRoom.userId().value(),
            chatRoom.petId().value(),
            chatRoom.lastMessageAt()
        );
    }
    

}