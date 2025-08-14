package com.puppy.talk.service;

import com.puppy.talk.ai.AiResponsePort;
import com.puppy.talk.chat.ChatRoomRepository;
import com.puppy.talk.chat.MessageRepository;
import com.puppy.talk.pet.PetRepository;
import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.Message;
import com.puppy.talk.chat.MessageIdentity;
import com.puppy.talk.chat.SenderType;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.pet.Persona;
import com.puppy.talk.pet.PersonaIdentity;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.websocket.ChatMessage;
import com.puppy.talk.websocket.ChatMessageType;
import com.puppy.talk.chat.ChatService;
import com.puppy.talk.dto.MessageSendResult;
import com.puppy.talk.chat.command.MessageSendCommand;
import com.puppy.talk.notification.RealtimeNotificationPort;
import com.puppy.talk.pet.PersonaLookUpService;
import com.puppy.talk.websocket.WebSocketChatService;
import com.puppy.talk.pet.PetNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService WebSocket 연동 비즈니스 로직 테스트")
class ChatServiceWebSocketIntegrationTest {
    
    @Mock
    private ChatRoomRepository chatRoomRepository;
    
    @Mock
    private MessageRepository messageRepository;
    
    @Mock
    private PetRepository petRepository;
    
    @Mock
    private PersonaLookUpService personaLookUpService;
    
    @Mock
    private AiResponsePort aiResponsePort;
    
    @Mock
    private RealtimeNotificationPort realtimeNotificationPort;
    
    @Mock
    private WebSocketChatService webSocketChatService;
    
    @InjectMocks
    private ChatService chatService;
    
    private ChatRoomIdentity chatRoomId;
    private PetIdentity petId;
    private UserIdentity userId;
    private Pet mockPet;
    private ChatRoom mockChatRoom;
    private Persona mockPersona;
    
    @BeforeEach
    void setUp() {
        chatRoomId = ChatRoomIdentity.of(1L);
        petId = PetIdentity.of(1L);
        userId = UserIdentity.of(1L);
        
        mockPet = new Pet(petId, userId, PersonaIdentity.of(1L), "멍멍이", "골든리트리버", 3, null);
        mockChatRoom = new ChatRoom(chatRoomId, petId, "멍멍이와의 채팅방", LocalDateTime.now());
        mockPersona = new Persona(PersonaIdentity.of(1L), "친근한 펫", "활발하고 친근한 성격", 
            "밝고 긍정적", "당신은 친근한 펫입니다.", true);
    }
    
