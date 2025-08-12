package com.puppy.talk.infrastructure;

import com.puppy.talk.model.chat.ChatRoom;
import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.pet.PetIdentity;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository {

    Optional<ChatRoom> findByIdentity(ChatRoomIdentity identity);

    Optional<ChatRoom> findByPetId(PetIdentity petId);

    List<ChatRoom> findAll();

    ChatRoom save(ChatRoom chatRoom);

    void deleteByIdentity(ChatRoomIdentity identity);
}