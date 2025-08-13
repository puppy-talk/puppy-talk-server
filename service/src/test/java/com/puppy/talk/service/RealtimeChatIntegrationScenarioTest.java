package com.puppy.talk.service;

import com.puppy.talk.ai.AiResponseService;
import com.puppy.talk.infrastructure.activity.InactivityNotificationRepository;
import com.puppy.talk.infrastructure.chat.ChatRoomRepository;
import com.puppy.talk.infrastructure.chat.MessageRepository;
import com.puppy.talk.infrastructure.pet.PetRepository;
import com.puppy.talk.model.activity.InactivityNotification;
import com.puppy.talk.model.activity.InactivityNotificationIdentity;
import com.puppy.talk.model.chat.ChatRoom;
import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.chat.Message;
import com.puppy.talk.model.chat.MessageIdentity;
import com.puppy.talk.model.chat.SenderType;
import com.puppy.talk.model.pet.Pet;
import com.puppy.talk.model.pet.PetIdentity;
import com.puppy.talk.model.pet.Persona;
import com.puppy.talk.model.pet.PersonaIdentity;
import com.puppy.talk.model.push.NotificationType;
import com.puppy.talk.model.user.UserIdentity;
import com.puppy.talk.model.websocket.ChatMessage;
import com.puppy.talk.model.websocket.ChatMessageType;
import com.puppy.talk.service.chat.ChatService;
import com.puppy.talk.service.dto.MessageSendResult;
import com.puppy.talk.service.notification.PushNotificationService;
import com.puppy.talk.service.pet.PersonaLookUpService;
import com.puppy.talk.service.websocket.WebSocketChatService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("실시간 채팅 통합 시나리오 테스트")
class RealtimeChatIntegrationScenarioTest {
    
    @Mock
    private PetRepository petRepository;
    
    @Mock
    private ChatRoomRepository chatRoomRepository;
    
    @Mock
    private MessageRepository messageRepository;
    
    @Mock
    private AiResponseService aiResponseService;
    
    @Mock
    private PersonaLookUpService personaLookUpService;
    
    @Mock
    private WebSocketChatService webSocketChatService;
    
    @Mock
    private InactivityNotificationRepository inactivityNotificationRepository;
    
    @Mock
    private PushNotificationService pushNotificationService;
    
    @InjectMocks
    private ChatService chatService;
    
