package com.puppytalk.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BCrypt 패스워드 인코더 테스트")
class BCryptPasswordEncoderTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("비밀번호를 암호화한다")
    void encodePassword() {
        // given
        String rawPassword = "mySecretPassword123";

        // when
        String encoded = encoder.encode(rawPassword);

        // then
        assertThat(encoded).isNotNull();
        assertThat(encoded).isNotEqualTo(rawPassword);
        assertThat(encoded).matches("\\$2[ayb]\\$\\d{2}\\$.{53}"); // BCrypt format
        assertThat(encoded).hasSize(60); // BCrypt hash length
    }

    @Test
    @DisplayName("같은 비밀번호라도 매번 다른 해시를 생성한다")
    void encodeSamePasswordDifferently() {
        // given
        String rawPassword = "password123";

        // when
        String encoded1 = encoder.encode(rawPassword);
        String encoded2 = encoder.encode(rawPassword);

        // then
        assertThat(encoded1).isNotEqualTo(encoded2);
        assertThat(encoder.matches(rawPassword, encoded1)).isTrue();
        assertThat(encoder.matches(rawPassword, encoded2)).isTrue();
    }

    @Test
    @DisplayName("올바른 비밀번호인 경우 매칭에 성공한다")
    void matchesCorrectPassword() {
        // given
        String rawPassword = "correctPassword";
        String encoded = encoder.encode(rawPassword);

        // when & then
        assertThat(encoder.matches(rawPassword, encoded)).isTrue();
    }

    @Test
    @DisplayName("잘못된 비밀번호인 경우 매칭에 실패한다")
    void matchesIncorrectPassword() {
        // given
        String rawPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String encoded = encoder.encode(rawPassword);

        // when & then
        assertThat(encoder.matches(wrongPassword, encoded)).isFalse();
    }

    @Test
    @DisplayName("null 비밀번호로 인코딩하면 예외가 발생한다")
    void encodeNullPassword() {
        assertThatThrownBy(() -> encoder.encode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 필수입니다");
    }

    @Test
    @DisplayName("공백 비밀번호로 인코딩하면 예외가 발생한다")
    void encodeBlankPassword() {
        assertThatThrownBy(() -> encoder.encode("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 필수입니다");
    }

    @Test
    @DisplayName("null 입력으로 매칭하면 false를 반환한다")
    void matchesWithNullInput() {
        String encoded = encoder.encode("password");

        assertThat(encoder.matches(null, encoded)).isFalse();
        assertThat(encoder.matches("password", null)).isFalse();
        assertThat(encoder.matches(null, null)).isFalse();
    }

    @Test
    @DisplayName("잘못된 해시 형식으로 매칭하면 false를 반환한다")
    void matchesWithInvalidHash() {
        assertThat(encoder.matches("password", "invalid-hash")).isFalse();
        assertThat(encoder.matches("password", "")).isFalse();
    }

    @Test
    @DisplayName("알고리즘 이름을 반환한다")
    void getAlgorithm() {
        assertThat(encoder.getAlgorithm()).isEqualTo("BCrypt");
    }

    @Test
    @DisplayName("잘못된 strength로 생성하면 예외가 발생한다")
    void invalidStrength() {
        assertThatThrownBy(() -> new BCryptPasswordEncoder(3))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new BCryptPasswordEncoder(32))
                .isInstanceOf(IllegalArgumentException.class);
    }
}