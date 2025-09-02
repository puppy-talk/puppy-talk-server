package com.puppytalk.auth;

import com.puppytalk.user.User;
import com.puppytalk.user.UserId;
import com.puppytalk.user.UserDomainService;
import com.puppytalk.user.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthenticationDomainService 테스트")
class AuthenticationDomainServiceTest {

    private AuthenticationDomainService authenticationDomainService;
    private TestUserDomainService userDomainService;
    private TestTokenProvider tokenProvider;
    private TestTokenStore tokenStore;

    @BeforeEach
    void setUp() {
        userDomainService = new TestUserDomainService();
        tokenProvider = new TestTokenProvider();
        tokenStore = new TestTokenStore();
        authenticationDomainService = new AuthenticationDomainService(
            userDomainService, tokenProvider, tokenStore);
    }

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("유효한 의존성으로 생성에 성공한다")
        void constructor_WithValidDependencies_Success() {
            // given & when & then
            assertThatCode(() -> new AuthenticationDomainService(
                userDomainService, tokenProvider, tokenStore))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("userDomainService가 null이면 예외가 발생한다")
        void constructor_WithNullUserDomainService_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> new AuthenticationDomainService(
                null, tokenProvider, tokenStore))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserDomainService");
        }

        @Test
        @DisplayName("tokenProvider가 null이면 예외가 발생한다")
        void constructor_WithNullTokenProvider_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> new AuthenticationDomainService(
                userDomainService, null, tokenStore))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TokenProvider");
        }

        @Test
        @DisplayName("tokenStore가 null이면 예외가 발생한다")
        void constructor_WithNullTokenStore_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> new AuthenticationDomainService(
                userDomainService, tokenProvider, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TokenStore");
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("유효한 자격증명으로 로그인에 성공한다")
        void login_WithValidCredentials_Success() {
            // given
            String username = "testuser";
            String password = "password123";
            User user = createTestUser();
            userDomainService.users.put(username, user);
            userDomainService.passwordCheckResult = true;

            // when
            JwtToken token = authenticationDomainService.login(username, password);

            // then
            assertThat(token).isNotNull();
            assertThat(token.accessToken()).isEqualTo("generated_token_" + user.getId().getValue());
            assertThat(userDomainService.updateLastActiveTimeCalled).isTrue();
            assertThat(tokenStore.storedTokens).hasSize(1);
            
            TestTokenStore.StoredToken storedToken = tokenStore.storedTokens.get(0);
            assertThat(storedToken.userId()).isEqualTo(user.getId());
            assertThat(storedToken.accessToken()).isEqualTo(token.accessToken());
        }

        @Test
        @DisplayName("공백이 포함된 username이 trim된다")
        void login_WithWhitespaceUsername_TrimmedAndSuccess() {
            // given
            String username = "testuser";
            String password = "password123";
            User user = createTestUser();
            userDomainService.users.put(username, user);
            userDomainService.passwordCheckResult = true;

            // when
            JwtToken token = authenticationDomainService.login("  " + username + "  ", password);

            // then
            assertThat(token).isNotNull();
            assertThat(userDomainService.getUserByUsernameCalled).contains(username);
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 로그인하면 예외가 발생한다")
        void login_WithNonExistentUser_ThrowsException() {
            // given
            String username = "nonexistent";
            String password = "password123";

            // when & then
            assertThatThrownBy(() -> authenticationDomainService.login(username, password))
                .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인하면 예외가 발생한다")
        void login_WithWrongPassword_ThrowsException() {
            // given
            String username = "testuser";
            String password = "wrongpassword";
            User user = createTestUser();
            userDomainService.users.put(username, user);
            userDomainService.passwordCheckResult = false;

            // when & then
            assertThatThrownBy(() -> authenticationDomainService.login(username, password))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("인증 정보가 올바르지 않습니다");
        }

        @Test
        @DisplayName("username이 null이면 예외가 발생한다")
        void login_WithNullUsername_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> authenticationDomainService.login(null, "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
        }

        @Test
        @DisplayName("password가 null이면 예외가 발생한다")
        void login_WithNullPassword_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> authenticationDomainService.login("testuser", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password");
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class ValidateTokenTest {

        @Test
        @DisplayName("유효한 토큰 검증에 성공한다")
        void validateToken_WithValidToken_Success() {
            // given
            String accessToken = "valid_token";
            tokenStore.activeTokens.add(accessToken);
            tokenProvider.validTokens.add(accessToken);

            // when & then
            assertThatCode(() -> authenticationDomainService.validateToken(accessToken))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("비활성 토큰이면 예외가 발생한다")
        void validateToken_WithInactiveToken_ThrowsException() {
            // given
            String accessToken = "inactive_token";
            tokenProvider.validTokens.add(accessToken);

            // when & then
            assertThatThrownBy(() -> authenticationDomainService.validateToken(accessToken))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("유효하지 않은 JWT 토큰이면 예외가 발생한다")
        void validateToken_WithInvalidJwt_ThrowsException() {
            // given
            String accessToken = "invalid_jwt";
            tokenStore.activeTokens.add(accessToken);

            // when & then
            assertThatThrownBy(() -> authenticationDomainService.validateToken(accessToken))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("토큰이 null이면 예외가 발생한다")
        void validateToken_WithNullToken_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> authenticationDomainService.validateToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AccessToken");
        }
    }

    @Nested
    @DisplayName("토큰에서 사용자 정보 조회 테스트")
    class GetUserFromTokenTest {

        @Test
        @DisplayName("유효한 토큰으로 사용자 조회에 성공한다")
        void getUserFromToken_WithValidToken_ReturnsUser() {
            // given
            String accessToken = "valid_token";
            UserId userId = UserId.from(1L);
            User user = createTestUser();
            
            tokenProvider.tokenUserIds.put(accessToken, userId);
            userDomainService.users.put("testuser", user);
            userDomainService.userById.put(userId, user);

            // when
            User result = authenticationDomainService.getUserFromToken(accessToken);

            // then
            assertThat(result).isEqualTo(user);
        }

        @Test
        @DisplayName("토큰이 null이면 예외가 발생한다")
        void getUserFromToken_WithNullToken_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> authenticationDomainService.getUserFromToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AccessToken");
        }
    }

    @Nested
    @DisplayName("토큰 검증 및 사용자 조회 테스트")
    class ValidateTokenAndGetUserTest {

        @Test
        @DisplayName("유효한 토큰으로 검증 및 사용자 조회에 성공한다")
        void validateTokenAndGetUser_WithValidToken_ReturnsUser() {
            // given
            String accessToken = "valid_token";
            UserId userId = UserId.from(1L);
            User user = createTestUser();
            
            tokenStore.activeTokens.add(accessToken);
            tokenProvider.validTokens.add(accessToken);
            tokenProvider.tokenUserIds.put(accessToken, userId);
            userDomainService.userById.put(userId, user);

            // when
            User result = authenticationDomainService.validateTokenAndGetUser(accessToken);

            // then
            assertThat(result).isEqualTo(user);
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {

        @Test
        @DisplayName("사용자 ID로 모든 토큰 무효화에 성공한다")
        void logout_WithUserId_Success() {
            // given
            UserId userId = UserId.from(1L);

            // when
            authenticationDomainService.logout(userId);

            // then
            assertThat(tokenStore.invalidatedUserIds).contains(userId);
        }

        @Test
        @DisplayName("특정 토큰 무효화에 성공한다")
        void logoutToken_WithToken_Success() {
            // given
            String accessToken = "token_to_invalidate";

            // when
            authenticationDomainService.logoutToken(accessToken);

            // then
            assertThat(tokenStore.invalidatedTokens).contains(accessToken);
        }

        @Test
        @DisplayName("userId가 null이면 예외가 발생한다")
        void logout_WithNullUserId_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> authenticationDomainService.logout(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId");
        }
    }

    @Nested
    @DisplayName("활성 토큰 조회 테스트")
    class GetActiveTokensTest {

        @Test
        @DisplayName("사용자의 활성 토큰 조회에 성공한다")
        void getActiveTokens_WithUserId_ReturnsActiveTokens() {
            // given
            UserId userId = UserId.from(1L);
            List<ActiveTokenInfo> expectedTokens = List.of(
                new ActiveTokenInfo(userId, "token1", LocalDateTime.now().plusHours(1), 
                    LocalDateTime.now(), "client1"),
                new ActiveTokenInfo(userId, "token2", LocalDateTime.now().plusHours(2), 
                    LocalDateTime.now(), "client2")
            );
            tokenStore.activeTokensByUser.put(userId, expectedTokens);

            // when
            List<ActiveTokenInfo> result = authenticationDomainService.getActiveTokens(userId);

            // then
            assertThat(result).isEqualTo(expectedTokens);
        }

        @Test
        @DisplayName("userId가 null이면 예외가 발생한다")
        void getActiveTokens_WithNullUserId_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> authenticationDomainService.getActiveTokens(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId");
        }
    }

    // 테스트용 더미 구현체들
    private User createTestUser() {
        UserId userId = UserId.from(1L);
        LocalDateTime now = LocalDateTime.now();
        return User.of(userId, "testuser", "test@example.com", "encoded_password", now, now, now, false);
    }

    private static class TestUserDomainService extends UserDomainService {
        private final java.util.Map<String, User> users = new java.util.HashMap<>();
        private final java.util.Map<UserId, User> userById = new java.util.HashMap<>();
        private final java.util.List<String> getUserByUsernameCalled = new java.util.ArrayList<>();
        private boolean passwordCheckResult = true;
        private boolean updateLastActiveTimeCalled = false;

        public TestUserDomainService() {
            super(null, null);
        }

        @Override
        public User getUserByUsername(String username) {
            getUserByUsernameCalled.add(username);
            User user = users.get(username);
            if (user == null) {
                throw UserNotFoundException.byUsername(username);
            }
            return user;
        }

        @Override
        public User getUserById(UserId userId) {
            User user = userById.get(userId);
            if (user == null) {
                throw UserNotFoundException.byId(userId);
            }
            return user;
        }

        @Override
        public boolean checkPassword(User user, String rawPassword) {
            return passwordCheckResult;
        }

        @Override
        public void updateLastActiveTime(UserId userId) {
            updateLastActiveTimeCalled = true;
        }
    }

    private static class TestTokenProvider implements TokenProvider {
        private final java.util.Map<UserId, String> generatedTokens = new java.util.HashMap<>();
        private final java.util.Map<String, UserId> tokenUserIds = new java.util.HashMap<>();
        private final java.util.Set<String> validTokens = new java.util.HashSet<>();

        @Override
        public JwtToken generateToken(UserId userId, String username) {
            String tokenValue = "generated_token_" + userId.getValue();
            generatedTokens.put(userId, tokenValue);
            tokenUserIds.put(tokenValue, userId);
            return new JwtToken(tokenValue, LocalDateTime.now().plusHours(1));
        }

        @Override
        public UserId getUserIdFromToken(String accessToken) {
            return tokenUserIds.get(accessToken);
        }

        @Override
        public boolean validateToken(String token) {
            return validTokens.contains(token);
        }
    }

    private static class TestTokenStore implements TokenStore {
        private final java.util.List<StoredToken> storedTokens = new java.util.ArrayList<>();
        private final java.util.Set<String> activeTokens = new java.util.HashSet<>();
        private final java.util.List<UserId> invalidatedUserIds = new java.util.ArrayList<>();
        private final java.util.List<String> invalidatedTokens = new java.util.ArrayList<>();
        private final java.util.Map<UserId, List<ActiveTokenInfo>> activeTokensByUser = new java.util.HashMap<>();

        @Override
        public void storeToken(UserId userId, String accessToken, LocalDateTime tokenExpiry) {
            storedTokens.add(new StoredToken(userId, accessToken, tokenExpiry));
        }

        @Override
        public boolean isTokenActive(String accessToken) {
            return activeTokens.contains(accessToken);
        }

        @Override
        public void invalidateAllTokensForUser(UserId userId) {
            invalidatedUserIds.add(userId);
        }

        @Override
        public void invalidateToken(String accessToken) {
            invalidatedTokens.add(accessToken);
        }

        @Override
        public List<ActiveTokenInfo> getActiveTokensForUser(UserId userId) {
            return activeTokensByUser.getOrDefault(userId, List.of());
        }

        @Override
        public int cleanupExpiredTokens() {
            return 0;
        }

        @Override
        public java.util.Optional<UserId> getUserIdByToken(String accessToken) {
            return java.util.Optional.empty();
        }

        public record StoredToken(UserId userId, String accessToken, LocalDateTime tokenExpiry) {}
    }
}