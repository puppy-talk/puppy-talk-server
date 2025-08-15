package com.puppy.talk.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // 테스트용 설정
        String secret = "test-secret-key-for-jwt-token-provider-testing-purpose-only";
        long expiration = 3600000L; // 1시간
        jwtTokenProvider = new JwtTokenProvider(secret, expiration);
    }

    @Test
    @DisplayName("JWT 토큰을 성공적으로 생성한다")
    void createToken_Success() {
        // given
        Long userId = 1L;
        String username = "testuser";

        // when
        String token = jwtTokenProvider.createToken(userId, username);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 3부분으로 구성
    }

    @Test
    @DisplayName("JWT 토큰에서 사용자 ID를 추출한다")
    void getUserIdFromToken_Success() {
        // given
        Long expectedUserId = 123L;
        String username = "testuser";
        String token = jwtTokenProvider.createToken(expectedUserId, username);

        // when
        Long actualUserId = jwtTokenProvider.getUserIdFromToken(token);

        // then
        assertThat(actualUserId).isEqualTo(expectedUserId);
    }

    @Test
    @DisplayName("JWT 토큰에서 사용자명을 추출한다")
    void getUsernameFromToken_Success() {
        // given
        Long userId = 1L;
        String expectedUsername = "testuser";
        String token = jwtTokenProvider.createToken(userId, expectedUsername);

        // when
        String actualUsername = jwtTokenProvider.getUsernameFromToken(token);

        // then
        assertThat(actualUsername).isEqualTo(expectedUsername);
    }

    @Test
    @DisplayName("유효한 JWT 토큰을 검증한다")
    void validateToken_ValidToken_ReturnsTrue() {
        // given
        Long userId = 1L;
        String username = "testuser";
        String token = jwtTokenProvider.createToken(userId, username);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 JWT 토큰을 검증한다")
    void validateToken_InvalidToken_ReturnsFalse() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("null 토큰을 검증한다")
    void validateToken_NullToken_ReturnsFalse() {
        // when
        boolean isValid = jwtTokenProvider.validateToken(null);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("빈 토큰을 검증한다")
    void validateToken_EmptyToken_ReturnsFalse() {
        // when
        boolean isValid = jwtTokenProvider.validateToken("");

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰을 검증한다")
    void isTokenExpired_ExpiredToken_ReturnsTrue() {
        // given - 매우 짧은 만료 시간으로 토큰 생성
        String shortSecret = "test-secret-key-for-jwt-token-provider-testing-purpose-only";
        long shortExpiration = 1L; // 1ms
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(shortSecret, shortExpiration);
        
        String token = shortLivedProvider.createToken(1L, "testuser");
        
        // 토큰이 만료될 때까지 대기
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // when
        boolean isExpired = shortLivedProvider.isTokenExpired(token);

        // then
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("유효한 토큰의 만료 상태를 확인한다")
    void isTokenExpired_ValidToken_ReturnsFalse() {
        // given
        Long userId = 1L;
        String username = "testuser";
        String token = jwtTokenProvider.createToken(userId, username);

        // when
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Bearer 토큰에서 실제 토큰을 추출한다")
    void resolveToken_BearerToken_ReturnsToken() {
        // given
        String actualToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciJ9.example";
        String bearerToken = "Bearer " + actualToken;

        // when
        String resolvedToken = jwtTokenProvider.resolveToken(bearerToken);

        // then
        assertThat(resolvedToken).isEqualTo(actualToken);
    }

    @Test
    @DisplayName("Bearer가 아닌 토큰에서는 null을 반환한다")
    void resolveToken_NonBearerToken_ReturnsNull() {
        // given
        String nonBearerToken = "Basic dGVzdDp0ZXN0";

        // when
        String resolvedToken = jwtTokenProvider.resolveToken(nonBearerToken);

        // then
        assertThat(resolvedToken).isNull();
    }

    @Test
    @DisplayName("null 토큰에서는 null을 반환한다")
    void resolveToken_NullToken_ReturnsNull() {
        // when
        String resolvedToken = jwtTokenProvider.resolveToken(null);

        // then
        assertThat(resolvedToken).isNull();
    }

    @Test
    @DisplayName("잘못된 형식의 토큰에서 사용자 ID 추출 시 예외가 발생한다")
    void getUserIdFromToken_InvalidToken_ThrowsException() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(invalidToken))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("잘못된 형식의 토큰에서 사용자명 추출 시 예외가 발생한다")
    void getUsernameFromToken_InvalidToken_ThrowsException() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.getUsernameFromToken(invalidToken))
            .isInstanceOf(Exception.class);
    }
}