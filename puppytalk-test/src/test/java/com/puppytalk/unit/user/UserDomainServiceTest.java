package com.puppytalk.unit.user;

import com.puppytalk.user.DuplicateUserException;
import com.puppytalk.user.PasswordEncoder;
import com.puppytalk.user.User;
import com.puppytalk.user.UserDomainService;
import com.puppytalk.user.UserId;
import com.puppytalk.user.UserNotFoundException;
import com.puppytalk.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserDomainService 단위 테스트")
class UserDomainServiceTest {
    
    private UserDomainService userDomainService;
    private MockUserRepository mockRepository;
    private MockPasswordEncoder mockPasswordEncoder;
    
    @BeforeEach
    void setUp() {
        mockRepository = new MockUserRepository();
        mockPasswordEncoder = new MockPasswordEncoder();
        userDomainService = new UserDomainService(mockRepository, mockPasswordEncoder);
    }
    
    @DisplayName("사용자 등록 - 성공")
    @Test
    void registerUser_Success() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        UserId expectedUserId = UserId.from(1L);
        
        mockRepository.setSaveResult(expectedUserId);
        mockRepository.setExistsByUsernameResult(false);
        mockRepository.setExistsByEmailResult(false);
        
        // when
        UserId result = userDomainService.registerUser(username, email, password);
        
        // then
        assertEquals(expectedUserId, result);
        assertTrue(mockRepository.isSaveCalled());
        assertTrue(mockRepository.isExistsByUsernameCalled());
        assertTrue(mockRepository.isExistsByEmailCalled());
        assertEquals(username.trim(), mockRepository.getLastExistsByUsernameParam());
        assertEquals(email.trim(), mockRepository.getLastExistsByEmailParam());
        
