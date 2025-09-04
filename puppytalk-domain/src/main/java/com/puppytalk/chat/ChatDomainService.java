package com.puppytalk.chat;

import com.puppytalk.chat.exception.ChatRoomAccessDeniedException;
import com.puppytalk.chat.exception.ChatRoomNotFoundException;
import com.puppytalk.chat.exception.MessageNotFoundException;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import java.util.List;
import java.util.Optional;

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
     * 채팅방을 생성하거나 기존 채팅방을 반환합니다.
     * <p>
     *
     * @param userId 채팅방을 생성하는 사용자의 ID
     * @param petId  채팅 대상 반려동물의 ID
     * @return ChatRoom 생성된 채팅방 또는 기존 채팅방
     * @throws IllegalArgumentException userId 또는 petId가 null인 경우
     */
    public ChatRoom createChatRoom(UserId userId, PetId petId) {

        Optional<ChatRoom> chatRoom = chatRoomRepository.findByUserIdAndPetId(userId, petId);
        return chatRoom.orElseGet(() -> chatRoomRepository.create(ChatRoom.create(userId, petId)));
    }


    /**
     * 채팅방 조회
     */
    public ChatRoom findChatRoom(ChatRoomId chatRoomId, UserId userId) {

        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));

        if (!room.isOwnedBy(userId)) {
            throw new ChatRoomAccessDeniedException("채팅방에 접근할 권한이 없습니다", userId, chatRoomId);
        }

        return room;
    }

    /**
     * 사용자의 채팅방 목록 조회
     */
    public List<ChatRoom> findChatRoomList(UserId userId) {
        return chatRoomRepository.findByUserId(userId);
    }

    /**
     * 사용자 메시지 전송
     */
    public void sendUserMessage(ChatRoomId chatRoomId, UserId userId, String content) {

        // 채팅방 존재 및 소유권 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));

        if (!chatRoom.isOwnedBy(userId)) {
            throw new ChatRoomAccessDeniedException("채팅방에 접근할 권한이 없습니다", userId, chatRoomId);
        }

        Message message = Message.create(chatRoomId, userId, content);
        messageRepository.create(message);

        ChatRoom updatedChatRoom = chatRoom.withLastMessageTime();
        chatRoomRepository.update(updatedChatRoom);
    }

    /**
     * 반려동물 메시지 전송 (AI 응답)
     */
    public Message sendPetMessage(ChatRoomId chatRoomId, String content) {

        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));

        // 메시지 생성 및 저장
        Message petMessage = Message.createPetMessage(chatRoomId, content);
        messageRepository.create(petMessage);

        // 채팅방 마지막 메시지 시각 업데이트
        ChatRoom updatedChatRoom = chatRoom.withLastMessageTime();
        chatRoomRepository.update(updatedChatRoom);

        return petMessage;
    }

    /**
     * 채팅방의 메시지 목록 조회
     */
    public List<Message> findMessageList(ChatRoomId chatRoomId, UserId userId) {
        validateChatRoomAccess(chatRoomId, userId);
        return messageRepository.findByChatRoomIdWithCursor(chatRoomId);
    }

    /**
     * 채팅방 메시지 목록 조회 (커서 기반 페이징)
     *
     * @param chatRoomId 채팅방 ID
     * @param userId     사용자 ID (소유권 확인용)
     * @param cursor     커서 (이전 조회의 마지막 메시지 ID), null이면 첫 페이지
     * @param size       조회할 메시지 개수
     * @return 메시지 목록 (오래된 순서부터)
     */
    public List<Message> findMessageListWithCursor(
        ChatRoomId chatRoomId,
        UserId userId,
        MessageId cursor,
        int size
    ) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }

        // 채팅방 접근 권한 검증
        validateChatRoomAccess(chatRoomId, userId);
        return messageRepository.findByChatRoomIdWithCursor(chatRoomId, cursor, size);
    }

    /**
     * 특정 메시지 조회
     */
    public Message findMessage(MessageId messageId, UserId userId) {

        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new MessageNotFoundException(messageId));

        validateChatRoomAccess(message.getChatRoomId(), userId);
        return message;
    }

    /**
     * AI 메시지 생성을 위한 채팅 히스토리 조회
     *
     * @param chatRoomId 채팅방 ID
     * @param limit      조회할 메시지 개수 (최신순)
     * @return 최신 메시지부터 정렬된 리스트
     */
    public List<Message> findRecentChatHistory(ChatRoomId chatRoomId, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }

        chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));

        return messageRepository.findRecentMessages(chatRoomId, limit);
    }

    /**
     * 특정 시간 이후의 새로운 메시지 조회
     */
    public List<Message> findNewMessages(ChatRoomId chatRoomId, UserId userId,
        java.time.LocalDateTime since) {
        if (since == null) {
            throw new IllegalArgumentException("Since time must not be null");
        }

        validateChatRoomAccess(chatRoomId, userId);

        return messageRepository.findByChatRoomIdAndCreatedAtAfter(chatRoomId, since);
    }

    /**
     * 사용자와 반려동물의 채팅방 조회
     */
    public Optional<ChatRoom> findChatRoomByUserIdAndPetId(UserId userId, PetId petId) {
        return chatRoomRepository.findByUserIdAndPetId(userId, petId);
    }

    /**
     * 채팅방 접근 권한 검증
     */
    private void validateChatRoomAccess(ChatRoomId chatRoomId, UserId userId) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다: " + chatRoomId));

        if (chatRoom.isOwnedBy(userId)) {
            return;
        }

        throw new ChatRoomAccessDeniedException("채팅방에 접근할 권한이 없습니다", userId, chatRoomId);
    }
}