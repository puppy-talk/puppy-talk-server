package com.puppytalk.chat.domain;

import com.puppytalk.chat.ChatDomainService;
import com.puppytalk.chat.ChatRoom;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.chat.ChatRoomRepository;
import com.puppytalk.chat.ChatRoomResult;
import com.puppytalk.chat.Message;
import com.puppytalk.chat.MessageId;
import com.puppytalk.chat.MessageRepository;
import com.puppytalk.chat.MessageType;
import com.puppytalk.chat.exception.ChatRoomAccessDeniedException;
import com.puppytalk.chat.exception.ChatRoomNotFoundException;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ChatDomainService 테스트")
class ChatDomainServiceTest {

    private ChatRoomRepository chatRoomRepository;
    private MessageRepository messageRepository;
    private ChatDomainService chatDomainService;

    @BeforeEach
    void setUp() {
        chatRoomRepository = new MockChatRoomRepository();
        messageRepository = new MockMessageRepository();
        chatDomainService = new ChatDomainService(chatRoomRepository, messageRepository);
    }

    @Nested
    @DisplayName("채팅방 생성/조회 테스트")
    class ChatRoomCreationTest {

        @Test
        @DisplayName("새 채팅방 생성 - 성공")
        void findOrCreateChatRoom_NewChatRoom_Success() {
            // Given
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);

            // When
            ChatRoomResult result = chatDomainService.findOrCreateChatRoom(userId, petId);

            // Then
            assertThat(result.isNewlyCreated()).isTrue();
            assertThat(result.chatRoom().userId()).isEqualTo(userId);
            assertThat(result.chatRoom().petId()).isEqualTo(petId);
        }

        @Test
        @DisplayName("기존 채팅방 조회 - 성공")
        void findOrCreateChatRoom_ExistingChatRoom_Success() {
            // Given
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);
            
            // 기존 채팅방 생성
            ChatRoom existingChatRoom = ChatRoom.of(
                ChatRoomId.of(1L), userId, petId, 
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()
            );
            ((MockChatRoomRepository) chatRoomRepository).addChatRoom(existingChatRoom);

            // When
            ChatRoomResult result = chatDomainService.findOrCreateChatRoom(userId, petId);

