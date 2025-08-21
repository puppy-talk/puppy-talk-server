package com.puppy.talk.chat.service.impl;

import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.service.ChatRoomLookUpService;
import com.puppy.talk.chat.ChatRoomNotFoundException;
import com.puppy.talk.chat.ChatRoomRepository;
import com.puppy.talk.pet.PetIdentity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 채팅방 도메인 서비스 구현체
 * 
 * 채팅방 관련 비즈니스 로직을 캡슐화하고
 * 도메인 규칙을 적용합니다.
 */
@Service
@RequiredArgsConstructor
public class ChatRoomLookUpServiceImpl implements ChatRoomLookUpService {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    @Transactional(readOnly = true)
    public ChatRoom findChatRoom(ChatRoomIdentity identity) {
        Assert.notNull(identity, "Identity cannot be null");
        return chatRoomRepository.findByIdentity(identity)
            .orElseThrow(() -> new ChatRoomNotFoundException(identity));
    }

    @Override
    @Transactional(readOnly = true)
    public ChatRoom findChatRoomByPetId(PetIdentity petId) {
        Assert.notNull(petId, "PetId cannot be null");
        return chatRoomRepository.findByPetId(petId)
            .orElseThrow(() -> new ChatRoomNotFoundException(
                "ChatRoom not found for pet: " + petId.id()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoom> findAllChatRooms() {
        return chatRoomRepository.findAll();
    }

    @Override
    @Transactional
    public ChatRoom createChatRoom(ChatRoom chatRoom) {
        Assert.notNull(chatRoom, "ChatRoom cannot be null");
        return chatRoomRepository.save(chatRoom);
    }

    @Override
    @Transactional
    public void deleteChatRoom(ChatRoomIdentity identity) {
        Assert.notNull(identity, "Identity cannot be null");
        
        // 존재 여부 확인
        chatRoomRepository.findByIdentity(identity)
            .orElseThrow(() -> new ChatRoomNotFoundException(identity));
            
        chatRoomRepository.deleteByIdentity(identity);
    }
}