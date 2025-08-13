package com.puppy.talk.service.chat;

import com.puppy.talk.model.chat.ChatRoom;
import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.pet.PetIdentity;
import java.util.List;

public interface ChatRoomLookUpService {

    ChatRoom findChatRoom(ChatRoomIdentity identity);

    ChatRoom findChatRoomByPetId(PetIdentity petId);

    List<ChatRoom> findAllChatRooms();

    ChatRoom createChatRoom(ChatRoom chatRoom);

    void deleteChatRoom(ChatRoomIdentity identity);
}