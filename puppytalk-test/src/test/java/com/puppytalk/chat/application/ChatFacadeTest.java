package com.puppytalk.chat.application;

import com.puppytalk.chat.ChatDomainService;
import com.puppytalk.chat.ChatFacade;
import com.puppytalk.chat.ChatRoom;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.chat.Message;
import com.puppytalk.chat.MessageId;
import com.puppytalk.chat.MessageType;
import com.puppytalk.chat.dto.request.ChatRoomCreateCommand;
import com.puppytalk.chat.dto.request.ChatRoomListQuery;
import com.puppytalk.chat.dto.request.MessageListQuery;
import com.puppytalk.chat.dto.request.MessageSendCommand;
import com.puppytalk.chat.dto.request.NewMessageQuery;
import com.puppytalk.chat.dto.response.ChatRoomCreateResponse;
import com.puppytalk.chat.dto.response.ChatRoomListResult;
import com.puppytalk.chat.dto.response.MessageListResult;
import com.puppytalk.chat.dto.response.NewMessageResult;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatFacade 테스트")
class ChatFacadeTest {

    @Mock
    private ChatDomainService chatDomainService;

    private ChatFacade chatFacade;

    @BeforeEach
    void setUp() {
        chatFacade = new ChatFacade(chatDomainService);
    }

    @Nested
    @DisplayName("채팅방 생성/조회 테스트")
    class ChatRoomCreationTest {

        @Test
        @DisplayName("새 채팅방 생성 - 성공")
        void createOrFindChatRoom_NewChatRoom_Success() {
            // Given
            ChatRoomCreateCommand command = ChatRoomCreateCommand.of(1L, 1L);
            ChatRoom chatRoom = createSampleChatRoom();
            com.puppytalk.chat.ChatRoomResult domainResult = 
                com.puppytalk.chat.ChatRoomResult.created(chatRoom);

            given(chatDomainService.findOrCreateChatRoom(any(UserId.class), any(PetId.class)))
                .willReturn(domainResult);

            // When
            ChatRoomCreateResponse response = chatFacade.createOrFindChatRoom(command);

            // Then
            assertThat(response.isNewlyCreated()).isTrue();
            assertThat(response.chatRoom().id()).isEqualTo(ChatRoomId.of(1L));
            verify(chatDomainService).findOrCreateChatRoom(UserId.of(1L), PetId.of(1L));
        }

        @Test
        @DisplayName("기존 채팅방 조회 - 성공")
        void createOrFindChatRoom_ExistingChatRoom_Success() {
            // Given
            ChatRoomCreateCommand command = ChatRoomCreateCommand.of(1L, 1L);
            ChatRoom chatRoom = createSampleChatRoom();
            com.puppytalk.chat.ChatRoomResult domainResult = 
                com.puppytalk.chat.ChatRoomResult.existing(chatRoom);

            given(chatDomainService.findOrCreateChatRoom(any(UserId.class), any(PetId.class)))
                .willReturn(domainResult);

            // When
            ChatRoomCreateResponse response = chatFacade.createOrFindChatRoom(command);

            // Then
            assertThat(response.isNewlyCreated()).isFalse();
            assertThat(response.chatRoom().id()).isEqualTo(ChatRoomId.of(1L));
        }

