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
import com.puppy.talk.chat.ActivityTrackingService;
import com.puppy.talk.websocket.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ChatService 단위 테스트")
class ChatServiceTest {
    
    // Mock objects 직접 생성
    private ChatRoomRepository chatRoomRepository;
    private MessageRepository messageRepository;
    private PetRepository petRepository;
    private PersonaLookUpService personaLookUpService;
    private AiResponsePort aiResponsePort;
    private RealtimeNotificationPort realtimeNotificationPort;
    private ActivityTrackingService activityTrackingService;
    
    private ChatService chatService;
    
    private PetIdentity petId;
    private Pet mockPet;
    private ChatRoom mockChatRoom;
    private ChatRoomIdentity chatRoomId;
    
    @BeforeEach
    void setUp() {
        // Mock objects 직접 생성
        chatRoomRepository = new MockChatRoomRepository();
        messageRepository = new MockMessageRepository();
        petRepository = new MockPetRepository();
        personaLookUpService = new MockPersonaLookUpService();
        aiResponsePort = new MockAiResponsePort();
        realtimeNotificationPort = new MockRealtimeNotificationPort();
        activityTrackingService = new MockActivityTrackingService();
        
        // ChatService는 @RequiredArgsConstructor를 사용하므로 필드 주입 방식으로 테스트
        chatService = new ChatService(
            petRepository,
            chatRoomRepository,
            messageRepository,
            aiResponsePort,
            realtimeNotificationPort,
            personaLookUpService,
            activityTrackingService
        );
        
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
        
        // Mock data 설정
        ((MockPetRepository) petRepository).setPet(mockPet);
        ((MockChatRoomRepository) chatRoomRepository).setChatRoom(mockChatRoom);
    }
    
    @Test
    @DisplayName("성공: 기존 채팅방이 있는 펫과 대화 시작")
    void startChatWithPet_ExistingChatRoom() {
        // Given
        List<Message> mockMessages = List.of(
            new Message(MessageIdentity.of(1L), chatRoomId, SenderType.USER, "안녕!", true, LocalDateTime.now()),
            new Message(MessageIdentity.of(2L), chatRoomId, SenderType.PET, "멍멍!", false, LocalDateTime.now())
        );
        
        ((MockMessageRepository) messageRepository).setMessages(mockMessages);
        
        // When
        ChatStartResult result = chatService.startChatWithPet(petId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.chatRoom()).isEqualTo(mockChatRoom);
        assertThat(result.pet()).isEqualTo(mockPet);
        assertThat(result.recentMessages()).hasSize(2);
    }
    
    @Test
    @DisplayName("성공: 새로운 채팅방 생성하여 펫과 대화 시작")
    void startChatWithPet_NewChatRoom() {
        // Given
        ((MockChatRoomRepository) chatRoomRepository).setChatRoom(null); // 기존 채팅방 없음
        ((MockMessageRepository) messageRepository).setMessages(List.of());
        
        // When
        ChatStartResult result = chatService.startChatWithPet(petId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.chatRoom()).isNotNull();
        assertThat(result.pet()).isEqualTo(mockPet);
        assertThat(result.recentMessages()).isEmpty();
    }
    
    @Test
    @DisplayName("실패: 존재하지 않는 펫과 대화 시작")
    void startChatWithPet_PetNotFound() {
        // Given
        ((MockPetRepository) petRepository).setPet(null);
        
        // When & Then
        assertThatThrownBy(() -> chatService.startChatWithPet(petId))
            .isInstanceOf(PetNotFoundException.class);
    }
    
    @Test
    @DisplayName("실패: null petId로 대화 시작")
    void startChatWithPet_NullPetId() {
        // When & Then
        assertThatThrownBy(() -> chatService.startChatWithPet(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("PetId cannot be null");
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
        
        // Persona 생성
        Persona mockPersona = new Persona(
            PersonaIdentity.of(1L),
            "친근한 펫",
            "활발하고 친근한 성격",
            "밝고 긍정적",
            "당신은 친근한 펫입니다.",
            true
        );
        
        ((MockPersonaLookUpService) personaLookUpService).setPersona(mockPersona);
        ((MockMessageRepository) messageRepository).setMessages(List.of());
        
        // When
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, messageContent);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.message().content()).isEqualTo(messageContent);
        assertThat(result.message().senderType()).isEqualTo(SenderType.USER);
        assertThat(result.message().isRead()).isTrue();
        assertThat(result.chatRoom().identity()).isEqualTo(mockChatRoom.identity());
        assertThat(result.chatRoom().roomName()).isEqualTo(mockChatRoom.roomName());
    }
    