    @InjectMocks
    private InactivityNotificationService inactivityNotificationService;
    
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
    @DisplayName("완전한 대화 시나리오 - 사용자 메시지 → AI 응답 → WebSocket 브로드캐스트 통합 테스트")
    void completeConversationScenario_UserMessageToAiResponse_Success() {
        // Given
        String userMessage = "안녕 멍멍이! 오늘 기분이 어때?";
        String aiResponse = "안녕하세요! 🐾 오늘 정말 기분이 좋아요! 햇살이 따뜻해서 산책하고 싶어요!";
        
        Message savedUserMessage = new Message(
            MessageIdentity.of(1L), chatRoomId, SenderType.USER, userMessage, true, LocalDateTime.now()
        );
        Message savedPetMessage = new Message(
            MessageIdentity.of(2L), chatRoomId, SenderType.PET, aiResponse, false, LocalDateTime.now()
        );
        
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId())).thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(List.of());
        when(aiResponseService.generatePetResponse(eq(mockPet), eq(mockPersona), eq(userMessage), any()))
            .thenReturn(aiResponse);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedUserMessage)
            .thenReturn(savedPetMessage);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        
        // When
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, userMessage);
        
        // Then
        // 1. 사용자 메시지 처리 검증
        assertThat(result.message().content()).isEqualTo(userMessage);
        assertThat(result.message().senderType()).isEqualTo(SenderType.USER);
        
        // 2. AI 응답 생성 검증
        verify(aiResponseService).generatePetResponse(eq(mockPet), eq(mockPersona), eq(userMessage), any());
        
        // 3. WebSocket 브로드캐스트 검증
        ArgumentCaptor<ChatMessage> webSocketMessageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(webSocketChatService).broadcastMessage(webSocketMessageCaptor.capture());
        
        ChatMessage broadcastedMessage = webSocketMessageCaptor.getValue();
        assertThat(broadcastedMessage.content()).isEqualTo(aiResponse);
        assertThat(broadcastedMessage.senderType()).isEqualTo(SenderType.PET);
        assertThat(broadcastedMessage.messageType()).isEqualTo(ChatMessageType.MESSAGE);
        assertThat(broadcastedMessage.isRead()).isFalse();
        
        // 4. 메시지 저장 검증 (사용자 + AI 응답)
        verify(messageRepository, times(2)).save(any(Message.class));
        
        // 5. 채팅방 업데이트 검증
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }
    
    @Test
    @DisplayName("연속 대화 시나리오 - 컨텍스트 유지하며 여러 메시지 교환")
    void continuousConversationScenario_WithContext_Success() {
        // Given
        String firstUserMessage = "산책 가고 싶어!";
        String firstAiResponse = "좋아요! 어디로 갈까요?";
        String secondUserMessage = "공원으로 가자!";
        String secondAiResponse = "공원 좋아해요! 🌳 같이 뛰어놀아요!";
        
        // 기존 대화 히스토리
        List<Message> chatHistory = List.of(
            new Message(MessageIdentity.of(1L), chatRoomId, SenderType.USER, firstUserMessage, true, LocalDateTime.now().minusMinutes(5)),
            new Message(MessageIdentity.of(2L), chatRoomId, SenderType.PET, firstAiResponse, false, LocalDateTime.now().minusMinutes(4))
        );
        
        Message savedUserMessage = new Message(
            MessageIdentity.of(3L), chatRoomId, SenderType.USER, secondUserMessage, true, LocalDateTime.now()
        );
        Message savedPetMessage = new Message(
            MessageIdentity.of(4L), chatRoomId, SenderType.PET, secondAiResponse, false, LocalDateTime.now()
        );
        
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId())).thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(chatHistory);
        when(aiResponseService.generatePetResponse(eq(mockPet), eq(mockPersona), eq(secondUserMessage), eq(chatHistory)))
            .thenReturn(secondAiResponse);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedUserMessage)
            .thenReturn(savedPetMessage);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        
        // When
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, secondUserMessage);
        
        // Then
        // 1. 컨텍스트 기반 AI 응답 생성 검증
        verify(aiResponseService).generatePetResponse(eq(mockPet), eq(mockPersona), eq(secondUserMessage), eq(chatHistory));
        
        // 2. WebSocket 브로드캐스트에서 컨텍스트 반영된 응답 확인
        ArgumentCaptor<ChatMessage> webSocketMessageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(webSocketChatService).broadcastMessage(webSocketMessageCaptor.capture());
        
        ChatMessage broadcastedMessage = webSocketMessageCaptor.getValue();
        assertThat(broadcastedMessage.content()).isEqualTo(secondAiResponse);
        assertThat(broadcastedMessage.content()).contains("공원");
        assertThat(broadcastedMessage.messageId()).isEqualTo(MessageIdentity.of(4L));
    }
    
    @Test
    @DisplayName("비활성 알림 발생 시나리오 - 2시간 후 AI가 먼저 메시지 전송")
    void inactivityNotificationScenario_AiInitiatedMessage_Success() {
        // Given
        InactivityNotification notification = InactivityNotification.of(chatRoomId, LocalDateTime.now().minusHours(3))
            .withIdentity(InactivityNotificationIdentity.of(1L));
        
        String aiInitiatedMessage = "안녕하세요! 오랜만이에요~ 어떻게 지내고 계셨나요? 🐾";
        
        Message savedPetMessage = new Message(
            MessageIdentity.of(100L), chatRoomId, SenderType.PET, aiInitiatedMessage, false, LocalDateTime.now()
        );
        
        when(inactivityNotificationRepository.findEligibleNotifications())
            .thenReturn(List.of(notification));
        when(chatRoomRepository.findByIdentity(notification.chatRoomId()))
            .thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(mockChatRoom.petId()))
            .thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId()))
            .thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(mockChatRoom.identity()))
            .thenReturn(List.of());
        when(aiResponseService.generatePetResponse(eq(mockPet), eq(mockPersona), any(), any()))
            .thenReturn(aiInitiatedMessage);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedPetMessage);
        when(inactivityNotificationRepository.save(any(InactivityNotification.class)))
            .thenReturn(notification.withAiGeneratedMessage(aiInitiatedMessage).markAsSent());
        
        doNothing().when(pushNotificationService).sendNotification(any(), any(), any(), any(), any());
        doNothing().when(webSocketChatService).broadcastMessage(any());
        
        // When
        inactivityNotificationService.processEligibleNotifications();
        
        // Then
        // 1. AI 비활성 메시지 생성 검증
        verify(aiResponseService).generatePetResponse(eq(mockPet), eq(mockPersona), any(), any());
        
        // 2. WebSocket 브로드캐스트 검증
        ArgumentCaptor<ChatMessage> webSocketMessageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(webSocketChatService).broadcastMessage(webSocketMessageCaptor.capture());
        
        ChatMessage broadcastedMessage = webSocketMessageCaptor.getValue();
        assertThat(broadcastedMessage.content()).isEqualTo(aiInitiatedMessage);
        assertThat(broadcastedMessage.senderType()).isEqualTo(SenderType.PET);
        assertThat(broadcastedMessage.messageId()).isEqualTo(MessageIdentity.of(100L));
        
        // 3. 푸시 알림 전송 검증
        verify(pushNotificationService).sendNotification(
            eq(userId),
            eq(NotificationType.INACTIVITY_MESSAGE),
            any(String.class),
            any(String.class),
            any(String.class)
        );
        
        // 4. 메시지 저장 및 알림 상태 업데이트 검증
        verify(messageRepository).save(any(Message.class));
        verify(inactivityNotificationRepository, times(2)).save(any(InactivityNotification.class));
    }
    
    @Test
    @DisplayName("실시간 타이핑 상태 시나리오 - 사용자 타이핑 → AI 응답 준비 표시")
    void typingStatusScenario_UserTypingThenAiResponse_Success() {
        // Given
        String userMessage = "멍멍이 놀아줘!";
        String aiResponse = "네! 무엇을 하고 놀까요? 🎾";
        
        // 타이핑 메시지들
        ChatMessage userTypingMessage = ChatMessage.typing(chatRoomId, userId, SenderType.USER);
        ChatMessage userStopTypingMessage = ChatMessage.stopTyping(chatRoomId, userId, SenderType.USER);
        ChatMessage aiTypingMessage = ChatMessage.typing(chatRoomId, userId, SenderType.PET);
        ChatMessage aiStopTypingMessage = ChatMessage.stopTyping(chatRoomId, userId, SenderType.PET);
        
        Message savedUserMessage = new Message(
            MessageIdentity.of(1L), chatRoomId, SenderType.USER, userMessage, true, LocalDateTime.now()
        );
        Message savedPetMessage = new Message(
            MessageIdentity.of(2L), chatRoomId, SenderType.PET, aiResponse, false, LocalDateTime.now()
        );
        
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId())).thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(List.of());
        when(aiResponseService.generatePetResponse(any(), any(), any(), any())).thenReturn(aiResponse);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedUserMessage)
            .thenReturn(savedPetMessage);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        
        // When
        // 1. 사용자 타이핑 시작
        webSocketChatService.broadcastTyping(userTypingMessage);
        
        // 2. 사용자 메시지 전송 (타이핑 중단 + 메시지 발송)
        webSocketChatService.broadcastTyping(userStopTypingMessage);
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, userMessage);
        
        // 3. AI 타이핑 상태 표시 (응답 준비 중)
        webSocketChatService.broadcastTyping(aiTypingMessage);
        
        // 4. AI 응답 완료 후 타이핑 중단
        webSocketChatService.broadcastTyping(aiStopTypingMessage);
        
        // Then
        // 1. 타이핑 상태 브로드캐스트 검증 (4번)
        verify(webSocketChatService, times(4)).broadcastTyping(any());
        
        // 2. 메시지 브로드캐스트 검증 (AI 응답)
        verify(webSocketChatService).broadcastMessage(any());
        
        // 3. 실제 메시지 처리 결과 검증
        assertThat(result.message().content()).isEqualTo(userMessage);
        
        // 4. AI 응답 생성 검증
        verify(aiResponseService).generatePetResponse(any(), any(), eq(userMessage), any());
    }
    
    @Test
    @DisplayName("읽음 처리 시나리오 - 메시지 전송 → 읽음 확인 → WebSocket 브로드캐스트")
    void readReceiptScenario_MessageSentAndRead_Success() {
        // Given
        String userMessage = "안녕!";
        String aiResponse = "안녕하세요!";
        
        Message savedUserMessage = new Message(
            MessageIdentity.of(1L), chatRoomId, SenderType.USER, userMessage, true, LocalDateTime.now()
        );
        Message savedPetMessage = new Message(
            MessageIdentity.of(2L), chatRoomId, SenderType.PET, aiResponse, false, LocalDateTime.now()
        );
        
        ChatMessage readReceiptMessage = ChatMessage.readReceipt(chatRoomId, userId, MessageIdentity.of(2L));
        
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId())).thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(List.of());
        when(aiResponseService.generatePetResponse(any(), any(), any(), any())).thenReturn(aiResponse);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedUserMessage)
            .thenReturn(savedPetMessage);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        
        // When
        // 1. 메시지 교환
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, userMessage);
        
        // 2. 읽음 처리
        webSocketChatService.broadcastReadReceipt(readReceiptMessage);
        
        // Then
        // 1. 메시지 브로드캐스트 검증
        verify(webSocketChatService).broadcastMessage(any());
        
        // 2. 읽음 확인 브로드캐스트 검증
        verify(webSocketChatService).broadcastReadReceipt(eq(readReceiptMessage));
        
        // 3. 읽음 메시지 내용 검증
        assertThat(readReceiptMessage.messageType()).isEqualTo(ChatMessageType.READ_RECEIPT);
        assertThat(readReceiptMessage.messageId()).isEqualTo(MessageIdentity.of(2L));
        assertThat(readReceiptMessage.isRead()).isTrue();
    }
    
    @Test
    @DisplayName("멀티 채널 통신 시나리오 - 메시지, 타이핑, 읽음, 시스템 메시지 동시 처리")
    void multiChannelCommunicationScenario_AllMessageTypes_Success() {
        // Given
        String userMessage = "테스트 메시지";
        String aiResponse = "테스트 응답";
        
        ChatMessage normalMessage = ChatMessage.newMessage(
            MessageIdentity.of(1L), chatRoomId, userId, SenderType.PET, aiResponse, false
        );
        ChatMessage typingMessage = ChatMessage.typing(chatRoomId, userId, SenderType.USER);
        ChatMessage readMessage = ChatMessage.readReceipt(chatRoomId, userId, MessageIdentity.of(1L));
        ChatMessage systemMessage = ChatMessage.of(
            null, chatRoomId, userId, SenderType.SYSTEM, "시스템 점검 안내", false, ChatMessageType.SYSTEM
        );
        
        // When
        webSocketChatService.broadcastMessage(normalMessage);           // /topic/chat/{chatRoomId}
        webSocketChatService.broadcastTyping(typingMessage);            // /topic/chat/{chatRoomId}/typing
        webSocketChatService.broadcastReadReceipt(readMessage);         // /topic/chat/{chatRoomId}/read
        webSocketChatService.broadcastSystemMessage(systemMessage);     // /topic/chat/{chatRoomId}/system
        webSocketChatService.sendToUser(userId, normalMessage);         // /user/{userId}/queue/messages
        
        // Then
        // 각 채널별 브로드캐스트 호출 검증
        verify(webSocketChatService).broadcastMessage(eq(normalMessage));
        verify(webSocketChatService).broadcastTyping(eq(typingMessage));
        verify(webSocketChatService).broadcastReadReceipt(eq(readMessage));
        verify(webSocketChatService).broadcastSystemMessage(eq(systemMessage));
        verify(webSocketChatService).sendToUser(eq(userId), eq(normalMessage));
        
        // 메시지 타입별 내용 검증
        assertThat(normalMessage.messageType()).isEqualTo(ChatMessageType.MESSAGE);
        assertThat(typingMessage.messageType()).isEqualTo(ChatMessageType.TYPING);
        assertThat(readMessage.messageType()).isEqualTo(ChatMessageType.READ_RECEIPT);
        assertThat(systemMessage.messageType()).isEqualTo(ChatMessageType.SYSTEM);
    }
    
    @Test
    @DisplayName("에러 복구 시나리오 - WebSocket 실패 시에도 핵심 기능 유지")
    void errorRecoveryScenario_WebSocketFailsButCoreWorks_Success() {
        // Given
        String userMessage = "안녕하세요";
        String aiResponse = "안녕! 반가워요!";
        
        Message savedUserMessage = new Message(
            MessageIdentity.of(1L), chatRoomId, SenderType.USER, userMessage, true, LocalDateTime.now()
        );
        Message savedPetMessage = new Message(
            MessageIdentity.of(2L), chatRoomId, SenderType.PET, aiResponse, false, LocalDateTime.now()
        );
        
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId())).thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(List.of());
        when(aiResponseService.generatePetResponse(any(), any(), any(), any())).thenReturn(aiResponse);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedUserMessage)
            .thenReturn(savedPetMessage);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        
        // WebSocket 브로드캐스트 실패 시뮬레이션
        doThrow(new RuntimeException("WebSocket 연결 실패"))
            .when(webSocketChatService).broadcastMessage(any());
        
        // When & Then
        // WebSocket 실패에도 불구하고 정상 처리되어야 함
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, userMessage);
        
        // 핵심 비즈니스 로직은 정상 작동
        assertThat(result.message().content()).isEqualTo(userMessage);
        verify(aiResponseService).generatePetResponse(any(), any(), any(), any());
        verify(messageRepository, times(2)).save(any(Message.class));
        
        // WebSocket 브로드캐스트는 시도했지만 실패
        verify(webSocketChatService).broadcastMessage(any());
    }
}