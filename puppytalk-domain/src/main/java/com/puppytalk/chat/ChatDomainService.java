package com.puppytalk.chat;

import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import java.util.List;
import java.util.Optional;

/**
 * 채팅 도메인 서비스
 * 채팅방과 메시지의 핵심 비즈니스 로직을 담당
 * 순수 도메인 로직만 포함 (프레임워크 무관)
 */
public class ChatDomainService {
    
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    
    public ChatDomainService(
        ChatRoomRepository chatRoomRepository,
        MessageRepository messageRepository
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.messageRepository = messageRepository;
    }
    
    /**
     * 채팅방을 찾거나 새로 생성하고 결과 반환
     * 
     * @param userId 사용자 ID
     * @param petId 반려동물 ID
     * @return 채팅방과 생성 여부 정보
     */
    public ChatRoomResult findOrCreateChatRoom(UserId userId, PetId petId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        if (petId == null) {
            throw new IllegalArgumentException("PetId must not be null");
        }

        Optional<ChatRoom> existingRoom = chatRoomRepository.findByUserIdAndPetId(userId, petId);
        if (existingRoom.isPresent()) {
            return ChatRoomResult.existing(existingRoom.get());
        }
        
        ChatRoom newRoom = ChatRoom.create(userId, petId);
        ChatRoom savedRoom = chatRoomRepository.save(newRoom);
        return ChatRoomResult.created(savedRoom);
    }
    
    /**
     * 채팅방 생성 결과
     */
    public record ChatRoomResult(
        ChatRoom chatRoom,
        boolean isNewlyCreated
    ) {
        public static ChatRoomResult created(ChatRoom chatRoom) {
            return new ChatRoomResult(chatRoom, true);
        }
        
        public static ChatRoomResult existing(ChatRoom chatRoom) {
            return new ChatRoomResult(chatRoom, false);
        }
    }
    
    /**
     * 채팅방 조회
     */
    public ChatRoom findChatRoom(ChatRoomId chatRoomId, UserId userId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));
            
        if (chatRoom.isOwnedBy(userId)) {
            return chatRoom;

        }
        throw new ChatRoomAccessDeniedException("채팅방에 접근할 권한이 없습니다", userId, chatRoomId);
    }
    
    /**
     * 사용자의 채팅방 목록 조회
     */
    public List<ChatRoom> findChatRoomList(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        return chatRoomRepository.findByUserId(userId);
    }
    
    /**
     * 사용자 메시지 전송
     */
    public void sendUserMessage(ChatRoomId chatRoomId, UserId userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content must not be null or empty");
        }

        ChatRoom chatRoom = findChatRoom(chatRoomId, userId);
        
        Message message = Message.of(chatRoomId, content);
        messageRepository.save(message);
        
        chatRoom.updateLastMessageTime();
        chatRoomRepository.save(chatRoom);
    }
    
    /**
     * 반려동물 메시지 전송 (AI 응답)
     */
    public Message sendPetMessage(ChatRoomId chatRoomId, String content) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId must not be null");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content must not be null or empty");
        }

        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));
        
        // 메시지 생성 및 저장
        Message message = Message.createPetMessage(chatRoomId, content);
        messageRepository.save(message);
        
        // 채팅방 마지막 메시지 시각 업데이트
        chatRoom.updateLastMessageTime();
        chatRoomRepository.save(chatRoom);
        
        return message;
    }
    
    /**
     * 채팅방의 메시지 목록 조회
     */
    public List<Message> findMessageList(ChatRoomId chatRoomId, UserId userId) {
        // 채팅방 존재 및 소유권 확인
        findChatRoom(chatRoomId, userId);
        
        return messageRepository.findByChatRoomIdOrderByCreatedAt(chatRoomId);
    }
    
    /**
     * 채팅방 메시지 목록 조회 (커서 기반 페이징)
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID (소유권 확인용)
     * @param cursor 커서 (이전 조회의 마지막 메시지 ID), null이면 첫 페이지
     * @param size 조회할 메시지 개수
     * @return 메시지 목록 (오래된 순서부터)
     */
    public List<Message> findMessageListWithCursor(ChatRoomId chatRoomId, UserId userId, 
                                                  MessageId cursor, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }

        findChatRoom(chatRoomId, userId);
        
        return messageRepository.findByChatRoomId(chatRoomId, cursor, size);
    }
    
    /**
     * 특정 메시지 조회
     */
    public Message findMessage(MessageId messageId, UserId userId) {
        if (messageId == null) {
            throw new IllegalArgumentException("MessageId must not be null");
        }

        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new MessageNotFoundException(messageId));
            
        // 메시지가 속한 채팅방의 소유권 확인
        findChatRoom(message.getChatRoomId(), userId);
        
        return message;
    }
    
    /**
     * AI 메시지 생성을 위한 채팅 히스토리 조회
     * 
     * @param chatRoomId 채팅방 ID
     * @param limit 조회할 메시지 개수 (최신순)
     * @return 최신 메시지부터 정렬된 리스트
     */
    public List<Message> findRecentChatHistory(ChatRoomId chatRoomId, int limit) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId must not be null");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }

        // 채팅방 존재 확인
        chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));
            
        return messageRepository.findRecentMessages(chatRoomId, limit);
    }
    
    /**
     * 사용자와 반려동물의 채팅방 조회 (소유권 확인 없이)
     * AI 메시지 생성 시 시스템에서 사용
     */
    public Optional<ChatRoom> findChatRoomByUserAndPet(UserId userId, PetId petId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        if (petId == null) {
            throw new IllegalArgumentException("PetId must not be null");
        }
        
        return chatRoomRepository.findByUserIdAndPetId(userId, petId);
    }
}