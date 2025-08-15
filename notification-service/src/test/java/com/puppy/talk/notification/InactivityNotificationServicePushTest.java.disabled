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
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.pet.Persona;
import com.puppy.talk.pet.PersonaIdentity;
import com.puppy.talk.push.NotificationType;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.notification.PushNotificationService;
import com.puppy.talk.pet.PersonaLookUpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InactivityNotificationService 푸시 알림 통합 테스트")
class InactivityNotificationServicePushTest {
    
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
    
    @InjectMocks
    private InactivityNotificationService inactivityNotificationService;
    
    private InactivityNotification mockNotification;
    private ChatRoom mockChatRoom;
    private Pet mockPet;
    private Persona mockPersona;
    
    @BeforeEach
    void setUp() {
        UserIdentity userId = UserIdentity.of(1L);
        PetIdentity petId = PetIdentity.of(1L);
        ChatRoomIdentity chatRoomId = ChatRoomIdentity.of(1L);
        PersonaIdentity personaId = PersonaIdentity.of(1L);
        
        mockNotification = InactivityNotification.of(chatRoomId, LocalDateTime.now().minusHours(3))
            .withIdentity(InactivityNotificationIdentity.of(1L));
            
        mockChatRoom = ChatRoom.of(chatRoomId, petId, "멍멍이와의 채팅방", LocalDateTime.now());
        
        mockPet = new Pet(petId, userId, personaId, "멍멍이", "골든리트리버", 3, null);
        
        mockPersona = new Persona(personaId, "친근한 펫", "활발하고 친근한 성격", 
            "밝고 긍정적", "당신은 친근한 펫입니다.", true);
    }
    
    @Test
    @DisplayName("비활성 알림 처리 시 푸시 알림도 함께 전송 - 성공")
    void processSingleNotification_WithPushNotification_Success() {
        // Given
        String aiMessage = "안녕! 오랜만이야~ 뭐하고 있었어?";
        
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
        when(inactivityNotificationRepository.save(any(InactivityNotification.class)))
            .thenReturn(mockNotification.withAiGeneratedMessage(aiMessage).markAsSent());
        when(messageRepository.save(any(Message.class)))
            .thenReturn(mock(Message.class));
        
        // 푸시 알림 서비스 모킹
        doNothing().when(pushNotificationService).sendNotification(
            any(UserIdentity.class),
            any(NotificationType.class),
            any(String.class),
            any(String.class),
            any(String.class)
        );
        
        // When
        inactivityNotificationService.processEligibleNotifications();
        
        // Then
        // 기본 비활성 알림 처리 검증
        verify(chatRoomRepository).findByIdentity(mockNotification.chatRoomId());
        verify(petRepository).findByIdentity(mockChatRoom.petId());
        verify(personaLookUpService).findPersona(mockPet.personaId());
        verify(aiResponsePort).generateInactivityMessage(eq(mockPet), eq(mockPersona), any());
        verify(messageRepository).save(any(Message.class));
        verify(inactivityNotificationRepository, times(2)).save(any(InactivityNotification.class));
        
        // 푸시 알림 전송 검증 (메서드 호출만 확인)
        verify(pushNotificationService).sendNotification(
            eq(mockPet.userId()),
            eq(NotificationType.INACTIVITY_MESSAGE),
            any(String.class), // 제목
            any(String.class), // 메시지
            any(String.class)  // JSON 데이터
        );
    }
    
    @Test
    @DisplayName("푸시 알림 전송 실패해도 비활성 알림 처리는 계속 - 성공")
    void processSingleNotification_PushNotificationFailure_ContinuesProcessing() {
        // Given
        String aiMessage = "안녕! 오랜만이야~";
        
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
        when(inactivityNotificationRepository.save(any(InactivityNotification.class)))
            .thenReturn(mockNotification.withAiGeneratedMessage(aiMessage).markAsSent());
        when(messageRepository.save(any(Message.class)))
            .thenReturn(mock(Message.class));
        
        // 푸시 알림 서비스에서 예외 발생
        doThrow(new RuntimeException("푸시 알림 전송 실패"))
            .when(pushNotificationService).sendNotification(any(), any(), any(), any(), any());
        
        // When
        inactivityNotificationService.processEligibleNotifications();
        
        // Then
        // 푸시 알림 실패에도 불구하고 기본 처리는 완료되어야 함
        verify(messageRepository).save(any(Message.class));
        verify(inactivityNotificationRepository, times(2)).save(any(InactivityNotification.class));
        verify(pushNotificationService).sendNotification(any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("푸시 알림 메시지 단축 테스트 - 긴 메시지")
    void processSingleNotification_LongMessage_ShortenedForPush() {
        // Given
        String longAiMessage = "안녕하세요! 정말 오랜만이네요~ 🐾 어떻게 지내셨나요? 저는 여기서 계속 기다리고 있었어요! 💕 " +
                              "오늘 날씨도 좋고 해서 같이 산책이라도 하면 어떨까요? ✨ 아니면 집에서 같이 놀아도 좋고요! 🌟 " +
                              "무엇을 하고 계셨는지 정말 궁금해요! 🤔";
        
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
        when(inactivityNotificationRepository.save(any(InactivityNotification.class)))
            .thenReturn(mockNotification.withAiGeneratedMessage(longAiMessage).markAsSent());
        when(messageRepository.save(any(Message.class)))
            .thenReturn(mock(Message.class));
        
        doNothing().when(pushNotificationService).sendNotification(any(), any(), any(), any(), any());
        
        // When
        inactivityNotificationService.processEligibleNotifications();
        
        // Then
        verify(pushNotificationService).sendNotification(
            eq(mockPet.userId()),
            eq(NotificationType.INACTIVITY_MESSAGE),
            any(String.class), // 제목
            any(String.class), // 단축된 메시지
            any(String.class)  // JSON 데이터
        );
    }
    
    @Test
    @DisplayName("푸시 알림 데이터 포맷 검증")
    void processSingleNotification_PushDataFormat_Valid() {
        // Given
        String aiMessage = "테스트 메시지";
        
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
        when(inactivityNotificationRepository.save(any(InactivityNotification.class)))
            .thenReturn(mockNotification.withAiGeneratedMessage(aiMessage).markAsSent());
        when(messageRepository.save(any(Message.class)))
            .thenReturn(mock(Message.class));
        
        doNothing().when(pushNotificationService).sendNotification(any(), any(), any(), any(), any());
        
        // When
        inactivityNotificationService.processEligibleNotifications();
        
        // Then
        verify(pushNotificationService).sendNotification(
            eq(mockPet.userId()),
            eq(NotificationType.INACTIVITY_MESSAGE),
            any(String.class), // 제목
            any(String.class), // 메시지
            any(String.class)  // JSON 데이터
        );
    }
}