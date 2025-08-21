package com.puppy.talk.chat.service;

import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.pet.PetIdentity;
import java.util.List;

/**
 * 채팅방 관련 도메인 서비스
 * 
 * 순수한 비즈니스 로직과 도메인 규칙을 담당합니다.
 * 인프라스트럭처 세부사항에 의존하지 않습니다.
 */
public interface ChatRoomLookUpService {

    ChatRoom findChatRoom(ChatRoomIdentity identity);

    ChatRoom findChatRoomByPetId(PetIdentity petId);

    List<ChatRoom> findAllChatRooms();

    ChatRoom createChatRoom(ChatRoom chatRoom);

    void deleteChatRoom(ChatRoomIdentity identity);
}