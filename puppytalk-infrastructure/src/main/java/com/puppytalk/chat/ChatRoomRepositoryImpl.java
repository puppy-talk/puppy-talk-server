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
        Assert.isTrue(chatRoom.getId().value() != null, "ChatRoom ID must be stored");
        
        ChatRoomJpaEntity entity = jpaRepository.findById(chatRoom.getId().value())
                .orElseThrow(() -> new ChatRoomNotFoundException(chatRoom.getId()));
        
        entity.update(chatRoom);
        
        ChatRoomJpaEntity savedEntity = jpaRepository.save(entity);
        
        return savedEntity.toDomain();
    }
    
    @Override
    public Optional<ChatRoom> findById(ChatRoomId id) {
        Assert.notNull(id, "ChatRoomId must not be null");
        
        if (id.value() == null) {
            return Optional.empty();
        }
        
        return jpaRepository.findById(id.value())
                .map(ChatRoomJpaEntity::toDomain);
    }
    
    @Override
    public Optional<ChatRoom> findByUserIdAndPetId(UserId userId, PetId petId) {
        Assert.notNull(userId, "UserId must not be null");
        Assert.notNull(petId, "PetId must not be null");
        
        if (userId.value() == null || petId.value() == null) {
            return Optional.empty();
        }
        
        return jpaRepository.findByUserIdAndPetId(userId.value(), petId.value())
                .map(ChatRoomJpaEntity::toDomain);
    }
    
    @Override
    public List<ChatRoom> findByUserId(UserId userId) {
        Assert.notNull(userId, "UserId must not be null");
        
        if (userId.value() == null) {
            throw new IllegalArgumentException("사용자 ID 값은 필수입니다");
        }
        
        return jpaRepository.findByUserIdOrderByLastMessageAtDesc(userId.value())
                .stream()
                .map(ChatRoomJpaEntity::toDomain)
                .toList();
    }
    
    @Override
    public Optional<ChatRoom> findByPetId(PetId petId) {
        Assert.notNull(petId, "PetId must not be null");
        
        if (petId.value() == null) {
            return Optional.empty();
        }
        
        return jpaRepository.findByPetId(petId.value())
                .map(ChatRoomJpaEntity::toDomain);
    }
    
    @Override
    public boolean existsById(ChatRoomId id) {
        Assert.notNull(id, "ChatRoomId must not be null");
        
        if (id.value() == null) {
            return false;
        }
        
        return jpaRepository.existsById(id.value());
    }
    
    @Override
    public boolean existsByUserIdAndPetId(UserId userId, PetId petId) {
        Assert.notNull(userId, "UserId must not be null");
        Assert.notNull(petId, "PetId must not be null");
        
        if (userId.value() == null || petId.value() == null) {
            return false;
        }
        
        return jpaRepository.existsByUserIdAndPetId(userId.value(), petId.value());
    }
    
    @Override
    public long countByUserId(UserId userId) {
        Assert.notNull(userId, "UserId must not be null");
        
        if (userId.value() == null) {
            return 0;
        }
        
        return jpaRepository.countByUserId(userId.value());
    }
    
    

}