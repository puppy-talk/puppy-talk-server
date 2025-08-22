package com.puppytalk.chat;

import com.puppytalk.pet.PetId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ChatRepositoryImpl implements ChatRoomRepository {

    private final ChatJpaRepository chatJpaRepository;

    public ChatRepositoryImpl(ChatJpaRepository chatJpaRepository) {
        this.chatJpaRepository = chatJpaRepository;
    }

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        return null;
    }

    @Override
    public Optional<ChatRoom> findById(ChatRoomId chatRoomId) {
        return Optional.empty();
    }

    @Override
    public List<ChatRoom> findByUserId(Long userId) {
        return List.of();
    }

    @Override
    public Optional<ChatRoom> findByPetId(PetId petId) {
        return Optional.empty();
    }

    @Override
    public List<ChatRoom> findActiveByUserId(Long userId) {
        return List.of();
    }

    @Override
    public List<ChatRoom> findInactiveAfterMinutes(int minutes) {
        return List.of();
    }

    @Override
    public boolean existsByPetId(PetId petId) {
        return false;
    }

    @Override
    public void deleteById(ChatRoomId chatRoomId) {

    }
}
