package com.puppytalk.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserDomainService 테스트")
class UserDomainServiceTest {

    private UserDomainService userDomainService;
    private TestUserRepository userRepository;
    private TestPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = new TestUserRepository();
        passwordEncoder = new TestPasswordEncoder();
        userDomainService = new UserDomainService(userRepository, passwordEncoder);
    }

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("유효한 의존성으로 생성에 성공한다")
        void constructor_WithValidDependencies_Success() {
            // given & when & then
            assertThatCode(() -> new UserDomainService(userRepository, passwordEncoder))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("userRepository가 null이면 예외가 발생한다")
        void constructor_WithNullUserRepository_ThrowsException() {
            // given & when & then
            assertThatThrownBy(() -> new UserDomainService(null, passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserRepository");
        }

        @Test
        @DisplayName("passwordEncoder가 null이면 예외가 발생한다")
        void constructor_WithNullPasswordEncoder_ThrowsException() {
            // given & when & then
            assertThatThrownBy(() -> new UserDomainService(userRepository, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PasswordEncoder");
        }
    }

    @Nested
    @DisplayName("사용자 등록 테스트")
    class RegisterUserTest {

        @Test
        @DisplayName("유효한 정보로 사용자 등록에 성공한다")
        void registerUser_WithValidInfo_Success() {
            // given
            String username = "testuser";
            String email = "test@example.com";
            String rawPassword = "password123";

            // when
            UserId userId = userDomainService.registerUser(username, email, rawPassword);

            // then
            assertThat(userId).isNotNull();
            assertThat(userRepository.savedUsers).hasSize(1);
            
            User savedUser = userRepository.savedUsers.get(0);
            assertThat(savedUser.getUsername()).isEqualTo(username);
            assertThat(savedUser.getEmail()).isEqualTo(email);
            assertThat(savedUser.getPassword()).isEqualTo("encoded_" + rawPassword);
            assertThat(passwordEncoder.encodeCalled).isTrue();
        }

        @Test
        @DisplayName("공백이 포함된 username과 email이 trim된다")
        void registerUser_WithWhitespace_TrimmedSuccess() {
            // given
            String username = "  testuser  ";
            String email = "  test@example.com  ";
            String rawPassword = "password123";

            // when
            userDomainService.registerUser(username, email, rawPassword);

            // then
            User savedUser = userRepository.savedUsers.get(0);
            assertThat(savedUser.getUsername()).isEqualTo("testuser");
            assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("중복된 username이면 예외가 발생한다")
        void registerUser_WithDuplicateUsername_ThrowsException() {
            // given
            String username = "testuser";
            userRepository.existingUsernames.add(username);

            // when & then
            assertThatThrownBy(() -> userDomainService.registerUser(username, "test@example.com", "password123"))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessageContaining("이미 존재하는 사용자명입니다");
        }

        @Test
        @DisplayName("중복된 email이면 예외가 발생한다")
        void registerUser_WithDuplicateEmail_ThrowsException() {
            // given
            String email = "test@example.com";
            userRepository.existingEmails.add(email);

            // when & then
            assertThatThrownBy(() -> userDomainService.registerUser("testuser", email, "password123"))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessageContaining("이미 존재하는 이메일입니다");
        }

        @Test
        @DisplayName("username이 null이면 예외가 발생한다")
        void registerUser_WithNullUsername_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> userDomainService.registerUser(null, "test@example.com", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
        }

        @Test
        @DisplayName("email이 null이면 예외가 발생한다")
        void registerUser_WithNullEmail_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> userDomainService.registerUser("testuser", null, "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
        }

        @Test
        @DisplayName("password가 null이면 예외가 발생한다")
        void registerUser_WithNullPassword_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> userDomainService.registerUser("testuser", "test@example.com", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password");
        }
    }

    @Nested
    @DisplayName("사용자 조회 테스트")
    class GetUserTest {

        @Test
        @DisplayName("ID로 사용자 조회에 성공한다")
        void getUserById_WithExistingUser_ReturnsUser() {
            // given
            UserId userId = UserId.from(1L);
            User user = createTestUser(userId);
            userRepository.users.put(userId, user);

            // when
            User foundUser = userDomainService.getUserById(userId);

            // then
            assertThat(foundUser).isEqualTo(user);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
        void getUserById_WithNonExistentUser_ThrowsException() {
            // given
            UserId userId = UserId.from(999L);

            // when & then
            assertThatThrownBy(() -> userDomainService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("username으로 사용자 조회에 성공한다")
        void getUserByUsername_WithExistingUser_ReturnsUser() {
            // given
            String username = "testuser";
            User user = createTestUser(UserId.from(1L));
            userRepository.usersByUsername.put(username, user);

            // when
            User foundUser = userDomainService.getUserByUsername(username);

            // then
            assertThat(foundUser).isEqualTo(user);
        }

        @Test
        @DisplayName("공백이 포함된 username도 trim되어 조회된다")
        void getUserByUsername_WithWhitespace_TrimmedAndFound() {
            // given
            String username = "testuser";
            User user = createTestUser(UserId.from(1L));
            userRepository.usersByUsername.put(username, user);

            // when
            User foundUser = userDomainService.getUserByUsername("  testuser  ");

            // then
            assertThat(foundUser).isEqualTo(user);
        }

        @Test
        @DisplayName("존재하지 않는 username으로 조회하면 예외가 발생한다")
        void getUserByUsername_WithNonExistentUser_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> userDomainService.getUserByUsername("nonexistent"))
                .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("email로 사용자 조회에 성공한다")
        void getUserByEmail_WithExistingUser_ReturnsUser() {
            // given
            String email = "test@example.com";
            User user = createTestUser(UserId.from(1L));
            userRepository.usersByEmail.put(email.toLowerCase(), user);

            // when
            User foundUser = userDomainService.getUserByEmail(email);

            // then
            assertThat(foundUser).isEqualTo(user);
        }

        @Test
        @DisplayName("대소문자가 다른 email도 소문자로 변환되어 조회된다")
        void getUserByEmail_WithDifferentCase_FoundWithLowerCase() {
            // given
            String email = "test@example.com";
            User user = createTestUser(UserId.from(1L));
            userRepository.usersByEmail.put(email.toLowerCase(), user);

            // when
            User foundUser = userDomainService.getUserByEmail("Test@Example.Com");

            // then
            assertThat(foundUser).isEqualTo(user);
        }
    }

    @Nested
    @DisplayName("비밀번호 관련 테스트")
    class PasswordTest {

        @Test
        @DisplayName("올바른 비밀번호 검증에 성공한다")
        void checkPassword_WithCorrectPassword_ReturnsTrue() {
            // given
            User user = createTestUser(UserId.from(1L));
            String rawPassword = "password123";

            // when
            boolean result = userDomainService.checkPassword(user, rawPassword);

            // then
            assertThat(result).isTrue();
            assertThat(passwordEncoder.matchesCalled).isTrue();
        }

        @Test
        @DisplayName("잘못된 비밀번호 검증에 실패한다")
        void checkPassword_WithWrongPassword_ReturnsFalse() {
            // given
            User user = createTestUser(UserId.from(1L));
            String wrongPassword = "wrongpassword";
            passwordEncoder.matchesResult = false;

            // when
            boolean result = userDomainService.checkPassword(user, wrongPassword);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("비밀번호 변경에 성공한다")
        void changePassword_WithValidPassword_Success() {
            // given
            UserId userId = UserId.from(1L);
            User user = createTestUser(userId);
            userRepository.users.put(userId, user);
            String newPassword = "newpassword123";

            // when
            userDomainService.changePassword(userId, newPassword);

            // then
            assertThat(userRepository.savedUsers).hasSize(1);
            User updatedUser = userRepository.savedUsers.get(0);
            assertThat(updatedUser.getPassword()).isEqualTo("encoded_" + newPassword);
        }
    }

    @Nested
    @DisplayName("사용자 활동 및 상태 테스트")
    class ActivityAndStatusTest {

        @Test
        @DisplayName("활성 사용자 목록 조회에 성공한다")
        void findActiveUsers_ReturnsActiveUsers() {
            // given
            List<User> activeUsers = List.of(
                createTestUser(UserId.from(1L)),
                createTestUser(UserId.from(2L))
            );
            userRepository.activeUsers = activeUsers;

            // when
            List<User> result = userDomainService.findActiveUsers();

            // then
            assertThat(result).isEqualTo(activeUsers);
        }

        @Test
        @DisplayName("사용자 삭제에 성공한다")
        void deleteUser_WithExistingUser_Success() {
            // given
            UserId userId = UserId.from(1L);
            User user = createTestUser(userId);
            userRepository.users.put(userId, user);

            // when
            userDomainService.deleteUser(userId);

            // then
            assertThat(userRepository.savedUsers).hasSize(1);
            User deletedUser = userRepository.savedUsers.get(0);
            assertThat(deletedUser.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("마지막 활동 시간 업데이트에 성공한다")
        void updateLastActiveTime_WithExistingUser_Success() {
            // given
            UserId userId = UserId.from(1L);
            User user = createTestUser(userId);
            LocalDateTime originalActiveTime = user.getLastActiveAt();
            userRepository.users.put(userId, user);

            // when
            userDomainService.updateLastActiveTime(userId);

            // then
            assertThat(userRepository.savedUsers).hasSize(1);
            User updatedUser = userRepository.savedUsers.get(0);
            assertThat(updatedUser.getLastActiveAt()).isAfter(originalActiveTime);
        }

        @Test
        @DisplayName("비활성 사용자 조회에 성공한다")
        void findInactiveUsers_WithCutoffTime_ReturnsInactiveUsers() {
            // given
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(1);
            List<Long> inactiveUserIds = List.of(1L, 2L, 3L);
            userRepository.inactiveUserIds = inactiveUserIds;

            // when
            List<Long> result = userDomainService.findInactiveUsers(cutoffTime);

            // then
            assertThat(result).isEqualTo(inactiveUserIds);
        }
    }

    // 테스트용 더미 구현체들
    private User createTestUser(UserId userId) {
        LocalDateTime now = LocalDateTime.now();
        return User.of(userId, "testuser", "test@example.com", "encoded_password", now, now, now, false);
    }

    private static class TestUserRepository implements UserRepository {
        private final java.util.Map<UserId, User> users = new java.util.HashMap<>();
        private final java.util.Map<String, User> usersByUsername = new java.util.HashMap<>();
        private final java.util.Map<String, User> usersByEmail = new java.util.HashMap<>();
        private final java.util.Set<String> existingUsernames = new java.util.HashSet<>();
        private final java.util.Set<String> existingEmails = new java.util.HashSet<>();
        private final java.util.List<User> savedUsers = new java.util.ArrayList<>();
        private List<User> activeUsers = List.of();
        private List<Long> inactiveUserIds = List.of();
        private UserId nextId = UserId.from(1L);

        @Override
        public UserId save(User user) {
            savedUsers.add(user);
            if (user.getId() == null) {
                UserId newId = nextId;
                nextId = UserId.from(nextId.getValue() + 1);
                return newId;
            }
            return user.getId();
        }

        @Override
        public Optional<User> findById(UserId userId) {
            return Optional.ofNullable(users.get(userId));
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.ofNullable(usersByUsername.get(username));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(usersByEmail.get(email.toLowerCase()));
        }

        @Override
        public boolean existsByUsername(String username) {
            return existingUsernames.contains(username);
        }

        @Override
        public boolean existsByEmail(String email) {
            return existingEmails.contains(email);
        }

        @Override
        public List<User> findActiveUsers() {
            return activeUsers;
        }

        @Override
        public List<Long> findInactiveUsers(LocalDateTime cutoffTime) {
            return inactiveUserIds;
        }
    }

    private static class TestPasswordEncoder implements PasswordEncoder {
        private boolean encodeCalled = false;
        private boolean matchesCalled = false;
        private boolean matchesResult = true;

        @Override
        public String encode(String rawPassword) {
            encodeCalled = true;
            return "encoded_" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            matchesCalled = true;
            return matchesResult;
        }
    }
}