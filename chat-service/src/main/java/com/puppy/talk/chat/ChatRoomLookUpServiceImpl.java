package com.puppy.talk.chat;

import com.puppy.talk.pet.PetIdentity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomLookUpServiceImpl implements ChatRoomLookUpService {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    @Transactional(readOnly = true)
    public ChatRoom findChatRoom(ChatRoomIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        return chatRoomRepository.findByIdentity(identity)
            .orElseThrow(() -> new ChatRoomNotFoundException(identity));
    }

    @Override
    @Transactional(readOnly = true)
    public ChatRoom findChatRoomByPetId(PetIdentity petId) {
        if (petId == null) {
            throw new IllegalArgumentException("PetId cannot be null");
        }
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
        if (chatRoom == null) {
            throw new IllegalArgumentException("ChatRoom cannot be null");
        }
        return chatRoomRepository.save(chatRoom);
    }

    @Override
    @Transactional
    public void deleteChatRoom(ChatRoomIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        if (!chatRoomRepository.findByIdentity(identity).isPresent()) {
            throw new ChatRoomNotFoundException(identity);
        }
        chatRoomRepository.deleteByIdentity(identity);
    }
}
