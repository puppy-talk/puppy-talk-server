package com.puppytalk.unit.chat;

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
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatDomainService 단위 테스트")
class ChatDomainServiceTest {
    
    private ChatDomainService chatDomainService;
    private MockChatRoomRepository mockChatRoomRepository;
    private MockMessageRepository mockMessageRepository;
    
    @BeforeEach
    void setUp() {
        mockChatRoomRepository = new MockChatRoomRepository();
        mockMessageRepository = new MockMessageRepository();
        chatDomainService = new ChatDomainService(mockChatRoomRepository, mockMessageRepository);
    }
    
    @DisplayName("채팅방 찾기 또는 생성 - 기존 채팅방 존재")
    @Test
    void findOrCreateChatRoom_ExistingRoom_ReturnsExistingRoom() {
        // given
        UserId userId = UserId.of(1L);
        PetId petId = PetId.of(1L);
        ChatRoom existingChatRoom = ChatRoom.of(
            ChatRoomId.of(1L),
            userId,
            petId,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusHours(1)
        );
        
        mockChatRoomRepository.setFindByUserIdAndPetIdResult(Optional.of(existingChatRoom));
        
        // when
        ChatRoomResult result = chatDomainService.findOrCreateChatRoom(userId, petId);
        
        // then
        assertNotNull(result);
        assertFalse(result.isNewlyCreated());
        assertEquals(existingChatRoom, result.chatRoom());
        assertTrue(mockChatRoomRepository.isFindByUserIdAndPetIdCalled());
        assertFalse(mockChatRoomRepository.isSaveCalled());
    }
    
    @DisplayName("채팅방 찾기 또는 생성 - 새 채팅방 생성")
    @Test
    void findOrCreateChatRoom_NewRoom_CreatesNewRoom() {
        // given
        UserId userId = UserId.of(1L);
        PetId petId = PetId.of(1L);
        ChatRoom newChatRoom = ChatRoom.of(
            ChatRoomId.of(1L),
            userId,
            petId,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        mockChatRoomRepository.setFindByUserIdAndPetIdResult(Optional.empty());
        mockChatRoomRepository.setSaveResult(newChatRoom);
        
        // when
        ChatRoomResult result = chatDomainService.findOrCreateChatRoom(userId, petId);
        
        // then
        assertNotNull(result);
        assertTrue(result.isNewlyCreated());
        assertEquals(newChatRoom, result.chatRoom());
        assertTrue(mockChatRoomRepository.isFindByUserIdAndPetIdCalled());
        assertTrue(mockChatRoomRepository.isSaveCalled());
        
        ChatRoom savedChatRoom = mockChatRoomRepository.getLastSavedChatRoom();
        assertNotNull(savedChatRoom);
        assertEquals(userId, savedChatRoom.userId());
        assertEquals(petId, savedChatRoom.petId());
    }
    
    @DisplayName("채팅방 찾기 또는 생성 - null 사용자 ID로 실패")
    @Test
    void findOrCreateChatRoom_NullUserId_ThrowsException() {
        // given
        UserId userId = null;
        PetId petId = PetId.of(1L);
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> chatDomainService.findOrCreateChatRoom(userId, petId)
        );
        
        assertEquals("UserId must not be null", exception.getMessage());
        assertFalse(mockChatRoomRepository.isFindByUserIdAndPetIdCalled());
    }
    
    @DisplayName("채팅방 찾기 또는 생성 - null 반려동물 ID로 실패")
    @Test
    void findOrCreateChatRoom_NullPetId_ThrowsException() {
        // given
        UserId userId = UserId.of(1L);
        PetId petId = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> chatDomainService.findOrCreateChatRoom(userId, petId)
        );
        
