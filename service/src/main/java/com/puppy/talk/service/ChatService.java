package com.puppy.talk.service;

import com.puppy.talk.ai.AiResponseService;
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
import com.puppy.talk.model.pet.Persona;
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
    private static final int AI_CONTEXT_MESSAGE_LIMIT = 5;
    private static final String CHAT_ROOM_NAME_PATTERN = "%s와의 채팅방";

    private final PetRepository petRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final AiResponseService aiResponseService;
    private final PersonaLookUpService personaLookUpService;
    private final ActivityTrackingService activityTrackingService;

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

        // 채팅방 열기 활동 기록
        activityTrackingService.trackChatOpened(pet.userId(), chatRoom.identity());

        return new ChatStartResult(chatRoom, pet, recentMessages);
    }

    /**
     * 사용자가 펫에게 메시지를 보냅니다.
     * 사용자 메시지 저장 후 AI 펫 응답을 자동으로 생성하여 저장합니다.
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

        // 펫 정보 조회
        Pet pet = petRepository.findByIdentity(chatRoom.petId())
            .orElseThrow(() -> new PetNotFoundException(chatRoom.petId()));

        // 사용자 메시지 저장
        Message userMessage = Message.of(
            null, // identity는 저장 시 생성됨
            chatRoomId,
            SenderType.USER,
            content.trim(),
            true, // 사용자가 보낸 메시지는 항상 읽음 처리
            LocalDateTime.now()
        );

        Message savedUserMessage = messageRepository.save(userMessage);

        // 메시지 전송 활동 기록
        activityTrackingService.trackMessageSent(pet.userId(), chatRoomId);

        // AI 펫 응답 생성 및 저장
        generateAndSavePetResponse(chatRoom, pet, content.trim());

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

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByIdentity(chatRoomId)
            .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + chatRoomId.id()));

        // 펫 정보 조회
        Pet pet = petRepository.findByIdentity(chatRoom.petId())
            .orElseThrow(() -> new PetNotFoundException(chatRoom.petId()));

        messageRepository.markAllAsReadByChatRoomId(chatRoomId);

        // 메시지 읽기 활동 기록
        activityTrackingService.trackMessageRead(pet.userId(), chatRoomId);
    }

    /**
     * AI를 사용하여 펫의 응답을 생성하고 저장합니다.
     */
    private void generateAndSavePetResponse(ChatRoom chatRoom, Pet pet, String userMessage) {
        try {
            // 페르소나 조회
            Persona persona = personaLookUpService.findPersona(pet.personaId());
            
            // 채팅 히스토리 조회 (최근 5개)
            List<Message> chatHistory = messageRepository
                .findByChatRoomIdOrderByCreatedAtDesc(chatRoom.identity())
                .stream()
                .limit(AI_CONTEXT_MESSAGE_LIMIT)
                .toList();
            
            // AI 응답 생성
            String aiResponse = aiResponseService.generatePetResponse(pet, persona, userMessage, chatHistory);
            
            // 펫 응답 메시지 저장
            Message petMessage = Message.of(
                null, // identity는 저장 시 생성됨
                chatRoom.identity(),
                SenderType.PET,
                aiResponse,
                false, // 펫 메시지는 처음에 읽지 않음 상태
                LocalDateTime.now()
            );
            
            messageRepository.save(petMessage);
            
        } catch (Exception e) {
            // AI 응답 생성 실패 시 로그만 남기고 계속 진행
            // 사용자 메시지는 정상적으로 저장되어야 함
            System.err.println("Failed to generate pet response: " + e.getMessage());
        }
    }

    private ChatRoom createNewChatRoom(Pet pet) {
        String roomName = String.format(CHAT_ROOM_NAME_PATTERN, pet.name());
        ChatRoom newChatRoom = ChatRoom.of(
            pet.identity(),
            roomName,
            LocalDateTime.now()
        );
        return chatRoomRepository.save(newChatRoom);
    }
}
