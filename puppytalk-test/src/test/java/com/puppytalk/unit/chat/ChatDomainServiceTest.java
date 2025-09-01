package com.puppytalk.unit.chat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppytalk.chat.ChatDomainService;
import com.puppytalk.chat.ChatRoom;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.chat.ChatRoomRepository;
import com.puppytalk.chat.Message;
import com.puppytalk.chat.MessageId;
import com.puppytalk.chat.MessageRepository;
import com.puppytalk.chat.MessageType;
import com.puppytalk.chat.exception.ChatRoomAccessDeniedException;
import com.puppytalk.chat.exception.ChatRoomNotFoundException;
import com.puppytalk.chat.exception.MessageNotFoundException;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    
    @DisplayName("채팅방 생성 - 기존 채팅방이 있으면 반환")
    @Test
    void createChatRoom_ExistingRoom_ReturnsExisting() {
        // given
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        ChatRoom existingRoom = ChatRoom.of(
            ChatRoomId.from(1L),
            userId,
            petId,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        mockChatRoomRepository.setFindByUserIdAndPetIdResult(Optional.of(existingRoom));
        
        // when
        ChatRoom result = chatDomainService.createChatRoom(userId, petId);
        
        // then
        assertEquals(existingRoom, result);
        assertTrue(mockChatRoomRepository.isFindByUserIdAndPetIdCalled());
        assertFalse(mockChatRoomRepository.isCreateCalled());
        assertEquals(userId, mockChatRoomRepository.getLastFindByUserIdAndPetIdUserId());
        assertEquals(petId, mockChatRoomRepository.getLastFindByUserIdAndPetIdPetId());
    }
    
    @DisplayName("채팅방 생성 - 기존 채팅방이 없으면 새로 생성")
    @Test
    void createChatRoom_NoExistingRoom_CreatesNew() {
        // given
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        ChatRoom newRoom = ChatRoom.of(
            ChatRoomId.from(1L),
            userId,
            petId,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        mockChatRoomRepository.setFindByUserIdAndPetIdResult(Optional.empty());
        mockChatRoomRepository.setCreateResult(newRoom);
        
        // when
        ChatRoom result = chatDomainService.createChatRoom(userId, petId);
        
        // then
        assertEquals(newRoom, result);
        assertTrue(mockChatRoomRepository.isFindByUserIdAndPetIdCalled());
        assertTrue(mockChatRoomRepository.isCreateCalled());
    }
    
    @DisplayName("채팅방 생성 - null UserId로 실패")
    @Test
    void createChatRoom_NullUserId_ThrowsException() {
        // given
        UserId userId = null;
        PetId petId = PetId.from(1L);
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> chatDomainService.createChatRoom(userId, petId)
        );
        
        assertEquals("UserId must be a valid stored ID", exception.getMessage());
        assertFalse(mockChatRoomRepository.isFindByUserIdAndPetIdCalled());
    }
    
    @DisplayName("채팅방 조회 - 성공")
    @Test
    void findChatRoom_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        ChatRoom chatRoom = ChatRoom.of(chatRoomId, userId, petId, LocalDateTime.now(), LocalDateTime.now());
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        
        // when
        ChatRoom result = chatDomainService.findChatRoom(chatRoomId, userId);
        
        // then
        assertEquals(chatRoom, result);
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
        assertEquals(chatRoomId, mockChatRoomRepository.getLastFindByIdParam());
    }
    
    @DisplayName("채팅방 조회 - 존재하지 않는 채팅방으로 실패")
    @Test
    void findChatRoom_ChatRoomNotFound_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        
        mockChatRoomRepository.setFindByIdResult(Optional.empty());
        
        // when & then
        ChatRoomNotFoundException exception = assertThrows(
            ChatRoomNotFoundException.class,
            () -> chatDomainService.findChatRoom(chatRoomId, userId)
        );
        
        assertTrue(exception.getMessage().contains("1"));
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
    }
    
    @DisplayName("채팅방 조회 - 접근 권한 없음으로 실패")
    @Test
    void findChatRoom_AccessDenied_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        UserId otherUserId = UserId.from(2L);
        PetId petId = PetId.from(1L);
        ChatRoom chatRoom = ChatRoom.of(chatRoomId, otherUserId, petId, LocalDateTime.now(), LocalDateTime.now());
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        
        // when & then
        ChatRoomAccessDeniedException exception = assertThrows(
            ChatRoomAccessDeniedException.class,
            () -> chatDomainService.findChatRoom(chatRoomId, userId)
        );
        
        assertTrue(exception.getMessage().contains("채팅방에 접근할 권한이 없습니다"));
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
    }
    
    @DisplayName("사용자 메시지 전송 - 성공")
    @Test
    void sendUserMessage_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        String content = "안녕하세요!";
        ChatRoom chatRoom = ChatRoom.of(chatRoomId, userId, petId, LocalDateTime.now(), LocalDateTime.now());
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        
        // when
        chatDomainService.sendUserMessage(chatRoomId, userId, content);
        
        // then
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
        assertTrue(mockMessageRepository.isCreateCalled());
        assertTrue(mockChatRoomRepository.isUpdateCalled());
        
        Message savedMessage = mockMessageRepository.getLastCreatedMessage();
        assertNotNull(savedMessage);
    }
    
    @DisplayName("반려동물 메시지 전송 - 성공")
    @Test
    void sendPetMessage_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        String content = "안녕! 나는 버디야!";
        ChatRoom chatRoom = ChatRoom.of(chatRoomId, userId, petId, LocalDateTime.now(), LocalDateTime.now());
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        
        // when
        Message result = chatDomainService.sendPetMessage(chatRoomId, content);
        
        // then
        assertNotNull(result);
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
        assertTrue(mockMessageRepository.isCreateCalled());
        assertTrue(mockChatRoomRepository.isUpdateCalled());
    }
    
    @DisplayName("메시지 목록 조회 - 성공")
    @Test
    void findMessageList_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        ChatRoom chatRoom = ChatRoom.of(chatRoomId, userId, petId, LocalDateTime.now(), LocalDateTime.now());
        
        LocalDateTime now = LocalDateTime.now();
        List<Message> expectedMessages = Arrays.asList(
            Message.of(MessageId.from(1L), chatRoomId, userId, "안녕하세요!", MessageType.USER, now, now),
            Message.of(MessageId.from(2L), chatRoomId, null, "안녕!", MessageType.PET, now, now)  // PET 메시지는 senderId가 null
        );
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        mockMessageRepository.setFindByChatRoomIdOrderByCreatedAtResult(expectedMessages);
        
        // when
        List<Message> result = chatDomainService.findMessageList(chatRoomId, userId);
        
        // then
        assertEquals(expectedMessages, result);
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
        assertTrue(mockMessageRepository.isFindByChatRoomIdOrderByCreatedAtCalled());
        assertEquals(chatRoomId, mockMessageRepository.getLastFindByChatRoomIdOrderByCreatedAtParam());
    }
    
    @DisplayName("특정 메시지 조회 - 성공")
    @Test
    void findMessage_Success() {
        // given
        MessageId messageId = MessageId.from(1L);
        UserId userId = UserId.from(1L);
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        PetId petId = PetId.from(1L);
        
        LocalDateTime now = LocalDateTime.now();
        Message message = Message.of(messageId, chatRoomId, userId, "안녕하세요!", MessageType.USER, now, now);
        ChatRoom chatRoom = ChatRoom.of(chatRoomId, userId, petId, LocalDateTime.now(), LocalDateTime.now());
        
        mockMessageRepository.setFindByIdResult(Optional.of(message));
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        
        // when
        Message result = chatDomainService.findMessage(messageId, userId);
        
        // then
        assertEquals(message, result);
        assertTrue(mockMessageRepository.isFindByIdCalled());
        assertEquals(messageId, mockMessageRepository.getLastFindByIdParam());
    }
    
    @DisplayName("특정 메시지 조회 - 존재하지 않는 메시지로 실패")
    @Test
    void findMessage_MessageNotFound_ThrowsException() {
        // given
        MessageId messageId = MessageId.from(1L);
        UserId userId = UserId.from(1L);
        
        mockMessageRepository.setFindByIdResult(Optional.empty());
        
        // when & then
        MessageNotFoundException exception = assertThrows(
            MessageNotFoundException.class,
            () -> chatDomainService.findMessage(messageId, userId)
        );
        
        assertTrue(exception.getMessage().contains("1"));
        assertTrue(mockMessageRepository.isFindByIdCalled());
    }
    
    @DisplayName("채팅방 목록 조회 - 성공")
    @Test
    void findChatRoomList_Success() {
        // given
        UserId userId = UserId.from(1L);
        PetId petId1 = PetId.from(1L);
        PetId petId2 = PetId.from(2L);
        
        List<ChatRoom> expectedRooms = Arrays.asList(
            ChatRoom.of(ChatRoomId.from(1L), userId, petId1, LocalDateTime.now(), LocalDateTime.now()),
            ChatRoom.of(ChatRoomId.from(2L), userId, petId2, LocalDateTime.now(), LocalDateTime.now())
        );
        
        mockChatRoomRepository.setFindByUserIdResult(expectedRooms);
        
        // when
        List<ChatRoom> result = chatDomainService.findChatRoomList(userId);
        
        // then
        assertEquals(expectedRooms, result);
        assertTrue(mockChatRoomRepository.isFindByUserIdCalled());
        assertEquals(userId, mockChatRoomRepository.getLastFindByUserIdParam());
    }
    
    @DisplayName("채팅방 목록 조회 - null UserId로 실패")
    @Test
    void findChatRoomList_NullUserId_ThrowsException() {
        // given
        UserId userId = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> chatDomainService.findChatRoomList(userId)
        );
        
        assertTrue(exception.getMessage().contains("UserId"));
        assertFalse(mockChatRoomRepository.isFindByUserIdCalled());
    }
    
    @DisplayName("커서 기반 메시지 목록 조회 - 성공")
    @Test
    void findMessageListWithCursor_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        MessageId cursor = MessageId.from(5L);
        int size = 20;
        
        ChatRoom chatRoom = ChatRoom.of(chatRoomId, userId, petId, LocalDateTime.now(), LocalDateTime.now());
        List<Message> expectedMessages = Arrays.asList(
            Message.of(MessageId.from(6L), chatRoomId, userId, "메시지 6", MessageType.USER, LocalDateTime.now(), LocalDateTime.now()),
            Message.of(MessageId.from(7L), chatRoomId, userId, "메시지 7", MessageType.USER, LocalDateTime.now(), LocalDateTime.now())
        );
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        mockMessageRepository.setFindByChatRoomIdResult(expectedMessages);
        
        // when
        List<Message> result = chatDomainService.findMessageListWithCursor(chatRoomId, userId, cursor, size);
        
        // then
        assertEquals(expectedMessages, result);
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
        assertTrue(mockMessageRepository.isFindByChatRoomIdCalled());
    }
    
    @DisplayName("커서 기반 메시지 목록 조회 - 잘못된 크기로 실패")
    @Test
    void findMessageListWithCursor_InvalidSize_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        MessageId cursor = MessageId.from(5L);
        int size = 0;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> chatDomainService.findMessageListWithCursor(chatRoomId, userId, cursor, size)
        );
        
        assertEquals("Size must be positive", exception.getMessage());
        assertFalse(mockChatRoomRepository.isFindByIdCalled());
    }
    
    @DisplayName("최근 채팅 히스토리 조회 - 성공")
    @Test
    void findRecentChatHistory_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        int limit = 10;
        
        ChatRoom chatRoom = ChatRoom.of(chatRoomId, userId, petId, LocalDateTime.now(), LocalDateTime.now());
        List<Message> expectedMessages = Arrays.asList(
            Message.of(MessageId.from(2L), chatRoomId, userId, "최신 메시지", MessageType.USER, LocalDateTime.now(), LocalDateTime.now()),
            Message.of(MessageId.from(1L), chatRoomId, userId, "이전 메시지", MessageType.USER, LocalDateTime.now().minusMinutes(5), LocalDateTime.now().minusMinutes(5))
        );
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        mockMessageRepository.setFindRecentMessagesResult(expectedMessages);
        
        // when
        List<Message> result = chatDomainService.findRecentChatHistory(chatRoomId, limit);
        
        // then
        assertEquals(expectedMessages, result);
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
        assertTrue(mockMessageRepository.isFindRecentMessagesCalled());
    }
    
    @DisplayName("최근 채팅 히스토리 조회 - 잘못된 limit으로 실패")
    @Test
    void findRecentChatHistory_InvalidLimit_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        int limit = -1;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> chatDomainService.findRecentChatHistory(chatRoomId, limit)
        );
        
        assertEquals("Limit must be positive", exception.getMessage());
        assertFalse(mockChatRoomRepository.isFindByIdCalled());
    }
    
    @DisplayName("새로운 메시지 조회 - 성공")
    @Test
    void findNewMessages_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        
        ChatRoom chatRoom = ChatRoom.of(chatRoomId, userId, petId, LocalDateTime.now(), LocalDateTime.now());
        List<Message> expectedMessages = Arrays.asList(
            Message.of(MessageId.from(1L), chatRoomId, userId, "새 메시지", MessageType.USER, LocalDateTime.now(), LocalDateTime.now())
        );
        
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        mockMessageRepository.setFindByChatRoomIdAndCreatedAtAfterResult(expectedMessages);
        
        // when
        List<Message> result = chatDomainService.findNewMessages(chatRoomId, userId, since);
        
        // then
        assertEquals(expectedMessages, result);
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
        assertTrue(mockMessageRepository.isFindByChatRoomIdAndCreatedAtAfterCalled());
    }
    
    @DisplayName("새로운 메시지 조회 - null since로 실패")
    @Test
    void findNewMessages_NullSince_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        LocalDateTime since = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> chatDomainService.findNewMessages(chatRoomId, userId, since)
        );
        
        assertEquals("Since time must not be null", exception.getMessage());
        assertFalse(mockChatRoomRepository.isFindByIdCalled());
    }
    
    @DisplayName("채팅방 접근 권한 검증 - 성공")
    @Test
    void validateChatRoomAccess_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        
        ChatRoom chatRoom = ChatRoom.of(chatRoomId, userId, petId, LocalDateTime.now(), LocalDateTime.now());
        mockChatRoomRepository.setFindByIdResult(Optional.of(chatRoom));
        
        // when & then - 예외가 발생하지 않아야 함
        assertTrue(mockChatRoomRepository.isFindByIdCalled());
    }

    @DisplayName("생성자 - null ChatRoomRepository는 허용됨")
    @Test
    void constructor_NullChatRoomRepository_Allowed() {
        // when & then
        assertDoesNotThrow(() -> {
            ChatDomainService service = new ChatDomainService(null, mockMessageRepository);
            // 생성자에서는 null 검증을 하지 않음
        });
    }
    
    @DisplayName("생성자 - null MessageRepository는 허용됨")
    @Test
    void constructor_NullMessageRepository_Allowed() {
        // when & then
        assertDoesNotThrow(() -> {
            ChatDomainService service = new ChatDomainService(mockChatRoomRepository, null);
            // 생성자에서는 null 검증을 하지 않음
        });
    }
    
    /**
     * Mock ChatRoomRepository 구현체
     */
    private static class MockChatRoomRepository implements ChatRoomRepository {
        private boolean findByUserIdAndPetIdCalled = false;
        private boolean findByIdCalled = false;
        private boolean createCalled = false;
        private boolean updateCalled = false;
        private boolean findByUserIdCalled = false;
        private boolean existsByUserIdAndPetIdCalled = false;
        
        private UserId lastFindByUserIdAndPetIdUserId;
        private PetId lastFindByUserIdAndPetIdPetId;
        private ChatRoomId lastFindByIdParam;
        private ChatRoom lastCreatedChatRoom;
        private ChatRoom lastUpdatedChatRoom;
        private UserId lastFindByUserIdParam;
        
        private Optional<ChatRoom> findByUserIdAndPetIdResult = Optional.empty();
        private Optional<ChatRoom> findByIdResult = Optional.empty();
        private ChatRoom createResult;
        private List<ChatRoom> findByUserIdResult = Arrays.asList();
        private boolean existsByUserIdAndPetIdResult = false;
        
        @Override
        public Optional<ChatRoom> findByUserIdAndPetId(UserId userId, PetId petId) {
            findByUserIdAndPetIdCalled = true;
            lastFindByUserIdAndPetIdUserId = userId;
            lastFindByUserIdAndPetIdPetId = petId;
            return findByUserIdAndPetIdResult;
        }
        
        @Override
        public Optional<ChatRoom> findById(ChatRoomId id) {
            findByIdCalled = true;
            lastFindByIdParam = id;
            return findByIdResult;
        }
        
        @Override
        public ChatRoom create(ChatRoom chatRoom) {
            createCalled = true;
            lastCreatedChatRoom = chatRoom;
            return createResult != null ? createResult : chatRoom;
        }
        
        @Override
        public ChatRoom update(ChatRoom chatRoom) {
            updateCalled = true;
            lastUpdatedChatRoom = chatRoom;
            return chatRoom;
        }
        
        @Override
        public List<ChatRoom> findByUserId(UserId userId) {
            findByUserIdCalled = true;
            lastFindByUserIdParam = userId;
            return findByUserIdResult;
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
            existsByUserIdAndPetIdCalled = true;
            return existsByUserIdAndPetIdResult;
        }
        
        @Override
        public long countByUserId(UserId userId) {
            return 0;
        }
        
        // Test helper methods
        public void setFindByUserIdAndPetIdResult(Optional<ChatRoom> result) {
            this.findByUserIdAndPetIdResult = result;
        }
        
        public void setFindByIdResult(Optional<ChatRoom> result) {
            this.findByIdResult = result;
        }
        
        public void setCreateResult(ChatRoom result) {
            this.createResult = result;
        }
        
        public void setFindByUserIdResult(List<ChatRoom> result) {
            this.findByUserIdResult = result;
        }
        
        public void setExistsByUserIdAndPetIdResult(boolean result) {
            this.existsByUserIdAndPetIdResult = result;
        }
        
        public boolean isFindByUserIdAndPetIdCalled() { return findByUserIdAndPetIdCalled; }
        public boolean isFindByIdCalled() { return findByIdCalled; }
        public boolean isCreateCalled() { return createCalled; }
        public boolean isUpdateCalled() { return updateCalled; }
        public boolean isFindByUserIdCalled() { return findByUserIdCalled; }
        public boolean isExistsByUserIdAndPetIdCalled() { return existsByUserIdAndPetIdCalled; }
        
        public UserId getLastFindByUserIdAndPetIdUserId() { return lastFindByUserIdAndPetIdUserId; }
        public PetId getLastFindByUserIdAndPetIdPetId() { return lastFindByUserIdAndPetIdPetId; }
        public ChatRoomId getLastFindByIdParam() { return lastFindByIdParam; }
        public ChatRoom getLastCreatedChatRoom() { return lastCreatedChatRoom; }
        public ChatRoom getLastUpdatedChatRoom() { return lastUpdatedChatRoom; }
        public UserId getLastFindByUserIdParam() { return lastFindByUserIdParam; }
    }
    
    /**
     * Mock MessageRepository 구현체
     */
    private static class MockMessageRepository implements MessageRepository {
        private boolean createCalled = false;
        private boolean updateCalled = false;
        private boolean findByIdCalled = false;
        private boolean findByChatRoomIdOrderByCreatedAtCalled = false;
        private boolean findByChatRoomIdCalled = false;
        private boolean findRecentMessagesCalled = false;
        private boolean findByChatRoomIdAndCreatedAtAfterCalled = false;
        
        private Message lastCreatedMessage;
        private Message lastUpdatedMessage;
        private MessageId lastFindByIdParam;
        private ChatRoomId lastFindByChatRoomIdOrderByCreatedAtParam;
        private ChatRoomId lastFindByChatRoomIdParam;
        private MessageId lastFindByChatRoomIdCursor;
        private int lastFindByChatRoomIdSize;
        private ChatRoomId lastFindRecentMessagesParam;
        private int lastFindRecentMessagesLimit;
        private ChatRoomId lastFindByChatRoomIdAndCreatedAtAfterChatRoomId;
        private LocalDateTime lastFindByChatRoomIdAndCreatedAtAfterSince;
        
        private Message createResult;
        private Message updateResult;
        private Optional<Message> findByIdResult = Optional.empty();
        private List<Message> findByChatRoomIdOrderByCreatedAtResult = Arrays.asList();
        private List<Message> findByChatRoomIdResult = Arrays.asList();
        private List<Message> findRecentMessagesResult = Arrays.asList();
        private List<Message> findByChatRoomIdAndCreatedAtAfterResult = Arrays.asList();
        
        @Override
        public Message create(Message message) {
            createCalled = true;
            lastCreatedMessage = message;
            return createResult != null ? createResult : message;
        }
        
        @Override
        public Message update(Message message) {
            updateCalled = true;
            lastUpdatedMessage = message;
            return updateResult != null ? updateResult : message;
        }
        
        @Override
        public Optional<Message> findById(MessageId id) {
            findByIdCalled = true;
            lastFindByIdParam = id;
            return findByIdResult;
        }
        
        @Override
        public List<Message> findByChatRoomIdWithCursor(ChatRoomId chatRoomId) {
            findByChatRoomIdOrderByCreatedAtCalled = true;
            lastFindByChatRoomIdOrderByCreatedAtParam = chatRoomId;
            return findByChatRoomIdOrderByCreatedAtResult;
        }
        
        @Override
        public List<Message> findByChatRoomIdWithCursor(ChatRoomId chatRoomId, MessageId cursor, int size) {
            findByChatRoomIdCalled = true;
            lastFindByChatRoomIdParam = chatRoomId;
            lastFindByChatRoomIdCursor = cursor;
            lastFindByChatRoomIdSize = size;
            return findByChatRoomIdResult;
        }
        
        @Override
        public List<Message> findRecentMessages(ChatRoomId chatRoomId, int limit) {
            findRecentMessagesCalled = true;
            lastFindRecentMessagesParam = chatRoomId;
            lastFindRecentMessagesLimit = limit;
            return findRecentMessagesResult;
        }
        
        @Override
        public List<Message> findByChatRoomIdAndCreatedAtAfter(ChatRoomId chatRoomId, LocalDateTime since) {
            findByChatRoomIdAndCreatedAtAfterCalled = true;
            lastFindByChatRoomIdAndCreatedAtAfterChatRoomId = chatRoomId;
            lastFindByChatRoomIdAndCreatedAtAfterSince = since;
            return findByChatRoomIdAndCreatedAtAfterResult;
        }
        
        
        // Test helper methods
        public void setCreateResult(Message result) {
            this.createResult = result;
        }
        
        public void setUpdateResult(Message result) {
            this.updateResult = result;
        }
        
        public void setFindByIdResult(Optional<Message> result) {
            this.findByIdResult = result;
        }
        
        public void setFindByChatRoomIdOrderByCreatedAtResult(List<Message> result) {
            this.findByChatRoomIdOrderByCreatedAtResult = result;
        }
        
        public void setFindByChatRoomIdResult(List<Message> result) {
            this.findByChatRoomIdResult = result;
        }
        
        public void setFindRecentMessagesResult(List<Message> result) {
            this.findRecentMessagesResult = result;
        }
        
        public void setFindByChatRoomIdAndCreatedAtAfterResult(List<Message> result) {
            this.findByChatRoomIdAndCreatedAtAfterResult = result;
        }
        
        public boolean isCreateCalled() { return createCalled; }
        public boolean isUpdateCalled() { return updateCalled; }
        public boolean isFindByIdCalled() { return findByIdCalled; }
        public boolean isFindByChatRoomIdOrderByCreatedAtCalled() { return findByChatRoomIdOrderByCreatedAtCalled; }
        public boolean isFindByChatRoomIdCalled() { return findByChatRoomIdCalled; }
        public boolean isFindRecentMessagesCalled() { return findRecentMessagesCalled; }
        public boolean isFindByChatRoomIdAndCreatedAtAfterCalled() { return findByChatRoomIdAndCreatedAtAfterCalled; }
        
        public Message getLastCreatedMessage() { return lastCreatedMessage; }
        public Message getLastUpdatedMessage() { return lastUpdatedMessage; }
        public MessageId getLastFindByIdParam() { return lastFindByIdParam; }
        public ChatRoomId getLastFindByChatRoomIdOrderByCreatedAtParam() { return lastFindByChatRoomIdOrderByCreatedAtParam; }
    }
}