        assertEquals("PetId must not be null", exception.getMessage());
        assertFalse(mockChatRoomRepository.isFindByUserIdAndPetIdCalled());
    }
    
    @DisplayName("채팅방 조회 - 성공")
    @Test
    void findChatRoom_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        UserId userId = UserId.of(1L);
        ChatRoom chatRoom = ChatRoom.of(
            chatRoomId,
            userId,
            PetId.of(1L),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        
        // when
        ChatRoom result = chatDomainService.findChatRoom(chatRoomId, userId);
        
        // then
        assertEquals(chatRoom, result);
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
        assertEquals(chatRoomId, mockChatRoomRepository.getLastFindByIdParam());
    }
    
    @DisplayName("채팅방 조회 - 접근 권한 없음으로 실패")
    @Test
    void findChatRoom_AccessDenied_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        UserId userId = UserId.of(1L);
        UserId otherUserId = UserId.of(2L);
        ChatRoom chatRoom = ChatRoom.of(
            chatRoomId,
            otherUserId, // 다른 사용자 소유
            PetId.of(1L),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        
        // when & then
        ChatRoomAccessDeniedException exception = assertThrows(
            ChatRoomAccessDeniedException.class,
            () -> chatDomainService.findChatRoom(chatRoomId, userId)
        );
        
        assertEquals("채팅방에 접근할 권한이 없습니다", exception.getMessage());
    }
    
    @DisplayName("사용자 메시지 전송 - 성공")
    @Test
    void sendUserMessage_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        UserId userId = UserId.of(1L);
        String content = "안녕하세요!";
        ChatRoom chatRoom = ChatRoom.of(
            chatRoomId,
            userId,
            PetId.of(1L),
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().minusMinutes(30)
        );
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        mockMessageRepository.setSaveResult(MessageId.of(1L));
        mockChatRoomRepository.setSaveResult(chatRoom);
        
        // when
        chatDomainService.sendUserMessage(chatRoomId, userId, content);
        
        // then
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
        assertTrue(mockMessageRepository.isSaveCalled());
        assertTrue(mockChatRoomRepository.isSaveCalled());
        
        Message savedMessage = mockMessageRepository.getLastSavedMessage();
        assertNotNull(savedMessage);
        assertEquals(chatRoomId, savedMessage.chatRoomId());
        assertEquals(content.trim(), savedMessage.content());
        assertEquals(MessageType.USER, savedMessage.type());
        
        ChatRoom savedChatRoom = mockChatRoomRepository.getLastSavedChatRoom();
        assertNotNull(savedChatRoom);
        assertEquals(chatRoom.id(), savedChatRoom.id());
    }
    
    @DisplayName("사용자 메시지 전송 - null 내용으로 실패")
    @Test
    void sendUserMessage_NullContent_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        UserId userId = UserId.of(1L);
        String content = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> chatDomainService.sendUserMessage(chatRoomId, userId, content)
        );
        
        assertEquals("Message content must not be null or empty", exception.getMessage());
        assertFalse(mockMessageRepository.isSaveCalled());
    }
    
    @DisplayName("사용자 메시지 전송 - 빈 내용으로 실패")
    @Test
    void sendUserMessage_EmptyContent_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        UserId userId = UserId.of(1L);
        String content = "   ";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> chatDomainService.sendUserMessage(chatRoomId, userId, content)
        );
        
        assertEquals("Message content must not be null or empty", exception.getMessage());
        assertFalse(mockMessageRepository.isSaveCalled());
    }
    
    @DisplayName("반려동물 메시지 전송 - 성공")
    @Test
    void sendPetMessage_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        String content = "멍멍! 잘 지내고 있어요!";
        ChatRoom chatRoom = ChatRoom.of(
            chatRoomId,
            UserId.of(1L),
            PetId.of(1L),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        Message expectedMessage = Message.createPetMessage(chatRoomId, content);
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        mockMessageRepository.setSaveResult(MessageId.of(1L));
        mockChatRoomRepository.setSaveResult(chatRoom);
        
        // when
        Message result = chatDomainService.sendPetMessage(chatRoomId, content);
        
        // then
        assertNotNull(result);
        assertEquals(MessageType.PET, result.type());
        assertEquals(content.trim(), result.content());
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
        assertTrue(mockMessageRepository.isSaveCalled());
        assertTrue(mockChatRoomRepository.isSaveCalled());
    }
    
    @DisplayName("반려동물 메시지 전송 - 존재하지 않는 채팅방으로 실패")
    @Test
    void sendPetMessage_ChatRoomNotFound_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        String content = "멍멍!";
        
        mockChatRoomRepository.setFindByIdResult(Optional.empty());
        
        // when & then
        ChatRoomNotFoundException exception = assertThrows(
            ChatRoomNotFoundException.class,
            () -> chatDomainService.sendPetMessage(chatRoomId, content)
        );
        
        assertTrue(exception.getMessage().contains("1"));
        assertFalse(mockMessageRepository.isSaveCalled());
    }
    
    @DisplayName("채팅방 목록 조회 - 성공")
    @Test
    void findChatRoomList_Success() {
        // given
        UserId userId = UserId.of(1L);
        List<ChatRoom> expectedChatRooms = Arrays.asList(
            ChatRoom.of(ChatRoomId.of(1L), userId, PetId.of(1L), LocalDateTime.now(), LocalDateTime.now()),
            ChatRoom.of(ChatRoomId.of(2L), userId, PetId.of(2L), LocalDateTime.now(), LocalDateTime.now())
        );
        
        mockChatRoomRepository.setFindByUserIdResult(expectedChatRooms);
        
        // when
        List<ChatRoom> result = chatDomainService.findChatRoomList(userId);
        
        // then
        assertEquals(expectedChatRooms, result);
        assertTrue(mockChatRoomRepository.isFindByUserIdCalled());
        assertEquals(userId, mockChatRoomRepository.getLastFindByUserIdParam());
    }
    
    @DisplayName("채팅방 목록 조회 - null 사용자 ID로 실패")
    @Test
    void findChatRoomList_NullUserId_ThrowsException() {
        // given
        UserId userId = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> chatDomainService.findChatRoomList(userId)
        );
        
        assertEquals("UserId must not be null", exception.getMessage());
        assertFalse(mockChatRoomRepository.isFindByUserIdCalled());
    }
    
    @DisplayName("특정 시간 이후 새 메시지 조회 - 성공")
    @Test
    void findNewMessages_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        UserId userId = UserId.of(1L);
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        ChatRoom chatRoom = ChatRoom.of(
            chatRoomId,
            userId,
            PetId.of(1L),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        List<Message> expectedMessages = Arrays.asList(
            Message.restore(MessageId.of(1L), chatRoomId, MessageType.PET, "새 메시지1", LocalDateTime.now()),
            Message.restore(MessageId.of(2L), chatRoomId, MessageType.PET, "새 메시지2", LocalDateTime.now())
        );
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        mockMessageRepository.setFindByChatRoomIdAndCreatedAtAfterResult(expectedMessages);
        
        // when
        List<Message> result = chatDomainService.findNewMessages(chatRoomId, userId, since);
        
        // then
        assertEquals(expectedMessages, result);
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
        assertTrue(mockMessageRepository.isFindByChatRoomIdAndCreatedAtAfterCalled());
        assertEquals(chatRoomId, mockMessageRepository.getLastFindByChatRoomIdAndCreatedAtAfterChatRoomId());
        assertEquals(since, mockMessageRepository.getLastFindByChatRoomIdAndCreatedAtAfterSince());
    }
    
    @DisplayName("특정 시간 이후 새 메시지 조회 - null 시간으로 실패")
    @Test
    void findNewMessages_NullSince_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        UserId userId = UserId.of(1L);
        LocalDateTime since = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> chatDomainService.findNewMessages(chatRoomId, userId, since)
        );
        
        assertEquals("Since time must not be null", exception.getMessage());
        assertFalse(mockMessageRepository.isFindByChatRoomIdAndCreatedAtAfterCalled());
    }
    
    @DisplayName("채팅방 검증 - 성공")
    @Test
    void validateChatRoom_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        UserId userId = UserId.of(1L);
        ChatRoom chatRoom = ChatRoom.of(
            chatRoomId,
            userId,
            PetId.of(1L),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        
        // when & then
        assertDoesNotThrow(() -> chatDomainService.validateChatRoom(chatRoomId, userId));
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
    }
    
    /**
     * Mock ChatRoomRepository 구현체
     */
    private static class MockChatRoomRepository implements ChatRoomRepository {
        private boolean saveCalled = false;
        private boolean findByIdCalled = false;
        private boolean findByUserIdCalled = false;
        private boolean findByUserIdAndPetIdCalled = false;
        
        private ChatRoom lastSavedChatRoom;
        private ChatRoomId lastFindByIdParam;
        private UserId lastFindByUserIdParam;
        private UserId lastFindByUserIdAndPetIdUserId;
        private PetId lastFindByUserIdAndPetIdPetId;
        
        private ChatRoom saveResult;
        private Optional<ChatRoom> findByIdResult = Optional.empty();
        private List<ChatRoom> findByUserIdResult = Arrays.asList();
        private Optional<ChatRoom> findByUserIdAndPetIdResult = Optional.empty();
        
        @Override
        public ChatRoom save(ChatRoom chatRoom) {
            saveCalled = true;
            lastSavedChatRoom = chatRoom;
            return saveResult != null ? saveResult : chatRoom;
        }
        
        @Override
        public Optional<ChatRoom> findById(ChatRoomId id) {
            findByIdCalled = true;
            lastFindByIdParam = id;
            return findByIdResult;
        }
        
        @Override
        public List<ChatRoom> findByUserId(UserId userId) {
            findByUserIdCalled = true;
            lastFindByUserIdParam = userId;
            return findByUserIdResult;
        }
        
        @Override
        public Optional<ChatRoom> findByUserIdAndPetId(UserId userId, PetId petId) {
            findByUserIdAndPetIdCalled = true;
            lastFindByUserIdAndPetIdUserId = userId;
            lastFindByUserIdAndPetIdPetId = petId;
            return findByUserIdAndPetIdResult;
        }
        
        @Override
        public Optional<ChatRoom> findByPetId(PetId petId) {
            return Optional.empty();
        }
        
        @Override
        public boolean existsById(ChatRoomId id) {
            return false;
        }
        
        @Override
        public boolean existsByUserIdAndPetId(UserId userId, PetId petId) {
            return false;
        }
        
        @Override
        public long countByUserId(UserId userId) {
            return 0;
        }
        
        // Test helper methods
        public void setSaveResult(ChatRoom result) { this.saveResult = result; }
        public void setFindByIdResult(Optional<ChatRoom> result) { this.findByIdResult = result; }
        public void setFindByUserIdResult(List<ChatRoom> result) { this.findByUserIdResult = result; }
        public void setFindByUserIdAndPetIdResult(Optional<ChatRoom> result) { this.findByUserIdAndPetIdResult = result; }
        
        public boolean isSaveCalled() { return saveCalled; }
        public boolean isFindByIdCalled() { return findByIdCalled; }
        public boolean isFindByUserIdCalled() { return findByUserIdCalled; }
        public boolean isFindByUserIdAndPetIdCalled() { return findByUserIdAndPetIdCalled; }
        
        public ChatRoom getLastSavedChatRoom() { return lastSavedChatRoom; }
        public ChatRoomId getLastFindByIdParam() { return lastFindByIdParam; }
        public UserId getLastFindByUserIdParam() { return lastFindByUserIdParam; }
        public UserId getLastFindByUserIdAndPetIdUserId() { return lastFindByUserIdAndPetIdUserId; }
        public PetId getLastFindByUserIdAndPetIdPetId() { return lastFindByUserIdAndPetIdPetId; }
    }
    
    /**
     * Mock MessageRepository 구현체
     */
    private static class MockMessageRepository implements MessageRepository {
        private boolean saveCalled = false;
        private boolean findByChatRoomIdAndCreatedAtAfterCalled = false;
        
        private Message lastSavedMessage;
        private ChatRoomId lastFindByChatRoomIdAndCreatedAtAfterChatRoomId;
        private LocalDateTime lastFindByChatRoomIdAndCreatedAtAfterSince;
        
        private MessageId saveResult;
        private List<Message> findByChatRoomIdAndCreatedAtAfterResult = Arrays.asList();
        
        @Override
        public void save(Message message) {
            saveCalled = true;
            lastSavedMessage = message;
        }
        
        @Override
        public List<Message> findByChatRoomIdOrderByCreatedAt(ChatRoomId chatRoomId) {
            return Arrays.asList();
        }
        
        @Override
        public List<Message> findByChatRoomId(ChatRoomId chatRoomId, MessageId cursor, int size) {
            return Arrays.asList();
        }
        
        @Override
        public List<Message> findRecentMessages(ChatRoomId chatRoomId, int limit) {
            return Arrays.asList();
        }
        
        @Override
        public Optional<Message> findById(MessageId id) {
            return Optional.empty();
        }
        
        @Override
        @Deprecated
        public List<Message> findByChatRoomIdOrderByCreatedAtDesc(ChatRoomId chatRoomId, int limit) {
            return Arrays.asList();
        }
        
        @Override
        public Optional<Message> findLatestByChatRoomId(ChatRoomId chatRoomId) {
            return Optional.empty();
        }
        
        @Override
        public long countByChatRoomId(ChatRoomId chatRoomId) {
            return 0;
        }
        
        @Override
        public long countByChatRoomIdAndType(ChatRoomId chatRoomId, MessageType type) {
            return 0;
        }
        
        @Override
        public boolean existsById(MessageId id) {
            return false;
        }
        
        @Override
        public List<Message> findByChatRoomIdAndCreatedAtAfter(ChatRoomId chatRoomId, LocalDateTime since) {
            findByChatRoomIdAndCreatedAtAfterCalled = true;
            lastFindByChatRoomIdAndCreatedAtAfterChatRoomId = chatRoomId;
            lastFindByChatRoomIdAndCreatedAtAfterSince = since;
            return findByChatRoomIdAndCreatedAtAfterResult;
        }
        
        // Test helper methods
        public void setSaveResult(MessageId result) { this.saveResult = result; }
        public void setFindByChatRoomIdAndCreatedAtAfterResult(List<Message> result) { 
            this.findByChatRoomIdAndCreatedAtAfterResult = result; 
        }
        
        public boolean isSaveCalled() { return saveCalled; }
        public boolean isFindByChatRoomIdAndCreatedAtAfterCalled() { return findByChatRoomIdAndCreatedAtAfterCalled; }
        
        public Message getLastSavedMessage() { return lastSavedMessage; }
        public ChatRoomId getLastFindByChatRoomIdAndCreatedAtAfterChatRoomId() { return lastFindByChatRoomIdAndCreatedAtAfterChatRoomId; }
        public LocalDateTime getLastFindByChatRoomIdAndCreatedAtAfterSince() { return lastFindByChatRoomIdAndCreatedAtAfterSince; }
    }
}