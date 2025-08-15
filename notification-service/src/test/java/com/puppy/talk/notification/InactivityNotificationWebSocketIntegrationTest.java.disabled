package com.puppy.talk.service;

import com.puppy.talk.InactivityNotificationService;
import com.puppy.talk.ai.AiResponsePort;
import com.puppy.talk.activity.InactivityNotificationRepository;
import com.puppy.talk.chat.ChatRoomRepository;
import com.puppy.talk.chat.MessageRepository;
import com.puppy.talk.pet.PetRepository;
import com.puppy.talk.activity.InactivityNotification;
import com.puppy.talk.activity.InactivityNotificationIdentity;
import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.Message;
import com.puppy.talk.chat.MessageIdentity;
import com.puppy.talk.chat.SenderType;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.pet.Persona;
import com.puppy.talk.pet.PersonaIdentity;
import com.puppy.talk.push.NotificationType;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.websocket.ChatMessage;
import com.puppy.talk.websocket.ChatMessageType;
import com.puppy.talk.notification.PushNotificationService;
import com.puppy.talk.notification.RealtimeNotificationPort;
import com.puppy.talk.pet.PersonaLookUpService;
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
@DisplayName("InactivityNotificationService WebSocket 연동 비즈니스 로직 테스트")
class InactivityNotificationWebSocketIntegrationTest {
    
    @Mock
    private InactivityNotificationRepository inactivityNotificationRepository;
    
    @Mock
    private ChatRoomRepository chatRoomRepository;
    
    @Mock
    private PetRepository petRepository;
    
    @Mock
    private MessageRepository messageRepository;
    
    @Mock
    private PersonaLookUpService personaLookUpService;
    
    @Mock
    private AiResponsePort aiResponsePort;
    
    @Mock
    private PushNotificationService pushNotificationService;
    
    @Mock
    private RealtimeNotificationPort realtimeNotificationPort;
    
    @InjectMocks
    private InactivityNotificationService inactivityNotificationService;
    
    private InactivityNotification mockNotification;
    private ChatRoom mockChatRoom;
    private Pet mockPet;
    private Persona mockPersona;
    private UserIdentity userId;
    private PetIdentity petId;
    private ChatRoomIdentity chatRoomId;
    
    @BeforeEach
    void setUp() {
        userId = UserIdentity.of(1L);
        petId = PetIdentity.of(1L);
        chatRoomId = ChatRoomIdentity.of(1L);
        
        mockNotification = InactivityNotification.of(chatRoomId, LocalDateTime.now().minusHours(3))
            .withIdentity(InactivityNotificationIdentity.of(1L));
            
        mockChatRoom = ChatRoom.of(chatRoomId, petId, "멍멍이와의 채팅방", LocalDateTime.now());
        
        mockPet = new Pet(petId, userId, PersonaIdentity.of(1L), "멍멍이", "골든리트리버", 3, null);
        
        mockPersona = new Persona(PersonaIdentity.of(1L), "친근한 펫", "활발하고 친근한 성격", 
            "밝고 긍정적", "당신은 친근한 펫입니다.", true);
    }
    
