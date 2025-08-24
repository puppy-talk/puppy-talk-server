package com.puppytalk.chat.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puppytalk.chat.ChatFacade;
import com.puppytalk.chat.dto.request.ChatRoomCreateRequest;
import com.puppytalk.chat.dto.request.MessageSendRequest;
import com.puppytalk.chat.dto.response.ChatRoomCreateResponse;
import com.puppytalk.chat.dto.response.ChatRoomListResult;
import com.puppytalk.chat.dto.response.MessageListResult;
import com.puppytalk.chat.dto.response.NewMessageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.puppytalk.chat.ChatController.class)
@DisplayName("ChatController 통합 테스트")
class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatFacade chatFacade;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("채팅방 생성/조회 API 테스트")
    class ChatRoomCreationApiTest {

        @Test
        @DisplayName("채팅방 생성 API - 성공 (201 Created)")
        void createChatRoom_Success_Returns201() throws Exception {
            // Given
            ChatRoomCreateRequest request = new ChatRoomCreateRequest(1L, 1L);
            ChatRoomCreateResponse mockResponse = createMockChatRoomCreateResponse(true);

            given(chatFacade.createOrFindChatRoom(any())).willReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/chat/rooms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.chatRoom.id").value(1L))
                .andExpect(jsonPath("$.message").value("채팅방이 성공적으로 생성되었습니다"));
        }

        @Test
        @DisplayName("기존 채팅방 조회 API - 성공 (200 OK)")
        void findExistingChatRoom_Success_Returns200() throws Exception {
            // Given
            ChatRoomCreateRequest request = new ChatRoomCreateRequest(1L, 1L);
            ChatRoomCreateResponse mockResponse = createMockChatRoomCreateResponse(false);

            given(chatFacade.createOrFindChatRoom(any())).willReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/chat/rooms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.chatRoom.id").value(1L))
                .andExpect(jsonPath("$.message").value("채팅방을 성공적으로 조회했습니다"));
        }

        @Test
        @DisplayName("잘못된 요청으로 채팅방 생성 - 실패 (400 Bad Request)")
        void createChatRoom_WithInvalidRequest_Returns400() throws Exception {
            // Given - userId가 null인 잘못된 요청
            String invalidRequest = """
                {
                    "userId": null,
                    "petId": 1
                }
                """;

            // When & Then
            mockMvc.perform(post("/api/chat/rooms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidRequest))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("채팅방 목록 조회 API 테스트")
    class ChatRoomListApiTest {

        @Test
        @DisplayName("채팅방 목록 조회 API - 성공")
        void getChatRoomList_Success() throws Exception {
            // Given
            ChatRoomListResult mockResult = createMockChatRoomListResult();
            given(chatFacade.getChatRoomList(any())).willReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/chat/rooms")
                    .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.chatRooms").isArray())
                .andExpect(jsonPath("$.message").value("채팅방 목록을 성공적으로 조회했습니다"));
        }

        @Test
        @DisplayName("잘못된 사용자 ID로 채팅방 목록 조회 - 실패")
        void getChatRoomList_WithInvalidUserId_Returns400() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/chat/rooms")
                    .param("userId", "-1"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("메시지 전송 API 테스트")
    class MessageSendApiTest {

        @Test
        @DisplayName("메시지 전송 API - 성공")
        void sendMessage_Success() throws Exception {
            // Given
            MessageSendRequest request = new MessageSendRequest("안녕하세요!");

            // When & Then
            mockMvc.perform(post("/api/chat/rooms/1/messages")
                    .param("userId", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpected(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("메시지가 성공적으로 전송되었습니다"));

            verify(chatFacade).sendUserMessage(any());
        }

        @Test
        @DisplayName("빈 메시지 전송 - 실패")
        void sendMessage_WithEmptyContent_Returns400() throws Exception {
            // Given
            MessageSendRequest request = new MessageSendRequest("");

            // When & Then
            mockMvc.perform(post("/api/chat/rooms/1/messages")
                    .param("userId", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("메시지 목록 조회 API 테스트")
    class MessageListApiTest {

        @Test
        @DisplayName("메시지 목록 조회 API - 성공")
        void getMessageList_Success() throws Exception {
            // Given
            MessageListResult mockResult = createMockMessageListResult();
            given(chatFacade.getMessageList(any())).willReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/chat/rooms/1/messages")
                    .param("userId", "1")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.messages").isArray())
                .andExpect(jsonPath("$.message").value("메시지 목록을 성공적으로 조회했습니다"));
        }

        @Test
        @DisplayName("커서와 함께 메시지 목록 조회 API - 성공")
        void getMessageList_WithCursor_Success() throws Exception {
            // Given
            MessageListResult mockResult = createMockMessageListResult();
            given(chatFacade.getMessageList(any())).willReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/chat/rooms/1/messages")
                    .param("userId", "1")
                    .param("cursor", "10")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("새 메시지 조회 (폴링) API 테스트")
    class NewMessagePollingApiTest {

        @Test
        @DisplayName("새 메시지 조회 API - 새 메시지 있음")
        void getNewMessages_WithNewMessages_Success() throws Exception {
            // Given
            LocalDateTime since = LocalDateTime.now().minusMinutes(10);
            NewMessageResult mockResult = createMockNewMessageResult(true);
            given(chatFacade.getNewMessages(any())).willReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/chat/rooms/1/messages/new")
                    .param("userId", "1")
                    .param("since", since.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasNewMessages").value(true))
                .andExpect(jsonPath("$.message").value("새 메시지가 있습니다"));
        }

        @Test
        @DisplayName("새 메시지 조회 API - 새 메시지 없음")
        void getNewMessages_NoNewMessages_Success() throws Exception {
            // Given
            LocalDateTime since = LocalDateTime.now().minusMinutes(10);
            NewMessageResult mockResult = createMockNewMessageResult(false);
            given(chatFacade.getNewMessages(any())).willReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/chat/rooms/1/messages/new")
                    .param("userId", "1")
                    .param("since", since.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasNewMessages").value(false))
                .andExpect(jsonPath("$.message").value("새 메시지가 없습니다"));
        }

        @Test
        @DisplayName("잘못된 시간 형식으로 새 메시지 조회 - 실패")
        void getNewMessages_WithInvalidTimeFormat_Returns400() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/chat/rooms/1/messages/new")
                    .param("userId", "1")
                    .param("since", "invalid-time-format"))
                .andExpect(status().isBadRequest());
        }
    }

    // Helper methods
    private ChatRoomCreateResponse createMockChatRoomCreateResponse(boolean isNewlyCreated) {
        com.puppytalk.chat.dto.response.ChatRoomResult chatRoom = 
            com.puppytalk.chat.dto.response.ChatRoomResult.of(
                com.puppytalk.chat.ChatRoomId.of(1L),
                com.puppytalk.user.UserId.of(1L),
                com.puppytalk.pet.PetId.of(1L),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()
            );
        
        return isNewlyCreated ? 
            ChatRoomCreateResponse.created(chatRoom) : 
            ChatRoomCreateResponse.existing(chatRoom);
    }

    private ChatRoomListResult createMockChatRoomListResult() {
        return ChatRoomListResult.from(List.of());
    }

    private MessageListResult createMockMessageListResult() {
        return MessageListResult.withCursor(List.of(), 20);
    }

    private NewMessageResult createMockNewMessageResult(boolean hasNewMessages) {
        if (hasNewMessages) {
            return NewMessageResult.from(List.of(createMockMessage()));
        } else {
            return NewMessageResult.from(List.of());
        }
    }

    private com.puppytalk.chat.Message createMockMessage() {
        return com.puppytalk.chat.Message.restore(
            com.puppytalk.chat.MessageId.of(1L),
            com.puppytalk.chat.ChatRoomId.of(1L),
            com.puppytalk.chat.MessageType.USER,
            "테스트 메시지",
            LocalDateTime.now()
        );
    }
}