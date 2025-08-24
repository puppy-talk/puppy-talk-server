package com.puppytalk.chat.domain;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.chat.Message;
import com.puppytalk.chat.MessageId;
import com.puppytalk.chat.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Message 도메인 테스트")
class MessageTest {

    @Nested
    @DisplayName("메시지 생성 테스트")
    class MessageCreationTest {

        @Test
        @DisplayName("사용자 메시지 생성 - 성공")
        void createUserMessage_Success() {
            // Given
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            String content = "안녕하세요!";

            // When
            Message message = Message.of(chatRoomId, content);

            // Then
            assertThat(message).isNotNull();
            assertThat(message.chatRoomId()).isEqualTo(chatRoomId);
            assertThat(message.type()).isEqualTo(MessageType.USER);
            assertThat(message.content()).isEqualTo("안녕하세요!");
            assertThat(message.isFromUser()).isTrue();
            assertThat(message.isFromPet()).isFalse();
            assertThat(message.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("반려동물 메시지 생성 - 성공")
        void createPetMessage_Success() {
            // Given
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            String content = "멍멍! 안녕!";

            // When
            Message message = Message.createPetMessage(chatRoomId, content);

            // Then
            assertThat(message).isNotNull();
            assertThat(message.chatRoomId()).isEqualTo(chatRoomId);
            assertThat(message.type()).isEqualTo(MessageType.PET);
            assertThat(message.content()).isEqualTo("멍멍! 안녕!");
            assertThat(message.isFromUser()).isFalse();
            assertThat(message.isFromPet()).isTrue();
        }

        @Test
        @DisplayName("저장된 메시지 복원 - 성공")
        void restoreMessage_Success() {
            // Given
            MessageId messageId = MessageId.of(1L);
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            MessageType type = MessageType.USER;
            String content = "테스트 메시지";
            LocalDateTime createdAt = LocalDateTime.now().minusHours(1);

            // When
            Message message = Message.restore(messageId, chatRoomId, type, content, createdAt);

            // Then
            assertThat(message.id()).isEqualTo(messageId);
            assertThat(message.chatRoomId()).isEqualTo(chatRoomId);
            assertThat(message.type()).isEqualTo(type);
            assertThat(message.content()).isEqualTo(content);
            assertThat(message.createdAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("메시지 생성 실패 테스트")
    class MessageCreationFailureTest {

        @Test
        @DisplayName("채팅방 ID가 null인 경우 예외 발생")
        void createMessage_WithNullChatRoomId_ThrowsException() {
            // Given
            ChatRoomId chatRoomId = null;
            String content = "테스트 메시지";

            // When & Then
            assertThatThrownBy(() -> Message.of(chatRoomId, content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("채팅방 ID는 필수입니다");
        }

        @Test
        @DisplayName("메시지 내용이 null인 경우 예외 발생")
        void createMessage_WithNullContent_ThrowsException() {
            // Given
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            String content = null;

            // When & Then
            assertThatThrownBy(() -> Message.of(chatRoomId, content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("메시지 내용은 필수입니다");
        }

        @Test
        @DisplayName("메시지 내용이 빈 문자열인 경우 예외 발생")
        void createMessage_WithEmptyContent_ThrowsException() {
            // Given
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            String content = "   ";

            // When & Then
            assertThatThrownBy(() -> Message.of(chatRoomId, content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("메시지 내용은 필수입니다");
        }

        @Test
        @DisplayName("메시지 내용이 1000자 초과인 경우 예외 발생")
        void createMessage_WithTooLongContent_ThrowsException() {
            // Given
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            String content = "a".repeat(1001);

            // When & Then
            assertThatThrownBy(() -> Message.of(chatRoomId, content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("메시지 내용은 1000자를 초과할 수 없습니다");
        }

        @Test
        @DisplayName("저장된 메시지 복원 시 ID가 null인 경우 예외 발생")
        void restoreMessage_WithNullId_ThrowsException() {
            // Given
            MessageId messageId = null;
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            MessageType type = MessageType.USER;
            String content = "테스트";
            LocalDateTime createdAt = LocalDateTime.now();

            // When & Then
            assertThatThrownBy(() -> Message.restore(messageId, chatRoomId, type, content, createdAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("저장된 메시지 ID가 필요합니다");
        }
    }

    @Nested
    @DisplayName("메시지 동작 테스트")
    class MessageBehaviorTest {

        @Test
        @DisplayName("채팅방 소속 확인 - 동일한 채팅방")
        void belongsToChatRoom_SameChatRoom_ReturnsTrue() {
            // Given
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            Message message = Message.of(chatRoomId, "테스트");

            // When
            boolean result = message.belongsToChatRoom(chatRoomId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("채팅방 소속 확인 - 다른 채팅방")
        void belongsToChatRoom_DifferentChatRoom_ReturnsFalse() {
            // Given
            ChatRoomId chatRoomId1 = ChatRoomId.of(1L);
            ChatRoomId chatRoomId2 = ChatRoomId.of(2L);
            Message message = Message.of(chatRoomId1, "테스트");

            // When
            boolean result = message.belongsToChatRoom(chatRoomId2);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("메시지 내용 공백 제거 확인")
        void createMessage_TrimsContent() {
            // Given
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            String contentWithSpaces = "  테스트 메시지  ";

            // When
            Message message = Message.of(chatRoomId, contentWithSpaces);

            // Then
            assertThat(message.content()).isEqualTo("테스트 메시지");
        }
    }

    @Nested
    @DisplayName("메시지 동등성 테스트")
    class MessageEqualityTest {

        @Test
        @DisplayName("동일한 ID를 가진 메시지는 동등하다")
        void equals_SameId_ReturnsTrue() {
            // Given
            MessageId messageId = MessageId.of(1L);
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            LocalDateTime now = LocalDateTime.now();
            
            Message message1 = Message.restore(messageId, chatRoomId, MessageType.USER, "내용1", now);
            Message message2 = Message.restore(messageId, chatRoomId, MessageType.PET, "내용2", now);

            // When & Then
            assertThat(message1).isEqualTo(message2);
            assertThat(message1.hashCode()).isEqualTo(message2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 메시지는 동등하지 않다")
        void equals_DifferentId_ReturnsFalse() {
            // Given
            MessageId messageId1 = MessageId.of(1L);
            MessageId messageId2 = MessageId.of(2L);
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            LocalDateTime now = LocalDateTime.now();
            
            Message message1 = Message.restore(messageId1, chatRoomId, MessageType.USER, "내용", now);
            Message message2 = Message.restore(messageId2, chatRoomId, MessageType.USER, "내용", now);

            // When & Then
            assertThat(message1).isNotEqualTo(message2);
        }
    }
}