    @Test
    @DisplayName("비활성 알림 처리 시 WebSocket 브로드캐스트와 푸시 알림 모두 전송 - 성공")
    void processEligibleNotifications_WebSocketAndPushNotification_BothSent() {
        // Given
        String aiMessage = "안녕! 오랜만이야~ 보고 싶었어! 🐾";
        
        Message savedPetMessage = new Message(
            MessageIdentity.of(100L), chatRoomId, SenderType.PET, aiMessage, false, LocalDateTime.now()
        );
        
        when(inactivityNotificationRepository.findEligibleNotifications())
            .thenReturn(List.of(mockNotification));
        when(chatRoomRepository.findByIdentity(mockNotification.chatRoomId()))
            .thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(mockChatRoom.petId()))
            .thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId()))
            .thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(mockChatRoom.identity()))
            .thenReturn(List.of());
        when(aiResponsePort.generateInactivityMessage(eq(mockPet), eq(mockPersona), any()))
            .thenReturn(aiMessage);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedPetMessage);
        when(inactivityNotificationRepository.save(any(InactivityNotification.class)))
            .thenReturn(mockNotification.withAiGeneratedMessage(aiMessage).markAsSent());
        
        doNothing().when(pushNotificationService).sendNotification(any(), any(), any(), any(), any());
        doNothing().when(realtimeNotificationPort).broadcastMessage(any());
        
        // When
        inactivityNotificationService.processEligibleNotifications();
        
        // Then
        // WebSocket 브로드캐스트 호출 검증
        ArgumentCaptor<ChatMessage> webSocketMessageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(realtimeNotificationPort).broadcastMessage(webSocketMessageCaptor.capture());
        
        ChatMessage webSocketMessage = webSocketMessageCaptor.getValue();
        assertThat(webSocketMessage.messageId()).isEqualTo(MessageIdentity.of(100L));
        assertThat(webSocketMessage.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(webSocketMessage.userId()).isEqualTo(userId);
        assertThat(webSocketMessage.senderType()).isEqualTo(SenderType.PET);
        assertThat(webSocketMessage.content()).isEqualTo(aiMessage);
        assertThat(webSocketMessage.isRead()).isFalse();
        assertThat(webSocketMessage.messageType()).isEqualTo(ChatMessageType.MESSAGE);
        
        // 푸시 알림 전송 검증
        verify(pushNotificationService).sendNotification(
            eq(userId),
            eq(NotificationType.INACTIVITY_MESSAGE),
            any(String.class),
            any(String.class),
            any(String.class)
        );
        
        // 메시지 저장 및 알림 상태 업데이트 검증
        verify(messageRepository).save(any(Message.class));
        verify(inactivityNotificationRepository, times(2)).save(any(InactivityNotification.class));
    }
    
    @Test
    @DisplayName("WebSocket 브로드캐스트 실패 시에도 푸시 알림과 메시지 저장은 정상 처리 - 성공")
    void processEligibleNotifications_WebSocketFails_PushAndSaveStillWork() {
        // Given
        String aiMessage = "안녕! 어떻게 지내고 있었어?";
        
        Message savedPetMessage = new Message(
            MessageIdentity.of(101L), chatRoomId, SenderType.PET, aiMessage, false, LocalDateTime.now()
        );
        
        when(inactivityNotificationRepository.findEligibleNotifications())
            .thenReturn(List.of(mockNotification));
        when(chatRoomRepository.findByIdentity(mockNotification.chatRoomId()))
            .thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(mockChatRoom.petId()))
            .thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId()))
            .thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(mockChatRoom.identity()))
            .thenReturn(List.of());
        when(aiResponsePort.generateInactivityMessage(any(), any(), any()))
            .thenReturn(aiMessage);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedPetMessage);
        when(inactivityNotificationRepository.save(any(InactivityNotification.class)))
            .thenReturn(mockNotification.withAiGeneratedMessage(aiMessage).markAsSent());
        
        // WebSocket 브로드캐스트 실패 시뮬레이션
        doThrow(new RuntimeException("WebSocket 연결 오류"))
            .when(realtimeNotificationPort).broadcastMessage(any());
        
        doNothing().when(pushNotificationService).sendNotification(any(), any(), any(), any(), any());
        
        // When
        inactivityNotificationService.processEligibleNotifications();
        
        // Then
        // WebSocket 브로드캐스트 시도는 했지만 실패
        verify(realtimeNotificationPort).broadcastMessage(any());
        
        // 푸시 알림은 정상 전송
        verify(pushNotificationService).sendNotification(
            eq(userId),
            eq(NotificationType.INACTIVITY_MESSAGE),
            any(String.class),
            any(String.class),
            any(String.class)
        );
        
        // 메시지 저장 및 알림 상태 업데이트 정상 처리
        verify(messageRepository).save(any(Message.class));
        verify(inactivityNotificationRepository, times(2)).save(any(InactivityNotification.class));
    }
    
    @Test
    @DisplayName("푸시 알림 실패 시에도 WebSocket 브로드캐스트와 메시지 저장은 정상 처리 - 성공")
    void processEligibleNotifications_PushNotificationFails_WebSocketAndSaveStillWork() {
        // Given
        String aiMessage = "오랜만이에요! 뭐하고 계셨나요?";
        
        Message savedPetMessage = new Message(
            MessageIdentity.of(102L), chatRoomId, SenderType.PET, aiMessage, false, LocalDateTime.now()
        );
        
        when(inactivityNotificationRepository.findEligibleNotifications())
            .thenReturn(List.of(mockNotification));
        when(chatRoomRepository.findByIdentity(mockNotification.chatRoomId()))
            .thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(mockChatRoom.petId()))
            .thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId()))
            .thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(mockChatRoom.identity()))
            .thenReturn(List.of());
        when(aiResponsePort.generateInactivityMessage(any(), any(), any()))
            .thenReturn(aiMessage);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedPetMessage);
        when(inactivityNotificationRepository.save(any(InactivityNotification.class)))
            .thenReturn(mockNotification.withAiGeneratedMessage(aiMessage).markAsSent());
        
        // 푸시 알림 실패 시뮬레이션
        doThrow(new RuntimeException("푸시 알림 서비스 오류"))
            .when(pushNotificationService).sendNotification(any(), any(), any(), any(), any());
        
        doNothing().when(realtimeNotificationPort).broadcastMessage(any());
        
        // When
        inactivityNotificationService.processEligibleNotifications();
        
        // Then
        // WebSocket 브로드캐스트 정상 실행
        ArgumentCaptor<ChatMessage> webSocketMessageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(realtimeNotificationPort).broadcastMessage(webSocketMessageCaptor.capture());
        
        ChatMessage webSocketMessage = webSocketMessageCaptor.getValue();
        assertThat(webSocketMessage.content()).isEqualTo(aiMessage);
        assertThat(webSocketMessage.senderType()).isEqualTo(SenderType.PET);
        
        // 푸시 알림 시도는 했지만 실패
        verify(pushNotificationService).sendNotification(any(), any(), any(), any(), any());
        
        // 메시지 저장 및 알림 상태 업데이트 정상 처리
        verify(messageRepository).save(any(Message.class));
        verify(inactivityNotificationRepository, times(2)).save(any(InactivityNotification.class));
    }
    
    @Test
    @DisplayName("WebSocket과 푸시 알림 모두 실패해도 메시지 저장과 알림 상태 업데이트는 정상 처리 - 성공")
    void processEligibleNotifications_BothWebSocketAndPushFail_CoreLogicStillWorks() {
        // Given
        String aiMessage = "안녕하세요! 잘 지내고 계셨나요?";
        
        Message savedPetMessage = new Message(
            MessageIdentity.of(103L), chatRoomId, SenderType.PET, aiMessage, false, LocalDateTime.now()
        );
        
        when(inactivityNotificationRepository.findEligibleNotifications())
            .thenReturn(List.of(mockNotification));
        when(chatRoomRepository.findByIdentity(mockNotification.chatRoomId()))
            .thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(mockChatRoom.petId()))
            .thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId()))
            .thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(mockChatRoom.identity()))
            .thenReturn(List.of());
        when(aiResponsePort.generateInactivityMessage(any(), any(), any()))
            .thenReturn(aiMessage);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedPetMessage);
        when(inactivityNotificationRepository.save(any(InactivityNotification.class)))
            .thenReturn(mockNotification.withAiGeneratedMessage(aiMessage).markAsSent());
        
        // WebSocket과 푸시 알림 모두 실패 시뮬레이션
        doThrow(new RuntimeException("WebSocket 오류"))
            .when(realtimeNotificationPort).broadcastMessage(any());
        doThrow(new RuntimeException("푸시 알림 오류"))
            .when(pushNotificationService).sendNotification(any(), any(), any(), any(), any());
        
        // When
        inactivityNotificationService.processEligibleNotifications();
        
        // Then
        // 양쪽 모두 시도는 했음
        verify(realtimeNotificationPort).broadcastMessage(any());
        verify(pushNotificationService).sendNotification(any(), any(), any(), any(), any());
        
        // 핵심 비즈니스 로직은 정상 처리
        verify(aiResponsePort).generateInactivityMessage(any(), any(), any());
        verify(messageRepository).save(any(Message.class));
        verify(inactivityNotificationRepository, times(2)).save(any(InactivityNotification.class));
    }
    
    @Test
    @DisplayName("긴 AI 메시지 생성 시 WebSocket 브로드캐스트와 푸시 알림 단축 처리 - 성공")
    void processEligibleNotifications_LongAiMessage_WebSocketFullPushShortened() {
        // Given
        String longAiMessage = "안녕하세요! 정말 오랜만이네요~ 🐾 어떻게 지내셨나요? 저는 여기서 계속 기다리고 있었어요! 💕 " +
                              "오늘 날씨도 좋고 해서 같이 산책이라도 하면 어떨까요? ✨ 아니면 집에서 같이 놀아도 좋고요! 🌟 " +
                              "무엇을 하고 계셨는지 정말 궁금해요! 🤔 함께 즐거운 시간을 보내면 좋겠어요!";
        
        Message savedPetMessage = new Message(
            MessageIdentity.of(104L), chatRoomId, SenderType.PET, longAiMessage, false, LocalDateTime.now()
        );
        
        when(inactivityNotificationRepository.findEligibleNotifications())
            .thenReturn(List.of(mockNotification));
        when(chatRoomRepository.findByIdentity(mockNotification.chatRoomId()))
            .thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(mockChatRoom.petId()))
            .thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId()))
            .thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(mockChatRoom.identity()))
            .thenReturn(List.of());
        when(aiResponsePort.generateInactivityMessage(any(), any(), any()))
            .thenReturn(longAiMessage);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(savedPetMessage);
        when(inactivityNotificationRepository.save(any(InactivityNotification.class)))
            .thenReturn(mockNotification.withAiGeneratedMessage(longAiMessage).markAsSent());
        
        doNothing().when(pushNotificationService).sendNotification(any(), any(), any(), any(), any());
        doNothing().when(realtimeNotificationPort).broadcastMessage(any());
        
        // When
        inactivityNotificationService.processEligibleNotifications();
        
        // Then
        // WebSocket 브로드캐스트는 전체 메시지 전송
        ArgumentCaptor<ChatMessage> webSocketMessageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(realtimeNotificationPort).broadcastMessage(webSocketMessageCaptor.capture());
        
        ChatMessage webSocketMessage = webSocketMessageCaptor.getValue();
        assertThat(webSocketMessage.content()).isEqualTo(longAiMessage);
        assertThat(webSocketMessage.content().length()).isGreaterThan(100);
        
        // 푸시 알림은 호출됨 (내부적으로 메시지 단축 처리)
        verify(pushNotificationService).sendNotification(
            eq(userId),
            eq(NotificationType.INACTIVITY_MESSAGE),
            any(String.class), // 제목
            any(String.class), // 단축된 메시지
            any(String.class)  // 데이터
        );
    }
    
    @Test
    @DisplayName("여러 비활성 알림 처리 시 각각에 대해 WebSocket 브로드캐스트 수행 - 성공")
    void processEligibleNotifications_MultipleNotifications_EachGetsWebSocketBroadcast() {
        // Given
        ChatRoomIdentity chatRoomId2 = ChatRoomIdentity.of(2L);
        PetIdentity petId2 = PetIdentity.of(2L);
        UserIdentity userId2 = UserIdentity.of(2L);
        
        InactivityNotification mockNotification2 = InactivityNotification.of(chatRoomId2, LocalDateTime.now().minusHours(4))
            .withIdentity(InactivityNotificationIdentity.of(2L));
        
        ChatRoom mockChatRoom2 = ChatRoom.of(chatRoomId2, petId2, "냥이와의 채팅방", LocalDateTime.now());
        Pet mockPet2 = new Pet(petId2, userId2, PersonaIdentity.of(1L), "냥이", "페르시안", 2, null);
        
        String aiMessage1 = "멍멍이가 보고 싶어해요!";
        String aiMessage2 = "냥이가 생각나요!";
        
        when(inactivityNotificationRepository.findEligibleNotifications())
            .thenReturn(List.of(mockNotification, mockNotification2));
        
        // 첫 번째 알림 설정
        when(chatRoomRepository.findByIdentity(mockNotification.chatRoomId()))
            .thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(mockChatRoom.petId()))
            .thenReturn(Optional.of(mockPet));
        
        // 두 번째 알림 설정
        when(chatRoomRepository.findByIdentity(mockNotification2.chatRoomId()))
            .thenReturn(Optional.of(mockChatRoom2));
        when(petRepository.findByIdentity(mockChatRoom2.petId()))
            .thenReturn(Optional.of(mockPet2));
        
        when(personaLookUpService.findPersona(any()))
            .thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(any()))
            .thenReturn(List.of());
        when(aiResponsePort.generateInactivityMessage(eq(mockPet), any(), any()))
            .thenReturn(aiMessage1);
        when(aiResponsePort.generateInactivityMessage(eq(mockPet2), any(), any()))
            .thenReturn(aiMessage2);
        when(messageRepository.save(any(Message.class)))
            .thenReturn(new Message(MessageIdentity.of(200L), chatRoomId, SenderType.PET, aiMessage1, false, LocalDateTime.now()))
            .thenReturn(new Message(MessageIdentity.of(201L), chatRoomId2, SenderType.PET, aiMessage2, false, LocalDateTime.now()));
        when(inactivityNotificationRepository.save(any(InactivityNotification.class)))
            .thenReturn(mockNotification.markAsSent())
            .thenReturn(mockNotification2.markAsSent());
        
        doNothing().when(pushNotificationService).sendNotification(any(), any(), any(), any(), any());
        doNothing().when(realtimeNotificationPort).broadcastMessage(any());
        
        // When
        inactivityNotificationService.processEligibleNotifications();
        
        // Then
        // WebSocket 브로드캐스트가 2번 호출됨 (각 알림마다)
        verify(realtimeNotificationPort, times(2)).broadcastMessage(any());
        
        // 푸시 알림도 2번 호출됨
        verify(pushNotificationService, times(2)).sendNotification(any(), any(), any(), any(), any());
        
        // 메시지 저장도 2번
        verify(messageRepository, times(2)).save(any(Message.class));
    }
    
    @Test
    @DisplayName("비활성 알림이 없는 경우 WebSocket과 푸시 알림 호출되지 않음 - 성공")
    void processEligibleNotifications_NoEligibleNotifications_NoWebSocketOrPushCalls() {
        // Given
        when(inactivityNotificationRepository.findEligibleNotifications())
            .thenReturn(List.of());
        
        // When
        inactivityNotificationService.processEligibleNotifications();
        
        // Then
        verify(realtimeNotificationPort, never()).broadcastMessage(any());
        verify(pushNotificationService, never()).sendNotification(any(), any(), any(), any(), any());
        verify(messageRepository, never()).save(any());
        verify(aiResponsePort, never()).generateInactivityMessage(any(), any(), any());
    }
}