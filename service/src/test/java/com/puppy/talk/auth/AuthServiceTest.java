package com.puppy.talk.auth;

import com.puppy.talk.user.User;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthService 테스트")
class AuthServiceTest {

    private UserRepository userRepository;
    private JwtTokenProvider jwtTokenProvider;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    private User testUser;
    private String rawPassword = "password123";
    private String hashedPassword = "$2a$10$hashedPassword";
    private String testToken = "eyJhbGciOiJIUzI1NiJ9.testToken";

    @BeforeEach
    void setUp() {
        // Mock objects 직접 생성
        userRepository = new MockUserRepository();
        jwtTokenProvider = new MockJwtTokenProvider("mockSecret", 3600000L);
        passwordEncoder = new MockPasswordEncoder();
        
        authService = new AuthService(userRepository, jwtTokenProvider, passwordEncoder);
        
        testUser = new User(
            UserIdentity.of(1L),
            "testuser",
            "test@example.com",
            hashedPassword
        );
        
        // Mock data 설정
        ((MockUserRepository) userRepository).setUser(testUser);
        ((MockPasswordEncoder) passwordEncoder).setHashedPassword(hashedPassword);
        ((MockJwtTokenProvider) jwtTokenProvider).setToken(testToken);
    }

    @Test
    @DisplayName("유효한 사용자명과 패스워드로 로그인에 성공한다")
    void login_ValidCredentials_Success() {
        // given
        ((MockUserRepository) userRepository).setUser(testUser);
        ((MockPasswordEncoder) passwordEncoder).setHashedPassword(hashedPassword);
        ((MockJwtTokenProvider) jwtTokenProvider).setToken(testToken);

        // when
        Optional<AuthService.AuthResult> result = authService.login("testuser", rawPassword);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().token()).isEqualTo(testToken);
        assertThat(result.get().user()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("존재하지 않는 사용자명으로 로그인에 실패한다")
    void login_NonExistentUser_Failure() {
        // given
        ((MockUserRepository) userRepository).setUser(null);

        // when
        Optional<AuthService.AuthResult> result = authService.login("nonexistent", rawPassword);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("잘못된 패스워드로 로그인에 실패한다")
    void login_WrongPassword_Failure() {
        // given
        ((MockPasswordEncoder) passwordEncoder).setHashedPassword("wrongHash");

        // when
        Optional<AuthService.AuthResult> result = authService.login("testuser", "wrongpassword");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("빈 사용자명으로 로그인에 실패한다")
    void login_EmptyUsername_Failure() {
        // when
        Optional<AuthService.AuthResult> result = authService.login("", rawPassword);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("null 사용자명으로 로그인에 실패한다")
    void login_NullUsername_Failure() {
        // when
        Optional<AuthService.AuthResult> result = authService.login(null, rawPassword);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("새로운 사용자 회원가입에 성공한다")
    void register_NewUser_Success() {
        // given
        String username = "newuser";
        String email = "new@example.com";
        
        ((MockUserRepository) userRepository).setUser(null); // 중복 사용자 없음
        ((MockUserRepository) userRepository).setEmailExists(false); // 중복 이메일 없음
        
        User savedUser = new User(
            UserIdentity.of(2L),
            username,
            email,
            hashedPassword
        );
        
        ((MockUserRepository) userRepository).setSavedUser(savedUser);
        ((MockJwtTokenProvider) jwtTokenProvider).setToken(testToken);

        // when
        Optional<AuthService.AuthResult> result = authService.register(username, email, rawPassword);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().token()).isEqualTo(testToken);
        assertThat(result.get().user().username()).isEqualTo(username);
        assertThat(result.get().user().email()).isEqualTo(email);
    }

    @Test
    @DisplayName("중복된 사용자명으로 회원가입에 실패한다")
    void register_DuplicateUsername_Failure() {
        // given
        String username = "testuser";
        String email = "new@example.com";
        
        ((MockUserRepository) userRepository).setUser(testUser); // 중복 사용자 존재

        // when
        Optional<AuthService.AuthResult> result = authService.register(username, email, rawPassword);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입에 실패한다")
    void register_DuplicateEmail_Failure() {
        // given
        String username = "newuser";
        String email = "test@example.com"; // 기존 사용자와 같은 이메일
        
        ((MockUserRepository) userRepository).setUser(null); // 중복 사용자 없음
        ((MockUserRepository) userRepository).setEmailExists(true); // 중복 이메일 존재

        // when
        Optional<AuthService.AuthResult> result = authService.register(username, email, rawPassword);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("짧은 패스워드로 회원가입에 실패한다")
    void register_ShortPassword_Failure() {
        // given
        String username = "newuser";
        String email = "new@example.com";
        String shortPassword = "123"; // 6자 미만

        // when
        Optional<AuthService.AuthResult> result = authService.register(username, email, shortPassword);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("유효한 JWT 토큰을 검증한다")
    void validateToken_ValidToken_Success() {
        // given
        ((MockJwtTokenProvider) jwtTokenProvider).setValidToken(true);
        ((MockJwtTokenProvider) jwtTokenProvider).setTokenExpired(false);
        ((MockJwtTokenProvider) jwtTokenProvider).setUserId(1L);

        // when
        Optional<User> result = authService.validateToken(testToken);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰을 검증한다")
    void validateToken_InvalidToken_Failure() {
        // given
        String invalidToken = "invalid.token";
        ((MockJwtTokenProvider) jwtTokenProvider).setValidToken(false);

        // when
        Optional<User> result = authService.validateToken(invalidToken);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("만료된 JWT 토큰을 검증한다")
    void validateToken_ExpiredToken_Failure() {
        // given
        ((MockJwtTokenProvider) jwtTokenProvider).setValidToken(true);
        ((MockJwtTokenProvider) jwtTokenProvider).setTokenExpired(true);

        // when
        Optional<User> result = authService.validateToken(testToken);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("JWT 토큰에서 사용자 ID를 추출한다")
    void getUserIdFromToken_ValidToken_Success() {
        // given
        ((MockJwtTokenProvider) jwtTokenProvider).setValidToken(true);
        ((MockJwtTokenProvider) jwtTokenProvider).setUserId(1L);

        // when
        Optional<Long> result = authService.getUserIdFromToken(testToken);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(1L);
    }

    @Test
    @DisplayName("유효하지 않은 토큰에서 사용자 ID 추출에 실패한다")
    void getUserIdFromToken_InvalidToken_Failure() {
        // given
        String invalidToken = "invalid.token";
        ((MockJwtTokenProvider) jwtTokenProvider).setValidToken(false);

        // when
        Optional<Long> result = authService.getUserIdFromToken(invalidToken);

        // then
        assertThat(result).isEmpty();
    }
    
    // Mock 클래스들
    private static class MockUserRepository implements UserRepository {
        private User user;
        private boolean emailExists = false;
        private User savedUser;
        
        public void setUser(User user) {
            this.user = user;
        }
        
        public void setEmailExists(boolean emailExists) {
            this.emailExists = emailExists;
        }
        
        public void setSavedUser(User savedUser) {
            this.savedUser = savedUser;
        }
        
        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.ofNullable(user);
        }
        
        @Override
        public Optional<User> findByEmail(String email) {
            return emailExists ? Optional.ofNullable(user) : Optional.empty();
        }
        
        @Override
        public Optional<User> findByIdentity(UserIdentity identity) {
            return Optional.ofNullable(user);
        }
        
        @Override
        public User save(User user) {
            return savedUser != null ? savedUser : user;
        }
        
        @Override
        public void deleteByIdentity(UserIdentity identity) {
            // Mock implementation
        }
        
        @Override
        public List<User> findAll() {
            return user != null ? List.of(user) : List.of();
        }
    }
    
    private static class MockJwtTokenProvider extends JwtTokenProvider {
        private String token;
        private boolean validToken = true;
        private boolean tokenExpired = false;
        private Long userId = 1L;
        
        public MockJwtTokenProvider(String secret, long tokenValidityInMilliseconds) {
            super(secret, tokenValidityInMilliseconds);
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        public void setValidToken(boolean validToken) {
            this.validToken = validToken;
        }
        
        public void setTokenExpired(boolean tokenExpired) {
            this.tokenExpired = tokenExpired;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        @Override
        public String createToken(Long userId, String username) {
            return token;
        }
        
        @Override
        public boolean validateToken(String token) {
            return validToken;
        }
        
        @Override
        public boolean isTokenExpired(String token) {
            return tokenExpired;
        }
        
        @Override
        public Long getUserIdFromToken(String token) {
            return userId;
        }
    }
    
    private static class MockPasswordEncoder extends PasswordEncoder {
        private String hashedPassword;
        
        public void setHashedPassword(String hashedPassword) {
            this.hashedPassword = hashedPassword;
        }
        
        @Override
        public String encode(String rawPassword) {
            return hashedPassword;
        }
        
        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            return hashedPassword.equals(encodedPassword);
        }
    }
}