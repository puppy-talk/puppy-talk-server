package com.puppytalk.chat;

import com.puppytalk.chat.exception.ChatRoomNotFoundException;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
public class ChatRoomRepositoryImpl implements ChatRoomRepository {
    
    private final ChatRoomJpaRepository jpaRepository;
    
    public ChatRoomRepositoryImpl(ChatRoomJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public ChatRoom create(ChatRoom chatRoom) {
        Assert.notNull(chatRoom, "ChatRoom must not be null");
        
        ChatRoomJpaEntity entity = ChatRoomJpaEntity.from(chatRoom);
        ChatRoomJpaEntity savedEntity = jpaRepository.save(entity);
        
        return savedEntity.toDomain();
    }
    
    @Override
    public ChatRoom update(ChatRoom chatRoom) {
        Assert.notNull(chatRoom, "ChatRoom must not be null");
        Assert.notNull(chatRoom.getId(), "ChatRoom ID must not be null");
        Assert.isTrue(chatRoom.getId().isStored(), "ChatRoom ID must be stored");
        
        ChatRoomJpaEntity entity = jpaRepository.findById(chatRoom.getId().getValue())
                .orElseThrow(() -> new ChatRoomNotFoundException(chatRoom.getId()));
        
        entity.update(chatRoom);
        
        ChatRoomJpaEntity savedEntity = jpaRepository.save(entity);
        
        return savedEntity.toDomain();
    }
    
    @Override
    public Optional<ChatRoom> findById(ChatRoomId id) {
        Assert.notNull(id, "ChatRoomId must not be null");
        
        if (!id.isStored()) {
            return Optional.empty();
        }
        
        return jpaRepository.findById(id.getValue())
                .map(ChatRoomJpaEntity::toDomain);
    }
    
    @Override
    public Optional<ChatRoom> findByUserIdAndPetId(UserId userId, PetId petId) {
        Assert.notNull(userId, "UserId must not be null");
        Assert.notNull(petId, "PetId must not be null");
        
        if (!userId.isStored() || !petId.isStored()) {
            return Optional.empty();
        }
        
        return jpaRepository.findByUserIdAndPetId(userId.getValue(), petId.getValue())
                .map(ChatRoomJpaEntity::toDomain);
    }
    
    @Override
    public List<ChatRoom> findByUserId(UserId userId) {
        Assert.notNull(userId, "UserId must not be null");
        
        if (!userId.isStored()) {
            return List.of();
        }
        
        return jpaRepository.findByUserIdOrderByLastMessageAtDesc(userId.getValue())
                .stream()
                .map(ChatRoomJpaEntity::toDomain)
                .toList();
    }
    
    @Override
    public Optional<ChatRoom> findByPetId(PetId petId) {
        Assert.notNull(petId, "PetId must not be null");
        
        if (!petId.isStored()) {
            return Optional.empty();
        }
        
        return jpaRepository.findByPetId(petId.getValue())
                .map(ChatRoomJpaEntity::toDomain);
    }
    
    @Override
    public boolean existsById(ChatRoomId id) {
        Assert.notNull(id, "ChatRoomId must not be null");
        
        if (!id.isStored()) {
            return false;
        }
        
        return jpaRepository.existsById(id.getValue());
    }
    
    @Override
    public boolean existsByUserIdAndPetId(UserId userId, PetId petId) {
        Assert.notNull(userId, "UserId must not be null");
        Assert.notNull(petId, "PetId must not be null");
        
        if (!userId.isStored() || !petId.isStored()) {
            return false;
        }
        
        return jpaRepository.existsByUserIdAndPetId(userId.getValue(), petId.getValue());
    }
    
    @Override
    public long countByUserId(UserId userId) {
        Assert.notNull(userId, "UserId must not be null");
        
        if (!userId.isStored()) {
            return 0;
        }
        
        return jpaRepository.countByUserId(userId.getValue());
    }
    
    

}