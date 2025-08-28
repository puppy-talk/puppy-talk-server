package com.puppytalk.chat;

import com.puppytalk.ai.AiMessageGenerationService;
import com.puppytalk.chat.dto.request.ChatRoomCreateCommand;
import com.puppytalk.chat.dto.request.ChatRoomListQuery;
import com.puppytalk.chat.dto.request.MessageListQuery;
import com.puppytalk.chat.dto.request.MessageSendCommand;
import com.puppytalk.chat.dto.request.NewMessageQuery;
import com.puppytalk.chat.dto.response.ChatRoomCreateResponse;
import com.puppytalk.chat.dto.response.ChatRoomListResult;
import com.puppytalk.chat.dto.response.MessageListResult;
import com.puppytalk.chat.dto.response.NewMessageResult;
import com.puppytalk.pet.Pet;
import com.puppytalk.pet.PetDomainService;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional(readOnly = true)
public class ChatFacade {

    private static final Logger log = LoggerFactory.getLogger(ChatFacade.class);

    private final ChatDomainService chatDomainService;
    private final PetDomainService petDomainService;
    private final AiMessageGenerationService aiMessageGenerationService;

    public ChatFacade(
        ChatDomainService chatDomainService,
        PetDomainService petDomainService,
        AiMessageGenerationService aiMessageGenerationService
    ) {
        this.chatDomainService = chatDomainService;
        this.petDomainService = petDomainService;
        this.aiMessageGenerationService = aiMessageGenerationService;
    }

    /**
     * 채팅방을 생성하거나 기존 채형방을 조회합니다.
     * <p>
     * 사용자와 반려동물 간의 1:1 채팅방을 찾거나 새로 생성합니다. 이미 채팅방이 존재하는 경우 기존 채팅방 정보를 반환합니다.
     *
     * @param command 채팅방 생성 커맨드 (userId와 petId 필수)
     * @return ChatRoomCreateResponse 채형방 생성 결과 (새로 생성 여부 포함)
     * @throws IllegalArgumentException command가 null인 경우
     */
    @Transactional
    public ChatRoomCreateResponse createOrFindChatRoom(ChatRoomCreateCommand command) {
        Assert.notNull(command, "ChatRoomCreateCommand must not be null");
        Assert.notNull(command.petId(), "PetId must not be null");
        Assert.notNull(command.userId(), "UserId must not be null");

        log.info("Creating or finding chat room for user: {}, pet: {}",
            command.userId(), command.petId());

        UserId userId = UserId.from(command.userId());
        PetId petId = PetId.from(command.petId());

        ChatRoomResult result = chatDomainService.
            findOrCreateChatRoom(userId, petId);

        if (result.isCreated()) {
            return ChatRoomCreateResponse.created(result.chatRoom());
        }

        return ChatRoomCreateResponse.existing(result.chatRoom());
    }

    /**
     * 사용자 메시지를 전송하고 AI 응답을 생성합니다.
     * <p>
     * 메시지 내용에 대한 기본 검증을 수행한 후 전송하고, AI 서비스를 통해 반려동물 응답을 생성합니다.
     *
     * @param command 메시지 전송 커맨드 (chatRoomId, userId, content 필수)
     * @throws IllegalArgumentException command가 null이거나 메시지 내용이 유효하지 않은 경우
     */
    @Transactional
    public void sendUserMessage(MessageSendCommand command) {
        Assert.notNull(command, "MessageSendCommand must not be null");
        Assert.notNull(command.userId(), "UserId must not be null");
        Assert.hasText(command.content(), "Content cannot be null or empty");

        log.debug("Sending user message in chat room: {} by user: {}",
            command.chatRoomId(), command.userId()
        );

        ChatRoomId chatRoomId = ChatRoomId.from(command.chatRoomId());
        UserId userId = UserId.from(command.userId());

        // 1. 사용자 메시지 저장
        chatDomainService.sendUserMessage(chatRoomId, userId, command.content());

        // 2. AI 응답 생성 (동기 방식)
        PetId petId = chatDomainService.getPetIdFromChatRoom(chatRoomId);
        Pet pet = petDomainService.getPet(petId, userId);

        List<Message> conversationHistory = chatDomainService.findMessageListWithCursor(
            chatRoomId, userId, null, 10
        );

        String aiResponse = aiMessageGenerationService.generateChatResponse(
            userId, pet.id(), pet, command.content(), conversationHistory
        );

        // 3. AI 응답 메시지 저장
        log.info("AI response generated for chat room: {}", command.chatRoomId());
        chatDomainService.sendPetMessage(chatRoomId, aiResponse);
    }

    /**
     * 사용자의 채팅방 목록 조회
     */
    public ChatRoomListResult getChatRoomList(ChatRoomListQuery query) {
        Assert.notNull(query, "ChatRoomListQuery must not be null");
        Assert.notNull(query.userId(), "UserId must not be null");

        UserId userId = UserId.from(query.userId());
        List<ChatRoom> chatRooms = chatDomainService.findChatRoomList(userId);

        return ChatRoomListResult.from(chatRooms);
    }

    /**
     * 메시지 목록 조회 (커서 기반 페이징)
     */
    public MessageListResult getMessageList(MessageListQuery query) {
        Assert.notNull(query, "MessageListQuery must not be null");
        Assert.notNull(query.chatRoomId(), "ChatRoomId must not be null");
        Assert.notNull(query.userId(), "UserId must not be null");

        ChatRoomId chatRoomId = ChatRoomId.from(query.chatRoomId());
        UserId userId = UserId.from(query.userId());

        MessageId cursor = query.cursor() != null ? MessageId.from(query.cursor()) : null;
        int size = query.getSize();

        List<Message> messages = chatDomainService.findMessageListWithCursor(
            chatRoomId, userId, cursor, size
        );

        return MessageListResult.withCursor(messages, size);
    }

    /**
     * 새 메시지를 조회합니다 (폴링용).
     * <p>
     * 지정된 시간 이후에 생성된 새로운 메시지들을 조회합니다. 폴링 요청에 대한 메트릭을 수집하여 성능 모니터링에 활용합니다.
     *
     * @param query 새 메시지 조회 쿼리 (chatRoomId, userId, since 필수)
     * @return NewMessageResult 새 메시지 조회 결과
     * @throws IllegalArgumentException query가 null인 경우
     */
    public NewMessageResult getNewMessages(NewMessageQuery query) {
        Assert.notNull(query, "NewMessageQuery must not be null");
        Assert.notNull(query.chatRoomId(), "ChatRoomId must not be null");
        Assert.notNull(query.userId(), "UserId must not be null");
        Assert.notNull(query.since(), "Since must not be null");

        ChatRoomId chatRoomId = ChatRoomId.from(query.chatRoomId());
        UserId userId = UserId.from(query.userId());

        List<Message> newMessages = chatDomainService.findNewMessages(
            chatRoomId, userId, query.since()
        );

        return NewMessageResult.from(newMessages);
    }
}