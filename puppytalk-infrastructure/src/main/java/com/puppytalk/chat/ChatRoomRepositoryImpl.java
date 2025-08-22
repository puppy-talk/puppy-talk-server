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
    public ChatRoom save(ChatRoom chatRoom) {
        ChatRoomJpaEntity entity = toJpaEntity(chatRoom);
        ChatRoomJpaEntity savedEntity = jpaRepository.save(entity);
        
        // 저장된 엔티티를 도메인 객체로 변환하여 반환
        return toDomainEntity(savedEntity);
    }
    
    @Override
    public Optional<ChatRoom> findById(ChatRoomId id) {
        if (id == null || !id.isStored()) {
            return Optional.empty();
        }
        
        return jpaRepository.findById(id.getValue())
                .map(this::toDomainEntity);
    }
    
    @Override
    public Optional<ChatRoom> findByUserIdAndPetId(UserId userId, PetId petId) {
        if (userId == null || !userId.isStored() || petId == null || !petId.isStored()) {
            return Optional.empty();
        }
        
        return jpaRepository.findByUserIdAndPetId(userId.value(), petId.value())
                .map(this::toDomainEntity);
    }
    
    @Override
    public List<ChatRoom> findByUserId(UserId userId) {
        if (userId == null || !userId.isStored()) {
            return List.of();
        }
        
        return jpaRepository.findByUserIdOrderByLastMessageAtDesc(userId.value())
                .stream()
                .map(this::toDomainEntity)
                .toList();
    }
    
    @Override
    public Optional<ChatRoom> findByPetId(PetId petId) {
        if (petId == null || !petId.isStored()) {
            return Optional.empty();
        }
        
        return jpaRepository.findByPetId(petId.value())
                .map(this::toDomainEntity);
    }
    
    @Override
    public boolean existsById(ChatRoomId id) {
        if (id == null || !id.isStored()) {
            return false;
        }
        
        return jpaRepository.existsById(id.getValue());
    }
    
    @Override
    public boolean existsByUserIdAndPetId(UserId userId, PetId petId) {
        if (userId == null || !userId.isStored() || petId == null || !petId.isStored()) {
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
        ChatRoomJpaEntity entity = new ChatRoomJpaEntity(
            chatRoom.getUserId().value(),
            chatRoom.getPetId().value(),
            chatRoom.getLastMessageAt()
        );
        
        if (chatRoom.getId().isStored()) {
            entity.setId(chatRoom.getId().getValue());
        }
        
        return entity;
    }
    
    private ChatRoom toDomainEntity(ChatRoomJpaEntity entity) {
        return ChatRoom.restore(
            ChatRoomId.from(entity.getId()),
            UserId.from(entity.getUserId()),
            PetId.from(entity.getPetId()),
            entity.getCreatedAt(),
            entity.getLastMessageAt()
        );
    }
}