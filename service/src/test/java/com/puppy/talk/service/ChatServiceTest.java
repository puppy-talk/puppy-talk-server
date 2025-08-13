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
import com.puppy.talk.chat.ChatService;
import com.puppy.talk.dto.ChatStartResult;
import com.puppy.talk.dto.MessageSendResult;
import com.puppy.talk.notification.RealtimeNotificationPort;
import com.puppy.talk.pet.PersonaLookUpService;
import com.puppy.talk.pet.PetNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService 단위 테스트")
class ChatServiceTest {
    
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
    
    @InjectMocks
    private ChatService chatService;
    
    private PetIdentity petId;
    private Pet mockPet;
    private ChatRoom mockChatRoom;
    private ChatRoomIdentity chatRoomId;
    
    @BeforeEach
    void setUp() {
        petId = PetIdentity.of(1L);
        chatRoomId = ChatRoomIdentity.of(1L);
        
        mockPet = new Pet(
            petId,
            UserIdentity.of(1L),
            PersonaIdentity.of(1L),
            "멍멍이",
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        );
        
        mockChatRoom = new ChatRoom(
            chatRoomId,
            petId,
            "멍멍이와의 채팅방",
            LocalDateTime.now()
        );
    }
    
    @Test
    @DisplayName("성공: 기존 채팅방이 있는 펫과 대화 시작")
    void startChatWithPet_ExistingChatRoom() {
        // Given
        List<Message> mockMessages = List.of(
            new Message(MessageIdentity.of(1L), chatRoomId, SenderType.USER, "안녕!", true, LocalDateTime.now()),
            new Message(MessageIdentity.of(2L), chatRoomId, SenderType.PET, "멍멍!", false, LocalDateTime.now())
        );
        
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        when(chatRoomRepository.findByPetId(petId)).thenReturn(Optional.of(mockChatRoom));
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(mockMessages);
        
        // When
        ChatStartResult result = chatService.startChatWithPet(petId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.chatRoom()).isEqualTo(mockChatRoom);
        assertThat(result.pet()).isEqualTo(mockPet);
        assertThat(result.recentMessages()).hasSize(2);
        
        verify(petRepository).findByIdentity(petId);
        verify(chatRoomRepository).findByPetId(petId);
        verify(messageRepository).findByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
        verify(chatRoomRepository, never()).save(any()); // 새 채팅방 생성하지 않음
    }
    
    @Test
    @DisplayName("성공: 새로운 채팅방 생성하여 펫과 대화 시작")
    void startChatWithPet_NewChatRoom() {
        // Given
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        when(chatRoomRepository.findByPetId(petId)).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(List.of());
        
        // When
        ChatStartResult result = chatService.startChatWithPet(petId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.chatRoom()).isEqualTo(mockChatRoom);
        assertThat(result.pet()).isEqualTo(mockPet);
        assertThat(result.recentMessages()).isEmpty();
        
        verify(petRepository).findByIdentity(petId);
        verify(chatRoomRepository).findByPetId(petId);
        verify(chatRoomRepository).save(any(ChatRoom.class)); // 새 채팅방 생성
        verify(messageRepository).findByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
    }
    
    @Test
    @DisplayName("실패: 존재하지 않는 펫과 대화 시작")
    void startChatWithPet_PetNotFound() {
        // Given
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> chatService.startChatWithPet(petId))
            .isInstanceOf(PetNotFoundException.class);
        
