package com.puppytalk.chat;

import com.puppytalk.pet.Pet;
import com.puppytalk.pet.PetId;
import com.puppytalk.pet.PetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 채팅 애플리케이션 파사드
 */
@Service
@Transactional
public class ChatFacade {
    
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final PetRepository petRepository;
    
    public ChatFacade(
        ChatRoomRepository chatRoomRepository,
        MessageRepository messageRepository,
        PetRepository petRepository
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.messageRepository = messageRepository;
        this.petRepository = petRepository;
    }
    
    /**
     * 채팅방 생성 (반려동물 생성 시 자동 호출)
     */
    public ChatRoom createChatRoom(Long userId, PetId petId) {
        validateChatRoomCreation(userId, petId);
        
        // 이미 채팅방이 존재하는지 확인 (1:1 관계)
        if (chatRoomRepository.existsByPetId(petId)) {
            throw new IllegalStateException("반려동물에 대한 채팅방이 이미 존재합니다");
        }
        
        ChatRoom chatRoom = ChatRoom.create(userId, petId);
        return chatRoomRepository.save(chatRoom);
    }
    
    /**
     * 채팅방 생성 (Long ID 사용)
     */
    public ChatRoom createChatRoom(Long userId, Long petId) {
        return createChatRoom(userId, PetId.of(petId));
    }
    
    /**
     * 사용자 메시지 전송
     */
    public Message sendUserMessage(Long userId, ChatRoomId chatRoomId, String content) {
        ChatRoom chatRoom = getChatRoomAndValidateUser(chatRoomId, userId);
        
        if (!chatRoom.canChat()) {
            throw new IllegalStateException("채팅 불가능한 상태의 채팅방입니다");
        }
        
        Message message = Message.createUserMessage(chatRoomId, content);
        Message savedMessage = messageRepository.save(message);
        
        // 채팅방의 마지막 메시지 시각 업데이트
        chatRoom.updateLastMessageTime(savedMessage.getSentAt());
        chatRoomRepository.save(chatRoom);
        
        return savedMessage;
    }
    
    /**
     * 반려동물 메시지 전송 (AI 생성)
     */
    public Message sendPetMessage(ChatRoomId chatRoomId, String content) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        
        if (!chatRoom.canChat()) {
            throw new IllegalStateException("채팅 불가능한 상태의 채팅방입니다");
        }
        
        Message message = Message.createPetMessage(chatRoomId, content);
        Message savedMessage = messageRepository.save(message);
        
        // 채팅방의 마지막 메시지 시각 업데이트
        chatRoom.updateLastMessageTime(savedMessage.getSentAt());
        chatRoomRepository.save(chatRoom);
        
        return savedMessage;
    }
    
    /**
     * 사용자의 채팅방 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> getUserChatRooms(Long userId) {
        return chatRoomRepository.findActiveByUserId(userId);
    }
    
    /**
     * 채팅방의 메시지 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Message> getChatRoomMessages(Long userId, ChatRoomId chatRoomId) {
        ChatRoom chatRoom = getChatRoomAndValidateUser(chatRoomId, userId);
        return messageRepository.findByChatRoomIdOrderBySentAtDesc(chatRoomId);
    }
    
    /**
     * 채팅방의 메시지 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public List<Message> getChatRoomMessages(Long userId, ChatRoomId chatRoomId, int offset, int limit) {
        ChatRoom chatRoom = getChatRoomAndValidateUser(chatRoomId, userId);
        return messageRepository.findByChatRoomIdOrderBySentAtDesc(chatRoomId, offset, limit);
    }
    
    /**
     * 채팅방 비활성화
     */
    public void deactivateChatRoom(Long userId, ChatRoomId chatRoomId) {
        ChatRoom chatRoom = getChatRoomAndValidateUser(chatRoomId, userId);
        chatRoom.deactivate();
        chatRoomRepository.save(chatRoom);
    }
    
    /**
     * 채팅방 활성화
     */
    public void activateChatRoom(Long userId, ChatRoomId chatRoomId) {
        ChatRoom chatRoom = getChatRoomAndValidateUser(chatRoomId, userId);
        chatRoom.activate();
        chatRoomRepository.save(chatRoom);
    }
    
    /**
     * 채팅방 삭제
     */
    public void deleteChatRoom(Long userId, ChatRoomId chatRoomId) {
        ChatRoom chatRoom = getChatRoomAndValidateUser(chatRoomId, userId);
        chatRoom.delete();
        chatRoomRepository.save(chatRoom);
    }
    
    /**
     * 알림 대상 채팅방 조회 (마지막 활동으로부터 특정 시간 경과)
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> getInactiveChatRooms(int minutes) {
        return chatRoomRepository.findInactiveAfterMinutes(minutes);
    }
    
    private void validateChatRoomCreation(Long userId, PetId petId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다");
        }
        
        // 반려동물 존재 여부 확인
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반려동물입니다"));
        
        // 반려동물의 소유자 확인
        if (!pet.isOwnedBy(userId)) {
            throw new IllegalArgumentException("자신의 반려동물만 채팅방을 생성할 수 있습니다");
        }
    }
    
    private ChatRoom getChatRoom(ChatRoomId chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다"));
    }
    
    private ChatRoom getChatRoomAndValidateUser(ChatRoomId chatRoomId, Long userId) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        
        if (!chatRoom.belongsToUser(userId)) {
            throw new IllegalArgumentException("접근 권한이 없는 채팅방입니다");
        }
        
        return chatRoom;
    }
}