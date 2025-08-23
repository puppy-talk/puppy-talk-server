package com.puppytalk.chat;

import com.puppytalk.chat.dto.request.ChatRoomCreateCommand;
import com.puppytalk.chat.dto.request.ChatRoomListQuery;
import com.puppytalk.chat.dto.request.MessageListQuery;
import com.puppytalk.chat.dto.request.MessageSendCommand;
import com.puppytalk.chat.dto.response.ChatRoomCreateResponse;
import com.puppytalk.chat.dto.response.ChatRoomListResult;
import com.puppytalk.chat.dto.response.MessageListResult;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional(readOnly = true)
public class ChatFacade {

    private final ChatDomainService chatDomainService;

    public ChatFacade(ChatDomainService chatDomainService) {
        this.chatDomainService = chatDomainService;
    }

    /**
     * 채팅방 생성 (또는 기존 채팅방 조회)
     */
    @Transactional
    public ChatRoomCreateResponse createOrFindChatRoom(ChatRoomCreateCommand command) {
        Assert.notNull(command, "ChatRoomCreateCommand must not be null");

        UserId userId = UserId.of(command.userId());
        PetId petId = PetId.of(command.petId());

        ChatDomainService.ChatRoomResult result = chatDomainService.
            findOrCreateChatRoom(userId, petId);

        if (result.isNewlyCreated()) {
            return ChatRoomCreateResponse.created(result.chatRoom());
        }
        return ChatRoomCreateResponse.existing(result.chatRoom());
    }

    /**
     * 사용자 메시지 전송
     */
    @Transactional
    public void sendUserMessage(MessageSendCommand command) {
        Assert.notNull(command, "MessageSendCommand must not be null");

        ChatRoomId chatRoomId = ChatRoomId.of(command.chatRoomId());
        UserId userId = UserId.of(command.userId());

        chatDomainService.sendUserMessage(chatRoomId, userId, command.content());
    }

    /**
     * 사용자의 채팅방 목록 조회
     */
    public ChatRoomListResult getChatRoomList(ChatRoomListQuery query) {
        Assert.notNull(query, "ChatRoomListQuery must not be null");

        UserId userId = UserId.of(query.userId());
        List<ChatRoom> chatRooms = chatDomainService.findChatRoomList(userId);

        return ChatRoomListResult.from(chatRooms);
    }

    /**
     * 메시지 목록 조회 (커서 기반 페이징)
     */
    public MessageListResult getMessageList(MessageListQuery query) {
        Assert.notNull(query, "MessageListQuery must not be null");

        ChatRoomId chatRoomId = ChatRoomId.of(query.chatRoomId());
        UserId userId = UserId.of(query.userId());

        MessageId cursor = query.cursor() != null ? MessageId.of(query.cursor()) : null;
        int size = query.getSize();

        List<Message> messages = chatDomainService.findMessageListWithCursor(
            chatRoomId, userId, cursor, size
        );

        return MessageListResult.withCursor(messages, size);
    }
}