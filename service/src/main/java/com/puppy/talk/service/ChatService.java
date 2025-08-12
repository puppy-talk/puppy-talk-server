package com.puppy.talk.service;

import com.puppy.talk.exception.pet.PetNotFoundException;
import com.puppy.talk.infrastructure.chat.ChatRoomRepository;
import com.puppy.talk.infrastructure.chat.MessageRepository;
import com.puppy.talk.infrastructure.pet.PetRepository;
import com.puppy.talk.model.chat.ChatRoom;
import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.chat.Message;
import com.puppy.talk.model.chat.MessageIdentity;
import com.puppy.talk.model.chat.SenderType;
import com.puppy.talk.model.pet.Pet;
import com.puppy.talk.model.pet.PetIdentity;
import com.puppy.talk.service.dto.ChatStartResult;
import com.puppy.talk.service.dto.MessageSendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int DEFAULT_RECENT_MESSAGE_LIMIT = 50;
    private static final String CHAT_ROOM_NAME_PATTERN = "%s와의 채팅방";

    private final PetRepository petRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    /**
     * 펫과의 대화를 시작합니다.
     * 이미 채팅방이 있다면 기존 채팅방을 반환하고, 없다면 새로 생성합니다.
     */
    @Transactional
    public ChatStartResult startChatWithPet(PetIdentity petId) {
        if (petId == null) {
            throw new IllegalArgumentException("PetId cannot be null");
        }

        // 펫 존재 확인
        Pet pet = petRepository.findByIdentity(petId)
            .orElseThrow(() -> new PetNotFoundException(petId));

        // 기존 채팅방 찾기
        ChatRoom chatRoom = chatRoomRepository.findByPetId(petId)
            .orElseGet(() -> createNewChatRoom(pet));

        // 최근 메시지들 조회 (설정된 제한만큼)
        List<Message> recentMessages = messageRepository
            .findByChatRoomIdOrderByCreatedAtDesc(chatRoom.identity())
            .stream()
            .limit(DEFAULT_RECENT_MESSAGE_LIMIT)
            .toList();

        return new ChatStartResult(chatRoom, pet, recentMessages);
    }

    /**
     * 사용자가 펫에게 메시지를 보냅니다.
     */
    @Transactional
    public MessageSendResult sendMessageToPet(ChatRoomIdentity chatRoomId, String content) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }

        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findByIdentity(chatRoomId)
            .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + chatRoomId.id()));

        // 사용자 메시지 저장
        Message userMessage = new Message(
            null, // identity는 저장 시 생성됨
            chatRoomId,
            SenderType.USER,
            content.trim(),
            true, // 사용자가 보낸 메시지는 항상 읽음 처리
            LocalDateTime.now()
        );

        Message savedUserMessage = messageRepository.save(userMessage);

        // 채팅방 마지막 메시지 시간 업데이트
        ChatRoom updatedChatRoom = new ChatRoom(
            chatRoom.identity(),
            chatRoom.petId(),
            chatRoom.roomName(),
            LocalDateTime.now()
        );
        chatRoomRepository.save(updatedChatRoom);

        return new MessageSendResult(savedUserMessage, updatedChatRoom);
    }

    /**
     * 채팅방의 모든 메시지를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<Message> getChatHistory(ChatRoomIdentity chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }

        return messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
    }

    /**
     * 채팅방의 읽지 않은 메시지를 모두 읽음 처리합니다.
     */
    @Transactional
    public void markMessagesAsRead(ChatRoomIdentity chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }

        messageRepository.markAllAsReadByChatRoomId(chatRoomId);
    }

    private ChatRoom createNewChatRoom(Pet pet) {
        String roomName = String.format(CHAT_ROOM_NAME_PATTERN, pet.name());
        ChatRoom newChatRoom = new ChatRoom(
            null, // identity는 저장 시 생성됨
            pet.identity(),
            roomName,
            LocalDateTime.now()
        );
        return chatRoomRepository.save(newChatRoom);
    }
}