    @Test
    @DisplayName("메시지 전송 시 AI 응답과 WebSocket 브로드캐스트가 함께 실행 - 성공")
    void sendMessageToPet_WithWebSocketBroadcast_Success() {
        // Given
        String userMessageContent = "안녕 멍멍이!";
        String aiResponse = "안녕하세요! 오늘도 좋은 하루예요! 🐾";
        
        Message savedUserMessage = new Message(
            MessageIdentity.of(1L), chatRoomId, SenderType.USER, userMessageContent, true, LocalDateTime.now()
        );
        
        Message savedPetMessage = new Message(
            MessageIdentity.of(2L), chatRoomId, SenderType.PET, aiResponse, false, LocalDateTime.now()
        );
        
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId())).thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(List.of());
        when(aiResponsePort.generatePetResponse(eq(mockPet), eq(mockPersona), eq(userMessageContent), any()))
            .thenReturn(aiResponse);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedUserMessage)
            .thenReturn(savedPetMessage);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        
        // When
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, MessageSendCommand.of(userMessageContent));
        
        // Then
        // 기본적인 메시지 처리 검증
        assertThat(result.message().content()).isEqualTo(userMessageContent);
        assertThat(result.message().senderType()).isEqualTo(SenderType.USER);
        
        // WebSocket 브로드캐스트 호출 검증
        ArgumentCaptor<ChatMessage> chatMessageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(webSocketChatService).broadcastMessage(chatMessageCaptor.capture());
        
        ChatMessage broadcastedMessage = chatMessageCaptor.getValue();
        assertThat(broadcastedMessage.messageId()).isEqualTo(MessageIdentity.of(2L));
        assertThat(broadcastedMessage.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(broadcastedMessage.userId()).isEqualTo(userId);
        assertThat(broadcastedMessage.senderType()).isEqualTo(SenderType.PET);
        assertThat(broadcastedMessage.content()).isEqualTo(aiResponse);
        assertThat(broadcastedMessage.isRead()).isFalse();
        assertThat(broadcastedMessage.messageType()).isEqualTo(ChatMessageType.MESSAGE);
        
        // AI 응답 생성 호출 검증
        verify(aiResponsePort).generatePetResponse(eq(mockPet), eq(mockPersona), eq(userMessageContent), any());
        
        // 메시지 저장 호출 검증 (사용자 메시지 + AI 응답)
        verify(messageRepository, times(2)).save(any(Message.class));
    }
    
    @Test
    @DisplayName("AI 응답 생성 실패 시에도 사용자 메시지는 정상 처리되고 WebSocket 브로드캐스트 없음 - 성공")
    void sendMessageToPet_AiResponseFails_UserMessageStillProcessed() {
        // Given
        String userMessageContent = "안녕하세요!";
        
        Message savedUserMessage = new Message(
            MessageIdentity.of(1L), chatRoomId, SenderType.USER, userMessageContent, true, LocalDateTime.now()
        );
        
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId())).thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(List.of());
        when(aiResponsePort.generatePetResponse(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("AI 서비스 오류"));
        when(messageRepository.save(any(Message.class))).thenReturn(savedUserMessage);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        
        // When
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, MessageSendCommand.of(userMessageContent));
        
        // Then
        // 사용자 메시지는 정상 처리
        assertThat(result.message().content()).isEqualTo(userMessageContent);
        assertThat(result.message().senderType()).isEqualTo(SenderType.USER);
        
        // AI 응답 실패로 인해 WebSocket 브로드캐스트 호출되지 않음
        verify(webSocketChatService, never()).broadcastMessage(any());
        
        // 사용자 메시지만 저장됨 (AI 응답은 저장되지 않음)
        verify(messageRepository, times(1)).save(any(Message.class));
    }
    
    @Test
    @DisplayName("WebSocket 브로드캐스트 실패 시에도 메시지 저장은 정상 처리 - 성공")
    void sendMessageToPet_WebSocketFails_MessageStillSaved() {
        // Given
        String userMessageContent = "안녕하세요!";
        String aiResponse = "안녕! 반가워요! 🐾";
        
        Message savedUserMessage = new Message(
            MessageIdentity.of(1L), chatRoomId, SenderType.USER, userMessageContent, true, LocalDateTime.now()
        );
        
        Message savedPetMessage = new Message(
            MessageIdentity.of(2L), chatRoomId, SenderType.PET, aiResponse, false, LocalDateTime.now()
        );
        
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId())).thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(List.of());
        when(aiResponsePort.generatePetResponse(any(), any(), any(), any())).thenReturn(aiResponse);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedUserMessage)
            .thenReturn(savedPetMessage);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        
        // WebSocket 브로드캐스트 실패 시뮬레이션
        doThrow(new RuntimeException("WebSocket 연결 오류"))
            .when(webSocketChatService).broadcastMessage(any());
        
        // When & Then
        // WebSocket 실패에도 불구하고 정상 처리되어야 함
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, MessageSendCommand.of(userMessageContent));
        
        assertThat(result.message().content()).isEqualTo(userMessageContent);
        assertThat(result.message().senderType()).isEqualTo(SenderType.USER);
        
        // AI 응답 생성 및 저장 정상 실행
        verify(aiResponsePort).generatePetResponse(any(), any(), any(), any());
        verify(messageRepository, times(2)).save(any(Message.class));
        
        // WebSocket 브로드캐스트 시도는 했지만 실패
        verify(webSocketChatService).broadcastMessage(any());
    }
    
    @Test
    @DisplayName("존재하지 않는 채팅방으로 메시지 전송 시 예외 발생 - 실패")
    void sendMessageToPet_ChatRoomNotFound_ThrowsException() {
        // Given
        String userMessageContent = "안녕하세요!";
        
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> chatService.sendMessageToPet(chatRoomId, MessageSendCommand.of(userMessageContent)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ChatRoom not found");
        
        // WebSocket 관련 메서드 호출되지 않음
        verify(webSocketChatService, never()).broadcastMessage(any());
        verify(aiResponsePort, never()).generatePetResponse(any(), any(), any(), any());
        verify(messageRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("존재하지 않는 펫으로 메시지 전송 시 예외 발생 - 실패")
    void sendMessageToPet_PetNotFound_ThrowsException() {
        // Given
        String userMessageContent = "안녕하세요!";
        
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> chatService.sendMessageToPet(chatRoomId, MessageSendCommand.of(userMessageContent)))
            .isInstanceOf(PetNotFoundException.class);
        
        // WebSocket 관련 메서드 호출되지 않음
        verify(webSocketChatService, never()).broadcastMessage(any());
        verify(aiResponsePort, never()).generatePetResponse(any(), any(), any(), any());
        
        // 펫을 찾지 못해 사용자 메시지도 저장되지 않음
        verify(messageRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("빈 메시지 내용으로 전송 시 예외 발생 - 실패")
    void sendMessageToPet_EmptyContent_ThrowsException() {
        // Given
        String emptyContent = "   ";
        
        // When & Then
        assertThatThrownBy(() -> chatService.sendMessageToPet(chatRoomId, MessageSendCommand.of(emptyContent)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Message content cannot be null or empty");
        
        // 어떤 처리도 수행되지 않음
        verify(chatRoomRepository, never()).findByIdentity(any());
        verify(webSocketChatService, never()).broadcastMessage(any());
        verify(messageRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("AI 응답 내용이 긴 경우에도 WebSocket 브로드캐스트 정상 처리 - 성공")
    void sendMessageToPet_LongAiResponse_WebSocketBroadcastSuccess() {
        // Given
        String userMessageContent = "오늘 날씨가 어때?";
        String longAiResponse = "오늘 날씨는 정말 좋아요! 🌞 하늘이 맑고 파란색이에요. " +
            "이런 날에는 산책하기 딱 좋겠어요! 함께 밖에 나가서 놀면 어떨까요? " +
            "공원에서 뛰어놀거나 새로운 친구들을 만날 수도 있을 것 같아요! " +
            "아니면 집에서 창가에 앉아서 따뜻한 햇살을 쬐는 것도 좋을 것 같아요. 🐾✨";
        
        Message savedUserMessage = new Message(
            MessageIdentity.of(1L), chatRoomId, SenderType.USER, userMessageContent, true, LocalDateTime.now()
        );
        
        Message savedPetMessage = new Message(
            MessageIdentity.of(2L), chatRoomId, SenderType.PET, longAiResponse, false, LocalDateTime.now()
        );
        
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId())).thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(List.of());
        when(aiResponsePort.generatePetResponse(any(), any(), any(), any())).thenReturn(longAiResponse);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedUserMessage)
            .thenReturn(savedPetMessage);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        
        // When
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, MessageSendCommand.of(userMessageContent));
        
        // Then
        assertThat(result.message().content()).isEqualTo(userMessageContent);
        
        // 긴 AI 응답도 정상적으로 WebSocket 브로드캐스트
        ArgumentCaptor<ChatMessage> chatMessageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(webSocketChatService).broadcastMessage(chatMessageCaptor.capture());
        
        ChatMessage broadcastedMessage = chatMessageCaptor.getValue();
        assertThat(broadcastedMessage.content()).isEqualTo(longAiResponse);
        assertThat(broadcastedMessage.content().length()).isGreaterThan(100);
        assertThat(broadcastedMessage.senderType()).isEqualTo(SenderType.PET);
    }
    
    @Test
    @DisplayName("채팅 히스토리가 있는 상태에서 메시지 전송 시 AI 컨텍스트 활용 및 WebSocket 브로드캐스트 - 성공")
    void sendMessageToPet_WithChatHistory_ContextUsedAndWebSocketBroadcast() {
        // Given
        String userMessageContent = "그래서 어떻게 됐어?";
        String contextualAiResponse = "아, 그 이야기 말이군요! 결국 잘 해결됐어요! 😊";
        
        List<Message> chatHistory = List.of(
            new Message(MessageIdentity.of(10L), chatRoomId, SenderType.USER, "어제 산책 어땠어?", true, LocalDateTime.now().minusMinutes(10)),
            new Message(MessageIdentity.of(11L), chatRoomId, SenderType.PET, "정말 즐거웠어요!", false, LocalDateTime.now().minusMinutes(9))
        );
        
        Message savedUserMessage = new Message(
            MessageIdentity.of(12L), chatRoomId, SenderType.USER, userMessageContent, true, LocalDateTime.now()
        );
        
        Message savedPetMessage = new Message(
            MessageIdentity.of(13L), chatRoomId, SenderType.PET, contextualAiResponse, false, LocalDateTime.now()
        );
        
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId())).thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(chatHistory);
        when(aiResponsePort.generatePetResponse(eq(mockPet), eq(mockPersona), eq(userMessageContent), eq(chatHistory)))
            .thenReturn(contextualAiResponse);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedUserMessage)
            .thenReturn(savedPetMessage);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        
        // When
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, MessageSendCommand.of(userMessageContent));
        
        // Then
        assertThat(result.message().content()).isEqualTo(userMessageContent);
        
        // AI 서비스에 채팅 히스토리가 컨텍스트로 전달되었는지 확인
        verify(aiResponsePort).generatePetResponse(eq(mockPet), eq(mockPersona), eq(userMessageContent), eq(chatHistory));
        
        // WebSocket 브로드캐스트에서 컨텍스트 기반 응답 확인
        ArgumentCaptor<ChatMessage> chatMessageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(webSocketChatService).broadcastMessage(chatMessageCaptor.capture());
        
        ChatMessage broadcastedMessage = chatMessageCaptor.getValue();
        assertThat(broadcastedMessage.content()).isEqualTo(contextualAiResponse);
        assertThat(broadcastedMessage.messageId()).isEqualTo(MessageIdentity.of(13L));
    }
}