            // Then
            assertThat(result.isNewlyCreated()).isFalse();
            assertThat(result.chatRoom().id()).isEqualTo(ChatRoomId.of(1L));
        }

        @Test
        @DisplayName("채팅방 생성 시 사용자 ID가 null이면 예외 발생")
        void findOrCreateChatRoom_WithNullUserId_ThrowsException() {
            // Given
            UserId userId = null;
            PetId petId = PetId.of(1L);

            // When & Then
            assertThatThrownBy(() -> chatDomainService.findOrCreateChatRoom(userId, petId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserId must not be null");
        }
    }

    @Nested
    @DisplayName("메시지 전송 테스트")
    class MessageSendTest {

        @Test
        @DisplayName("사용자 메시지 전송 - 성공")
        void sendUserMessage_Success() {
            // Given
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);
            ChatRoom chatRoom = createChatRoom(userId, petId);
            String content = "안녕하세요!";

            // When
            assertThatNoException().isThrownBy(() -> 
                chatDomainService.sendUserMessage(chatRoom.id(), userId, content)
            );

            // Then
            List<Message> messages = ((MockMessageRepository) messageRepository).getMessages();
            assertThat(messages).hasSize(1);
            assertThat(messages.get(0).type()).isEqualTo(MessageType.USER);
            assertThat(messages.get(0).content()).isEqualTo(content);
        }

        @Test
        @DisplayName("반려동물 메시지 전송 - 성공")
        void sendPetMessage_Success() {
            // Given
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);
            ChatRoom chatRoom = createChatRoom(userId, petId);
            String content = "멍멍! 안녕!";

            // When
            Message result = chatDomainService.sendPetMessage(chatRoom.id(), content);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(MessageType.PET);
            assertThat(result.content()).isEqualTo(content);
        }

        @Test
        @DisplayName("메시지 전송 시 내용이 빈 문자열이면 예외 발생")
        void sendUserMessage_WithEmptyContent_ThrowsException() {
            // Given
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);
            ChatRoom chatRoom = createChatRoom(userId, petId);
            String content = "   ";

            // When & Then
            assertThatThrownBy(() -> 
                chatDomainService.sendUserMessage(chatRoom.id(), userId, content)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessage("Message content must not be null or empty");
        }

        @Test
        @DisplayName("존재하지 않는 채팅방에 메시지 전송 시 예외 발생")
        void sendUserMessage_WithNonExistentChatRoom_ThrowsException() {
            // Given
            ChatRoomId nonExistentChatRoomId = ChatRoomId.of(999L);
            UserId userId = UserId.of(1L);
            String content = "테스트";

            // When & Then
            assertThatThrownBy(() -> 
                chatDomainService.sendUserMessage(nonExistentChatRoomId, userId, content)
            ).isInstanceOf(ChatRoomNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("접근 권한 검증 테스트")
    class AccessValidationTest {

        @Test
        @DisplayName("채팅방 소유자가 아닌 경우 접근 거부")
        void validateChatRoom_NonOwner_ThrowsAccessDeniedException() {
            // Given
            UserId owner = UserId.of(1L);
            UserId nonOwner = UserId.of(2L);
            PetId petId = PetId.of(1L);
            ChatRoom chatRoom = createChatRoom(owner, petId);

            // When & Then
            assertThatThrownBy(() -> 
                chatDomainService.validateChatRoom(chatRoom.id(), nonOwner)
            ).isInstanceOf(ChatRoomAccessDeniedException.class)
             .hasMessage("채팅방에 접근할 권한이 없습니다");
        }

        @Test
        @DisplayName("채팅방 소유자인 경우 접근 허용")
        void validateChatRoom_Owner_Success() {
            // Given
            UserId owner = UserId.of(1L);
            PetId petId = PetId.of(1L);
            ChatRoom chatRoom = createChatRoom(owner, petId);

            // When & Then
            assertThatNoException().isThrownBy(() -> 
                chatDomainService.validateChatRoom(chatRoom.id(), owner)
            );
        }
    }

    @Nested
    @DisplayName("폴링 기능 테스트")
    class PollingTest {

        @Test
        @DisplayName("특정 시간 이후 새 메시지 조회 - 성공")
        void findNewMessages_Success() {
            // Given
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);
            ChatRoom chatRoom = createChatRoom(userId, petId);
            LocalDateTime since = LocalDateTime.now().minusMinutes(10);
            
            // 새 메시지 추가
            Message recentMessage = Message.restore(
                MessageId.of(1L), chatRoom.id(), MessageType.PET, 
                "새 메시지", LocalDateTime.now().minusMinutes(5)
            );
            ((MockMessageRepository) messageRepository).addMessage(recentMessage);

            // When
            List<Message> result = chatDomainService.findNewMessages(chatRoom.id(), userId, since);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(recentMessage);
        }

        @Test
        @DisplayName("기준 시간이 null인 경우 예외 발생")
        void findNewMessages_WithNullSince_ThrowsException() {
            // Given
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);
            ChatRoom chatRoom = createChatRoom(userId, petId);
            LocalDateTime since = null;

            // When & Then
            assertThatThrownBy(() -> 
                chatDomainService.findNewMessages(chatRoom.id(), userId, since)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessage("Since time must not be null");
        }
    }

    // Helper method
    private ChatRoom createChatRoom(UserId userId, PetId petId) {
        ChatRoom chatRoom = ChatRoom.of(
            ChatRoomId.of(1L), userId, petId,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now()
        );
        ((MockChatRoomRepository) chatRoomRepository).addChatRoom(chatRoom);
        return chatRoom;
    }
}