        verify(petRepository).findByIdentity(petId);
        verify(chatRoomRepository, never()).findByPetId(any());
    }
    
    @Test
    @DisplayName("실패: null petId로 대화 시작")
    void startChatWithPet_NullPetId() {
        // When & Then
        assertThatThrownBy(() -> chatService.startChatWithPet(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("PetId cannot be null");
        
        verify(petRepository, never()).findByIdentity(any());
    }
    
    @Test
    @DisplayName("성공: 펫에게 메시지 보내기")
    void sendMessageToPet_Success() {
        // Given
        String messageContent = "안녕 멍멍이!";
        Message savedMessage = new Message(
            MessageIdentity.of(1L),
            chatRoomId,
            SenderType.USER,
            messageContent,
            true,
            LocalDateTime.now()
        );
        
        ChatRoom updatedChatRoom = new ChatRoom(
            mockChatRoom.identity(),
            mockChatRoom.petId(),
            mockChatRoom.roomName(),
            LocalDateTime.now()
        );
        
        // Persona 생성
        Persona mockPersona = new Persona(
            PersonaIdentity.of(1L),
            "친근한 펫",
            "활발하고 친근한 성격",
            "밝고 긍정적",
            "당신은 친근한 펫입니다.",
            true
        );
        
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(mockPet.identity())).thenReturn(Optional.of(mockPet));
        when(personaLookUpService.findPersona(mockPet.personaId())).thenReturn(mockPersona);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(List.of());
        when(aiResponsePort.generatePetResponse(eq(mockPet), eq(mockPersona), eq(messageContent), any())).thenReturn("AI 응답");
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(updatedChatRoom);
        
        // When
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, messageContent);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.message().content()).isEqualTo(messageContent);
        assertThat(result.message().senderType()).isEqualTo(SenderType.USER);
        assertThat(result.message().isRead()).isTrue();
        assertThat(result.chatRoom().identity()).isEqualTo(mockChatRoom.identity());
        assertThat(result.chatRoom().roomName()).isEqualTo(mockChatRoom.roomName());
        
        verify(chatRoomRepository).findByIdentity(chatRoomId);
        verify(petRepository).findByIdentity(mockPet.identity());
        verify(personaLookUpService).findPersona(mockPet.personaId());
        verify(messageRepository, times(2)).save(any(Message.class)); // 사용자 메시지 + AI 응답 메시지
        verify(aiResponsePort).generatePetResponse(eq(mockPet), any(Persona.class), eq(messageContent), any());
        verify(chatRoomRepository).save(argThat(room ->
            room.lastMessageAt() != null &&
            !room.lastMessageAt().isBefore(savedMessage.createdAt())
        ));
    }
    
    @Test
    @DisplayName("실패: 존재하지 않는 채팅방에 메시지 보내기")
    void sendMessageToPet_ChatRoomNotFound() {
        // Given
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> chatService.sendMessageToPet(chatRoomId, "메시지"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ChatRoom not found");
        
        verify(chatRoomRepository).findByIdentity(chatRoomId);
        verify(messageRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("실패: 빈 메시지 내용으로 보내기")
    void sendMessageToPet_EmptyContent() {
        // When & Then
        assertThatThrownBy(() -> chatService.sendMessageToPet(chatRoomId, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Message content cannot be null or empty");
        
        verify(chatRoomRepository, never()).findByIdentity(any());
    }
    
    @Test
    @DisplayName("성공: 채팅 히스토리 조회")
    void getChatHistory_Success() {
        // Given
        List<Message> mockMessages = List.of(
            new Message(MessageIdentity.of(1L), chatRoomId, SenderType.USER, "안녕!", true, LocalDateTime.now()),
            new Message(MessageIdentity.of(2L), chatRoomId, SenderType.PET, "멍멍!", false, LocalDateTime.now())
        );
        
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(mockMessages);
        
        // When
        List<Message> result = chatService.getChatHistory(chatRoomId);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(mockMessages);
        
        verify(messageRepository).findByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
    }
    
    @Test
    @DisplayName("성공: 메시지 읽음 처리")
    void markMessagesAsRead_Success() {
        // Given
        when(chatRoomRepository.findByIdentity(chatRoomId)).thenReturn(Optional.of(mockChatRoom));
        when(petRepository.findByIdentity(mockChatRoom.petId())).thenReturn(Optional.of(mockPet));
        
        // When
        chatService.markMessagesAsRead(chatRoomId);
        
        // Then
        verify(chatRoomRepository).findByIdentity(chatRoomId);
        verify(petRepository).findByIdentity(mockChatRoom.petId());
        verify(messageRepository).markAllAsReadByChatRoomId(chatRoomId);
        // activityTrackingService 호출은 현재 주석 처리됨
        // verify(activityTrackingService).trackMessageRead(mockPet.userId(), chatRoomId);
    }
    
    @Test
    @DisplayName("검증: 새로운 채팅방 생성 시 올바른 이름 설정")
    void createNewChatRoom_CorrectRoomName() {
        // Given
        when(petRepository.findByIdentity(petId)).thenReturn(Optional.of(mockPet));
        when(chatRoomRepository.findByPetId(petId)).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(mockChatRoom);
        when(messageRepository.findByChatRoomIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
        
        // When
        chatService.startChatWithPet(petId);
        
        // Then
        verify(chatRoomRepository).save(argThat(chatRoom -> 
            chatRoom.roomName().equals("멍멍이와의 채팅방") &&
            chatRoom.petId().equals(petId)
        ));
    }
}