    @Test
    @DisplayName("실패: 존재하지 않는 채팅방에 메시지 보내기")
    void sendMessageToPet_ChatRoomNotFound() {
        // Given
        ((MockChatRoomRepository) chatRoomRepository).setChatRoom(null);
        
        // When & Then
        assertThatThrownBy(() -> chatService.sendMessageToPet(chatRoomId, "메시지"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ChatRoom not found");
    }
    
    @Test
    @DisplayName("실패: 빈 메시지 내용으로 보내기")
    void sendMessageToPet_EmptyContent() {
        // When & Then
        assertThatThrownBy(() -> chatService.sendMessageToPet(chatRoomId, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Message content cannot be null or empty");
    }
    
    @Test
    @DisplayName("성공: 채팅 히스토리 조회")
    void getChatHistory_Success() {
        // Given
        List<Message> mockMessages = List.of(
            new Message(MessageIdentity.of(1L), chatRoomId, SenderType.USER, "안녕!", true, LocalDateTime.now()),
            new Message(MessageIdentity.of(2L), chatRoomId, SenderType.PET, "멍멍!", false, LocalDateTime.now())
        );
        
        ((MockMessageRepository) messageRepository).setMessages(mockMessages);
        
        // When
        List<Message> result = chatService.getChatHistory(chatRoomId);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(mockMessages);
    }
    
    @Test
    @DisplayName("성공: 메시지 읽음 처리")
    void markMessagesAsRead_Success() {
        // When
        chatService.markMessagesAsRead(chatRoomId);
        
        // Then - Mock 객체들이 올바르게 호출되었는지 확인
        assertThat(((MockMessageRepository) messageRepository).isMarkAllAsReadCalled()).isTrue();
    }
    
    @Test
    @DisplayName("검증: 새로운 채팅방 생성 시 올바른 이름 설정")
    void createNewChatRoom_CorrectRoomName() {
        // Given
        ((MockChatRoomRepository) chatRoomRepository).setChatRoom(null);
        ((MockMessageRepository) messageRepository).setMessages(List.of());
        
        // When
        chatService.startChatWithPet(petId);
        
        // Then
        ChatRoom savedChatRoom = ((MockChatRoomRepository) chatRoomRepository).getSavedChatRoom();
        assertThat(savedChatRoom).isNotNull();
        assertThat(savedChatRoom.roomName()).isEqualTo("멍멍이와의 채팅방");
        assertThat(savedChatRoom.petId()).isEqualTo(petId);
    }
    
    // Mock 클래스들
    private static class MockChatRoomRepository implements ChatRoomRepository {
        private ChatRoom chatRoom;
        private ChatRoom savedChatRoom;
        
        public void setChatRoom(ChatRoom chatRoom) {
            this.chatRoom = chatRoom;
        }
        
        public ChatRoom getSavedChatRoom() {
            return savedChatRoom;
        }
        
        @Override
        public Optional<ChatRoom> findByPetId(PetIdentity petId) {
            return Optional.ofNullable(chatRoom);
        }
        
        @Override
        public Optional<ChatRoom> findByIdentity(ChatRoomIdentity identity) {
            return Optional.ofNullable(chatRoom);
        }
        
        @Override
        public ChatRoom save(ChatRoom chatRoom) {
            this.savedChatRoom = chatRoom;
            return chatRoom;
        }
        
        @Override
        public void deleteByIdentity(ChatRoomIdentity identity) {
            // Mock implementation
        }
        
        @Override
        public List<ChatRoom> findAll() {
            return chatRoom != null ? List.of(chatRoom) : List.of();
        }
    }
    
    private static class MockMessageRepository implements MessageRepository {
        private List<Message> messages = List.of();
        private boolean markAllAsReadCalled = false;
        
        public void setMessages(List<Message> messages) {
            this.messages = messages;
        }
        
        public boolean isMarkAllAsReadCalled() {
            return markAllAsReadCalled;
        }
        
        @Override
        public List<Message> findByChatRoomIdOrderByCreatedAtDesc(ChatRoomIdentity chatRoomId) {
            return messages;
        }
        
        @Override
        public Message save(Message message) {
            return message;
        }
        
        @Override
        public void markAllAsReadByChatRoomId(ChatRoomIdentity chatRoomId) {
            markAllAsReadCalled = true;
        }
        
        @Override
        public Optional<Message> findByIdentity(MessageIdentity identity) {
            return messages.stream()
                .filter(msg -> msg.identity().equals(identity))
                .findFirst();
        }
        
        @Override
        public void deleteByIdentity(MessageIdentity identity) {
            // Mock implementation
        }
        
        @Override
        public List<Message> findByChatRoomId(ChatRoomIdentity chatRoomId) {
            return messages;
        }
        
        @Override
        public List<Message> findUnreadMessagesByChatRoomId(ChatRoomIdentity chatRoomId) {
            return messages.stream()
                .filter(msg -> !msg.isRead())
                .toList();
        }
        
        @Override
        public void markAsRead(MessageIdentity identity) {
            // Mock implementation
        }
    }
    
    private static class MockPetRepository implements PetRepository {
        private Pet pet;
        
        public void setPet(Pet pet) {
            this.pet = pet;
        }
        
        @Override
        public Optional<Pet> findByIdentity(PetIdentity identity) {
            return Optional.ofNullable(pet);
        }
        
        @Override
        public void deleteByIdentity(PetIdentity identity) {
            // Mock implementation
        }
        
        @Override
        public List<Pet> findByUserId(UserIdentity userId) {
            return pet != null ? List.of(pet) : List.of();
        }
        
        @Override
        public List<Pet> findAll() {
            return pet != null ? List.of(pet) : List.of();
        }
        
        @Override
        public Pet save(Pet pet) {
            return pet;
        }
    }
    
    private static class MockPersonaLookUpService implements PersonaLookUpService {
        private Persona persona;
        
        public void setPersona(Persona persona) {
            this.persona = persona;
        }
        
        @Override
        public Persona findPersona(PersonaIdentity personaId) {
            return persona;
        }
        
        @Override
        public List<Persona> findActivePersonas() {
            return persona != null ? List.of(persona) : List.of();
        }
        
        @Override
        public Persona createPersona(Persona persona) {
            return persona;
        }
        
        @Override
        public void deletePersona(PersonaIdentity personaId) {
            // Mock implementation
        }
        
        @Override
        public List<Persona> findAllPersonas() {
            return persona != null ? List.of(persona) : List.of();
        }
    }
    
    private static class MockAiResponsePort implements AiResponsePort {
        @Override
        public String generatePetResponse(Pet pet, Persona persona, String userMessage, List<Message> chatHistory) {
            return "AI 응답";
        }
        
        @Override
        public String generateInactivityMessage(Pet pet, Persona persona, List<Message> chatHistory) {
            return "AI 비활성 메시지";
        }
    }
    
    private static class MockRealtimeNotificationPort implements RealtimeNotificationPort {
        @Override
        public void broadcastMessage(ChatMessage chatMessage) {
            // Mock implementation
        }
        
        @Override
        public void broadcastTypingStatus(ChatMessage typingMessage) {
            // Mock implementation
        }
        
        @Override
        public void broadcastReadReceipt(ChatMessage readReceiptMessage) {
            // Mock implementation
        }
        
        @Override
        public void broadcastSystemMessage(ChatMessage systemMessage) {
            // Mock implementation
        }
        
        @Override
        public void sendToUser(UserIdentity user, ChatMessage message) {
            // Mock implementation
        }
    }
    
    private static class MockActivityTrackingService extends ActivityTrackingService {
        public MockActivityTrackingService() {
            super(null, null); // Mock repositories
        }
        
        @Override
        public void trackChatOpened(UserIdentity userId, ChatRoomIdentity chatRoomId) {
            // Mock implementation
        }
        
        @Override
        public void trackMessageSent(UserIdentity userId, ChatRoomIdentity chatRoomId) {
            // Mock implementation
        }
        
        @Override
        public void trackMessageRead(UserIdentity userId, ChatRoomIdentity chatRoomId) {
            // Mock implementation
        }
    }
}