        User savedUser = mockRepository.getLastSavedUser();
        assertNotNull(savedUser);
        assertEquals(username.trim(), savedUser.username());
        assertEquals(email.trim().toLowerCase(), savedUser.email());
        assertEquals("encrypted_" + password.trim(), savedUser.password());
        assertTrue(mockPasswordEncoder.isEncodeCalled());
    }
    
    @DisplayName("사용자 등록 - null 사용자명으로 실패")
    @Test
    void registerUser_NullUsername_ThrowsException() {
        // given
        String username = null;
        String email = "test@example.com";
        String password = "password123";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userDomainService.registerUser(username, email, password)
        );
        
        assertEquals("Username must not be null or blank", exception.getMessage());
        assertFalse(mockRepository.isSaveCalled());
        assertFalse(mockRepository.isExistsByUsernameCalled());
    }
    
    @DisplayName("사용자 등록 - 빈 사용자명으로 실패")
    @Test
    void registerUser_EmptyUsername_ThrowsException() {
        // given
        String username = "   ";
        String email = "test@example.com";
        String password = "password123";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userDomainService.registerUser(username, email, password)
        );
        
        assertEquals("Username must not be null or blank", exception.getMessage());
        assertFalse(mockRepository.isSaveCalled());
    }
    
    @DisplayName("사용자 등록 - null 이메일로 실패")
    @Test
    void registerUser_NullEmail_ThrowsException() {
        // given
        String username = "testuser";
        String email = null;
        String password = "password123";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userDomainService.registerUser(username, email, password)
        );
        
        assertEquals("Email must not be null or blank", exception.getMessage());
        assertFalse(mockRepository.isSaveCalled());
    }
    
    @DisplayName("사용자 등록 - 중복된 사용자명으로 실패")
    @Test
    void registerUser_DuplicateUsername_ThrowsException() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        
        mockRepository.setExistsByUsernameResult(true);
        
        // when & then
        DuplicateUserException exception = assertThrows(
            DuplicateUserException.class,
            () -> userDomainService.registerUser(username, email, password)
        );
        
        assertEquals("이미 존재하는 사용자명입니다: testuser", exception.getMessage());
        assertTrue(mockRepository.isExistsByUsernameCalled());
        assertFalse(mockRepository.isSaveCalled());
    }
    
    @DisplayName("사용자 등록 - 중복된 이메일로 실패")
    @Test
    void registerUser_DuplicateEmail_ThrowsException() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        
        mockRepository.setExistsByUsernameResult(false);
        mockRepository.setExistsByEmailResult(true);
        
        // when & then
        DuplicateUserException exception = assertThrows(
            DuplicateUserException.class,
            () -> userDomainService.registerUser(username, email, password)
        );
        
        assertEquals("이미 존재하는 이메일입니다: test@example.com", exception.getMessage());
        assertTrue(mockRepository.isExistsByEmailCalled());
        assertFalse(mockRepository.isSaveCalled());
    }
    
    @DisplayName("사용자 등록 - null 비밀번호로 실패")
    @Test
    void registerUser_NullPassword_ThrowsException() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userDomainService.registerUser(username, email, password)
        );
        
        assertEquals("Password must not be null or blank", exception.getMessage());
        assertFalse(mockRepository.isSaveCalled());
    }
    
    @DisplayName("사용자 등록 - 빈 비밀번호로 실패")
    @Test
    void registerUser_EmptyPassword_ThrowsException() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = "   ";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userDomainService.registerUser(username, email, password)
        );
        
        assertEquals("Password must not be null or blank", exception.getMessage());
        assertFalse(mockRepository.isSaveCalled());
    }
    
    @DisplayName("사용자 ID로 조회 - 성공")
    @Test
    void findUserById_Success() {
        // given
        UserId userId = UserId.from(1L);
        User expectedUser = User.of(
            userId,
            "testuser",
            "test@example.com",
            "password123",
            LocalDateTime.now(),
            LocalDateTime.now(),
            false
        );
        
        mockRepository.setFindByIdResult(Optional.of(expectedUser));
        
        // when
        User result = userDomainService.findUserById(userId);
        
        // then
        assertEquals(expectedUser, result);
        assertTrue(mockRepository.isFindByIdCalled());
        assertEquals(userId, mockRepository.getLastFindByIdParam());
    }
    
    @DisplayName("사용자 ID로 조회 - null ID로 실패")
    @Test
    void findUserById_NullId_ThrowsException() {
        // given
        UserId userId = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userDomainService.findUserById(userId)
        );
        
        assertEquals("UserId must be a valid stored ID", exception.getMessage());
        assertFalse(mockRepository.isFindByIdCalled());
    }
    
    @DisplayName("사용자 ID로 조회 - 존재하지 않는 사용자로 실패")
    @Test
    void findUserById_UserNotFound_ThrowsException() {
        // given
        UserId userId = UserId.from(1L);
        mockRepository.setFindByIdResult(Optional.empty());
        
        // when & then
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userDomainService.findUserById(userId)
        );
        
        assertTrue(exception.getMessage().contains("1"));
        assertTrue(mockRepository.isFindByIdCalled());
    }
    
    @DisplayName("사용자명으로 조회 - 성공")
    @Test
    void findUserByUsername_Success() {
        // given
        String username = "testuser";
        User expectedUser = User.of(
            UserId.from(1L),
            username,
            "test@example.com",
            "password123",
            LocalDateTime.now(),
            LocalDateTime.now(),
            false
        );
        
        mockRepository.setFindByUsernameResult(Optional.of(expectedUser));
        
        // when
        User result = userDomainService.findUserByUsername(username);
        
        // then
        assertEquals(expectedUser, result);
        assertTrue(mockRepository.isFindByUsernameCalled());
        assertEquals(username.trim(), mockRepository.getLastFindByUsernameParam());
    }
    
    @DisplayName("사용자명으로 조회 - null 사용자명으로 실패")
    @Test
    void findUserByUsername_NullUsername_ThrowsException() {
        // given
        String username = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userDomainService.findUserByUsername(username)
        );
        
        assertEquals("Username must not be null or blank", exception.getMessage());
        assertFalse(mockRepository.isFindByUsernameCalled());
    }
    
    @DisplayName("사용자명으로 조회 - 존재하지 않는 사용자로 실패")
    @Test
    void findUserByUsername_UserNotFound_ThrowsException() {
        // given
        String username = "nonexistent";
        mockRepository.setFindByUsernameResult(Optional.empty());
        
        // when & then
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userDomainService.findUserByUsername(username)
        );
        
        assertTrue(exception.getMessage().contains("nonexistent"));
        assertTrue(mockRepository.isFindByUsernameCalled());
    }
    
    @DisplayName("활성 사용자 조회 - 성공")
    @Test
    void findActiveUsers_Success() {
        // given
        List<User> expectedUsers = Arrays.asList(
            User.of(UserId.from(1L), "user1", "user1@example.com", "password1", LocalDateTime.now(), LocalDateTime.now(), false),
            User.of(UserId.from(2L), "user2", "user2@example.com", "password2", LocalDateTime.now(), LocalDateTime.now(), false)
        );
        
        mockRepository.setFindActiveUsersResult(expectedUsers);
        
        // when
        List<User> result = userDomainService.findActiveUsers();
        
        // then
        assertEquals(expectedUsers, result);
        assertTrue(mockRepository.isFindActiveUsersCalled());
    }
    
    @DisplayName("이메일로 조회 - 성공")
    @Test
    void findUserByEmail_Success() {
        // given
        String email = "Test@EXAMPLE.COM";
        User expectedUser = User.of(
            UserId.from(1L),
            "testuser",
            "test@example.com",
            "password123",
            LocalDateTime.now(),
            LocalDateTime.now(),
            false
        );
        
        mockRepository.setFindByEmailResult(Optional.of(expectedUser));
        
        // when
        User result = userDomainService.findUserByEmail(email);
        
        // then
        assertEquals(expectedUser, result);
        assertTrue(mockRepository.isFindByEmailCalled());
        assertEquals("test@example.com", mockRepository.getLastFindByEmailParam());
    }
    
    @DisplayName("사용자 삭제 - 성공")
    @Test
    void deleteUser_Success() {
        // given
        UserId userId = UserId.from(1L);
        User user = User.of(userId, "testuser", "test@example.com", "password123", LocalDateTime.now(), LocalDateTime.now(), false);
        
        mockRepository.setFindByIdResult(Optional.of(user));
        mockRepository.setSaveResult(userId);
        
        // when
        userDomainService.deleteUser(userId);
        
        // then
        assertTrue(mockRepository.isFindByIdCalled());
        assertTrue(mockRepository.isSaveCalled());
        assertEquals(userId, mockRepository.getLastFindByIdParam());
        
        User savedUser = mockRepository.getLastSavedUser();
        assertNotNull(savedUser);
        assertEquals(user.id(), savedUser.id());
        assertEquals(user.username(), savedUser.username());
        assertEquals(user.email(), savedUser.email());
        assertEquals(user.password(), savedUser.password());
    }
    
    @DisplayName("비밀번호 검증 - 성공")
    @Test
    void checkPassword_Success() {
        // given
        User user = User.create("testuser", "test@example.com", "encrypted_password123");
        String rawPassword = "password123";
        
        mockPasswordEncoder.setMatchesResult(true);
        
        // when
        boolean result = userDomainService.checkPassword(user, rawPassword);
        
        // then
        assertTrue(result);
        assertTrue(mockPasswordEncoder.isMatchesCalled());
        assertEquals(rawPassword, mockPasswordEncoder.getLastMatchesRawPassword());
        assertEquals("encrypted_password123", mockPasswordEncoder.getLastMatchesEncryptedPassword());
    }
    
    @DisplayName("비밀번호 검증 - 실패")
    @Test
    void checkPassword_Failure() {
        // given
        User user = User.create("testuser", "test@example.com", "encrypted_password123");
        String wrongPassword = "wrongpassword";
        
        mockPasswordEncoder.setMatchesResult(false);
        
        // when
        boolean result = userDomainService.checkPassword(user, wrongPassword);
        
        // then
        assertFalse(result);
        assertTrue(mockPasswordEncoder.isMatchesCalled());
    }
    
    @DisplayName("비밀번호 변경 - 성공")
    @Test
    void changePassword_Success() {
        // given
        UserId userId = UserId.from(1L);
        User user = User.of(userId, "testuser", "test@example.com", "old_encrypted_password", LocalDateTime.now(), LocalDateTime.now(), false);
        String newRawPassword = "newpassword123";
        
        mockRepository.setFindByIdResult(Optional.of(user));
        mockRepository.setSaveResult(userId);
        mockPasswordEncoder.setEncodeResult("encrypted_newpassword123");
        
        // when
        userDomainService.changePassword(userId, newRawPassword);
        
        // then
        assertTrue(mockRepository.isFindByIdCalled());
        assertTrue(mockPasswordEncoder.isEncodeCalled());
        assertTrue(mockRepository.isSaveCalled());
        assertEquals(newRawPassword, mockPasswordEncoder.getLastEncodeParam());
        
        User savedUser = mockRepository.getLastSavedUser();
        assertEquals("encrypted_newpassword123", savedUser.password());
    }
    
    @DisplayName("생성자 - null 레포지토리로 실패")
    @Test
    void constructor_NullRepository_ThrowsException() {
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new UserDomainService(null, mockPasswordEncoder)
        );
        
        assertEquals("UserRepository must not be null", exception.getMessage());
    }
    
    @DisplayName("생성자 - null 패스워드 인코더로 실패")
    @Test
    void constructor_NullPasswordEncoder_ThrowsException() {
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new UserDomainService(mockRepository, null)
        );
        
        assertEquals("PasswordEncoder must not be null", exception.getMessage());
    }
    
    /**
     * Mock UserRepository 구현체
     * Mockito 라이브러리 사용 금지로 직접 Mock 객체 구현
     */
    private static class MockUserRepository implements UserRepository {
        private boolean saveCalled = false;
        private boolean findByIdCalled = false;
        private boolean findByUsernameCalled = false;
        private boolean findByEmailCalled = false;
        private boolean findActiveUsersCalled = false;
        private boolean existsByUsernameCalled = false;
        private boolean existsByEmailCalled = false;
        
        private User lastSavedUser;
        private UserId lastFindByIdParam;
        private String lastFindByUsernameParam;
        private String lastFindByEmailParam;
        private String lastExistsByUsernameParam;
        private String lastExistsByEmailParam;
        
        private UserId saveResult;
        private Optional<User> findByIdResult = Optional.empty();
        private Optional<User> findByUsernameResult = Optional.empty();
        private Optional<User> findByEmailResult = Optional.empty();
        private List<User> findActiveUsersResult = Arrays.asList();
        private boolean existsByUsernameResult = false;
        private boolean existsByEmailResult = false;
        
        @Override
        public UserId save(User user) {
            saveCalled = true;
            lastSavedUser = user;
            return saveResult;
        }
        
        @Override
        public Optional<User> findById(UserId id) {
            findByIdCalled = true;
            lastFindByIdParam = id;
            return findByIdResult;
        }
        
        @Override
        public Optional<User> findByUsername(String username) {
            findByUsernameCalled = true;
            lastFindByUsernameParam = username;
            return findByUsernameResult;
        }
        
        @Override
        public Optional<User> findByEmail(String email) {
            findByEmailCalled = true;
            lastFindByEmailParam = email;
            return findByEmailResult;
        }
        
        @Override
        public List<User> findActiveUsers() {
            findActiveUsersCalled = true;
            return findActiveUsersResult;
        }
        
        @Override
        public boolean existsByUsername(String username) {
            existsByUsernameCalled = true;
            lastExistsByUsernameParam = username;
            return existsByUsernameResult;
        }
        
        @Override
        public List<User> findDeletedUsers() {
            return Arrays.asList();
        }
        
        @Override
        public boolean existsByEmail(String email) {
            existsByEmailCalled = true;
            lastExistsByEmailParam = email;
            return existsByEmailResult;
        }
        
        // Test helper methods
        public void setSaveResult(UserId result) { this.saveResult = result; }
        public void setFindByIdResult(Optional<User> result) { this.findByIdResult = result; }
        public void setFindByUsernameResult(Optional<User> result) { this.findByUsernameResult = result; }
        public void setFindByEmailResult(Optional<User> result) { this.findByEmailResult = result; }
        public void setFindActiveUsersResult(List<User> result) { this.findActiveUsersResult = result; }
        public void setExistsByUsernameResult(boolean result) { this.existsByUsernameResult = result; }
        public void setExistsByEmailResult(boolean result) { this.existsByEmailResult = result; }
        
        public boolean isSaveCalled() { return saveCalled; }
        public boolean isFindByIdCalled() { return findByIdCalled; }
        public boolean isFindByUsernameCalled() { return findByUsernameCalled; }
        public boolean isFindByEmailCalled() { return findByEmailCalled; }
        public boolean isFindActiveUsersCalled() { return findActiveUsersCalled; }
        public boolean isExistsByUsernameCalled() { return existsByUsernameCalled; }
        public boolean isExistsByEmailCalled() { return existsByEmailCalled; }
        
        public User getLastSavedUser() { return lastSavedUser; }
        public UserId getLastFindByIdParam() { return lastFindByIdParam; }
        public String getLastFindByUsernameParam() { return lastFindByUsernameParam; }
        public String getLastFindByEmailParam() { return lastFindByEmailParam; }
        public String getLastExistsByUsernameParam() { return lastExistsByUsernameParam; }
        public String getLastExistsByEmailParam() { return lastExistsByEmailParam; }
    }
    
    /**
     * Mock PasswordEncoder 구현체
     * Mockito 라이브러리 사용 금지로 직접 Mock 객체 구현
     */
    private static class MockPasswordEncoder implements PasswordEncoder {
        private boolean encodeCalled = false;
        private boolean matchesCalled = false;
        
        private String lastEncodeParam;
        private String lastMatchesRawPassword;
        private String lastMatchesEncryptedPassword;
        
        private String encodeResult = "encrypted_password";
        private boolean matchesResult = true;
        
        @Override
        public String encode(String rawPassword) {
            encodeCalled = true;
            lastEncodeParam = rawPassword;
            return "encrypted_" + rawPassword;
        }
        
        @Override
        public boolean matches(String rawPassword, String encryptedPassword) {
            matchesCalled = true;
            lastMatchesRawPassword = rawPassword;
            lastMatchesEncryptedPassword = encryptedPassword;
            return matchesResult;
        }
        
        @Override
        public String getAlgorithm() {
            return "MOCK";
        }
        
        // Test helper methods
        public void setEncodeResult(String result) { this.encodeResult = result; }
        public void setMatchesResult(boolean result) { this.matchesResult = result; }
        
        public boolean isEncodeCalled() { return encodeCalled; }
        public boolean isMatchesCalled() { return matchesCalled; }
        
        public String getLastEncodeParam() { return lastEncodeParam; }
        public String getLastMatchesRawPassword() { return lastMatchesRawPassword; }
        public String getLastMatchesEncryptedPassword() { return lastMatchesEncryptedPassword; }
    }
}