        @Test
        @DisplayName("null 커맨드로 채팅방 생성 시 예외 발생")
        void createOrFindChatRoom_WithNullCommand_ThrowsException() {
            // Given
            ChatRoomCreateCommand command = null;

            // When & Then
            assertThatThrownBy(() -> chatFacade.createOrFindChatRoom(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ChatRoomCreateCommand must not be null");
        }
    }

    @Nested
    @DisplayName("메시지 전송 테스트")
    class MessageSendTest {

        @Test
        @DisplayName("사용자 메시지 전송 - 성공")
        void sendUserMessage_Success() {
            // Given
            MessageSendCommand command = MessageSendCommand.of(1L, 1L, "안녕하세요!");

            // When
            assertThatNoException().isThrownBy(() -> chatFacade.sendUserMessage(command));

            // Then
            verify(chatDomainService).sendUserMessage(
                ChatRoomId.of(1L), 
                UserId.of(1L), 
                "안녕하세요!"
            );
        }

        @Test
        @DisplayName("null 커맨드로 메시지 전송 시 예외 발생")
        void sendUserMessage_WithNullCommand_ThrowsException() {
            // Given
            MessageSendCommand command = null;

            // When & Then
            assertThatThrownBy(() -> chatFacade.sendUserMessage(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("MessageSendCommand must not be null");
        }
    }

    @Nested
    @DisplayName("채팅방 목록 조회 테스트")
    class ChatRoomListTest {

        @Test
        @DisplayName("채팅방 목록 조회 - 성공")
        void getChatRoomList_Success() {
            // Given
            ChatRoomListQuery query = ChatRoomListQuery.of(1L);
            List<ChatRoom> chatRooms = List.of(createSampleChatRoom());

            given(chatDomainService.findChatRoomList(any(UserId.class)))
                .willReturn(chatRooms);

            // When
            ChatRoomListResult result = chatFacade.getChatRoomList(query);

            // Then
            assertThat(result.chatRooms()).hasSize(1);
            verify(chatDomainService).findChatRoomList(UserId.of(1L));
        }

        @Test
        @DisplayName("null 쿼리로 채팅방 목록 조회 시 예외 발생")
        void getChatRoomList_WithNullQuery_ThrowsException() {
            // Given
            ChatRoomListQuery query = null;

            // When & Then
            assertThatThrownBy(() -> chatFacade.getChatRoomList(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ChatRoomListQuery must not be null");
        }
    }

    @Nested
    @DisplayName("메시지 목록 조회 테스트")
    class MessageListTest {

        @Test
        @DisplayName("메시지 목록 조회 (커서 없음) - 성공")
        void getMessageList_WithoutCursor_Success() {
            // Given
            MessageListQuery query = MessageListQuery.of(1L, 1L, null, 20);
            List<Message> messages = List.of(createSampleMessage());

            given(chatDomainService.findMessageListWithCursor(
                any(ChatRoomId.class), any(UserId.class), isNull(), eq(20)
            )).willReturn(messages);

            // When
            MessageListResult result = chatFacade.getMessageList(query);

            // Then
            assertThat(result.messages()).hasSize(1);
            verify(chatDomainService).findMessageListWithCursor(
                ChatRoomId.of(1L), UserId.of(1L), null, 20
            );
        }

        @Test
        @DisplayName("메시지 목록 조회 (커서 있음) - 성공")
        void getMessageList_WithCursor_Success() {
            // Given
            MessageListQuery query = MessageListQuery.of(1L, 1L, 5L, 20);
            List<Message> messages = List.of(createSampleMessage());

            given(chatDomainService.findMessageListWithCursor(
                any(ChatRoomId.class), any(UserId.class), any(MessageId.class), eq(20)
            )).willReturn(messages);

            // When
            MessageListResult result = chatFacade.getMessageList(query);

            // Then
            assertThat(result.messages()).hasSize(1);
            verify(chatDomainService).findMessageListWithCursor(
                ChatRoomId.of(1L), UserId.of(1L), MessageId.of(5L), 20
            );
        }
    }

    @Nested
    @DisplayName("새 메시지 조회 (폴링) 테스트")
    class NewMessagePollingTest {

        @Test
        @DisplayName("새 메시지 조회 - 성공")
        void getNewMessages_Success() {
            // Given
            LocalDateTime since = LocalDateTime.now().minusMinutes(10);
            NewMessageQuery query = NewMessageQuery.of(1L, 1L, since);
            List<Message> newMessages = List.of(createSampleMessage());

            given(chatDomainService.findNewMessages(
                any(ChatRoomId.class), any(UserId.class), any(LocalDateTime.class)
            )).willReturn(newMessages);

            // When
            NewMessageResult result = chatFacade.getNewMessages(query);

            // Then
            assertThat(result.hasNewMessages()).isTrue();
            assertThat(result.messages()).hasSize(1);
            verify(chatDomainService).findNewMessages(
                ChatRoomId.of(1L), UserId.of(1L), since
            );
        }

        @Test
        @DisplayName("새 메시지가 없는 경우")
        void getNewMessages_NoNewMessages_Success() {
            // Given
            LocalDateTime since = LocalDateTime.now().minusMinutes(10);
            NewMessageQuery query = NewMessageQuery.of(1L, 1L, since);
            List<Message> newMessages = List.of();

            given(chatDomainService.findNewMessages(
                any(ChatRoomId.class), any(UserId.class), any(LocalDateTime.class)
            )).willReturn(newMessages);

            // When
            NewMessageResult result = chatFacade.getNewMessages(query);

            // Then
            assertThat(result.hasNewMessages()).isFalse();
            assertThat(result.messages()).isEmpty();
        }

        @Test
        @DisplayName("null 쿼리로 새 메시지 조회 시 예외 발생")
        void getNewMessages_WithNullQuery_ThrowsException() {
            // Given
            NewMessageQuery query = null;

            // When & Then
            assertThatThrownBy(() -> chatFacade.getNewMessages(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("NewMessageQuery must not be null");
        }
    }

    // Helper methods
    private ChatRoom createSampleChatRoom() {
        return ChatRoom.of(
            ChatRoomId.of(1L),
            UserId.of(1L),
            PetId.of(1L),
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now()
        );
    }

    private Message createSampleMessage() {
        return Message.restore(
            MessageId.of(1L),
            ChatRoomId.of(1L),
            MessageType.USER,
            "테스트 메시지",
            LocalDateTime.now()
        );
    }
}