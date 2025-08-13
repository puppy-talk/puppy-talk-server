package com.puppy.talk.chat;

import com.puppy.talk.pet.PetIdentity;
import java.util.List;

public interface ChatRoomLookUpService {

    ChatRoom findChatRoom(ChatRoomIdentity identity);

    ChatRoom findChatRoomByPetId(PetIdentity petId);

    List<ChatRoom> findAllChatRooms();

    ChatRoom createChatRoom(ChatRoom chatRoom);

    void deleteChatRoom(ChatRoomIdentity identity);
}