package com.puppy.talk.chat;

import com.puppy.talk.pet.PetIdentity;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository {

    Optional<ChatRoom> findByIdentity(ChatRoomIdentity identity);

    Optional<ChatRoom> findByPetId(PetIdentity petId);

    List<ChatRoom> findAll();

    ChatRoom save(ChatRoom chatRoom);

    void deleteByIdentity(ChatRoomIdentity identity);
}