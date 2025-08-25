package com.puppytalk.unit.user;

import com.puppytalk.user.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password 값 객체 테스트")
class PasswordTest {

    @Test
    @DisplayName("평문 비밀번호로 객체 생성 - 성공")
    void fromRawPassword_Success() {
        // given
        String rawPassword = "testPassword123!";

        // when
        Password password = Password.fromRawPassword(rawPassword);

        // then
        assertNotNull(password);
        assertNotNull(password.value());
        assertFalse(password.value().isEmpty());
        assertNotEquals(rawPassword, password.value());
    }

    @Test
    @DisplayName("암호화된 비밀번호로 객체 생성 - 성공")
    void fromEncryptedPassword_Success() {
        // given
        String rawPassword = "testPassword123!";
        Password originalPassword = Password.fromRawPassword(rawPassword);
        String encryptedPassword = originalPassword.value();

        // when
        Password password = Password.fromEncryptedPassword(encryptedPassword);

        // then
        assertNotNull(password);
        assertEquals(encryptedPassword, password.value());
        assertTrue(password.matches(rawPassword));
    }

    @Test
    @DisplayName("동일한 비밀번호라도 매번 다른 암호화 결과 생성")
    void fromRawPassword_DifferentSaltEveryTime() {
        // given
        String rawPassword = "testPassword123!";

        // when
        Password password1 = Password.fromRawPassword(rawPassword);
        Password password2 = Password.fromRawPassword(rawPassword);

        // then
        assertNotEquals(password1.value(), password2.value());
        assertNotEquals(password1, password2);
        assertTrue(password1.matches(rawPassword));
        assertTrue(password2.matches(rawPassword));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("평문 비밀번호 객체 생성 - 유효하지 않은 입력 시 예외 발생")
    void fromRawPassword_InvalidInput_ThrowsException(String invalidPassword) {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> 
            Password.fromRawPassword(invalidPassword)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("암호화된 비밀번호 객체 생성 - 유효하지 않은 입력 시 예외 발생")
    void fromEncryptedPassword_InvalidInput_ThrowsException(String invalidPassword) {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> 
            Password.fromEncryptedPassword(invalidPassword)
        );
    }

    @Test
    @DisplayName("비밀번호 검증 - 올바른 비밀번호")
    void matches_CorrectPassword() {
        // given
        String rawPassword = "testPassword123!";
        Password password = Password.fromRawPassword(rawPassword);

        // when & then
        assertTrue(password.matches(rawPassword));
    }

    @Test
    @DisplayName("비밀번호 검증 - 잘못된 비밀번호")
    void matches_WrongPassword() {
        // given
        String rawPassword = "testPassword123!";
        String wrongPassword = "wrongPassword456!";
        Password password = Password.fromRawPassword(rawPassword);

        // when & then
        assertFalse(password.matches(wrongPassword));
    }

    @Test
    @DisplayName("비밀번호 검증 - null 입력")
    void matches_NullInput() {
        // given
        Password password = Password.fromRawPassword("testPassword123!");

        // when & then
        assertFalse(password.matches(null));
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    void change_Success() {
        // given
        String originalPassword = "originalPassword123!";
        String newPassword = "newPassword456!";
        Password password = Password.fromRawPassword(originalPassword);

        // when
        Password changedPassword = password.change(newPassword);

        // then
        assertNotNull(changedPassword);
        assertNotEquals(password, changedPassword);
        assertNotEquals(password.value(), changedPassword.value());
        
        // 원래 비밀번호는 검증 실패
        assertFalse(changedPassword.matches(originalPassword));
        // 새 비밀번호는 검증 성공
        assertTrue(changedPassword.matches(newPassword));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("비밀번호 변경 - 유효하지 않은 비밀번호로 실패")
    void change_InvalidPassword_ThrowsException(String invalidPassword) {
        // given
        Password password = Password.fromRawPassword("testPassword123!");

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
            password.change(invalidPassword)
        );
    }

    @Test
    @DisplayName("복잡한 비밀번호 패턴 테스트")
    void createAndMatch_ComplexPasswords() {
        // given
        String[] complexPasswords = {
            "MyComplex@Password123!",
            "특수문자포함된비밀번호!@#$%",
            "1234567890!@#$%^&*()",
            "MixedCase123!@#Korean한글"
        };

        for (String rawPassword : complexPasswords) {
            // when
            Password password = Password.fromRawPassword(rawPassword);

            // then
            assertTrue(password.matches(rawPassword), "복잡한 비밀번호 검증 실패: " + rawPassword);
            assertNotEquals(rawPassword, password.value(), "비밀번호가 암호화되지 않음: " + rawPassword);
        }
    }

    @Test
    @DisplayName("equals 메서드 - 동일한 암호화된 값")
    void equals_SameEncryptedValue() {
        // given
        String rawPassword = "testPassword123!";
        Password password1 = Password.fromRawPassword(rawPassword);
        Password password2 = Password.fromEncryptedPassword(password1.value());

        // when & then
        assertEquals(password1, password2);
        assertEquals(password1.hashCode(), password2.hashCode());
    }

    @Test
    @DisplayName("equals 메서드 - 다른 암호화된 값")
    void equals_DifferentEncryptedValue() {
        // given
        Password password1 = Password.fromRawPassword("password123!");
        Password password2 = Password.fromRawPassword("password456!");

        // when & then
        assertNotEquals(password1, password2);
    }

    @Test
    @DisplayName("toString 메서드 - 보안을 위해 실제 값 숨김")
    void toString_HidesActualValue() {
        // given
        Password password = Password.fromRawPassword("secretPassword123!");

        // when
        String result = password.toString();

        // then
        assertEquals("Password{***}", result);
        assertFalse(result.contains("secretPassword123!"));
        assertFalse(result.contains(password.value()));
    }
}