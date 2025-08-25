package com.puppytalk.unit.user;

import com.puppytalk.user.User;
import com.puppytalk.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User 비밀번호 기능 테스트")
class UserPasswordTest {

    @Test
    @DisplayName("사용자 생성 - 비밀번호 암호화 확인")
    void create_PasswordEncrypted() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String rawPassword = "password123!";

        // when
        User user = User.create(username, email, rawPassword);

        // then
        assertNotNull(user.password());
        assertNotEquals(rawPassword, user.password());
        assertTrue(user.password().length() > rawPassword.length()); // Base64 인코딩으로 길어짐
    }

    @Test
    @DisplayName("비밀번호 검증 - 올바른 비밀번호")
    void checkPassword_CorrectPassword() {
        // given
        String rawPassword = "password123!";
        User user = User.create("testuser", "test@example.com", rawPassword);

        // when & then
        assertTrue(user.checkPassword(rawPassword));
    }

    @Test
    @DisplayName("비밀번호 검증 - 잘못된 비밀번호")
    void checkPassword_WrongPassword() {
        // given
        String rawPassword = "password123!";
        String wrongPassword = "wrongPassword456!";
        User user = User.create("testuser", "test@example.com", rawPassword);

        // when & then
        assertFalse(user.checkPassword(wrongPassword));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("비밀번호 검증 - 유효하지 않은 입력")
    void checkPassword_InvalidInput(String invalidPassword) {
        // given
        User user = User.create("testuser", "test@example.com", "password123!");

        // when & then
        assertFalse(user.checkPassword(invalidPassword));
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    void changePassword_Success() {
        // given
        String originalPassword = "password123!";
        String newPassword = "newPassword456!";
        User user = User.create("testuser", "test@example.com", originalPassword);

        // when
        User updatedUser = user.changePassword(newPassword);

        // then
        assertNotNull(updatedUser);
        // User는 ID로 equals를 판단하므로, 필드 단위로 비교
        assertEquals(user.id(), updatedUser.id());
        assertEquals(user.username(), updatedUser.username());
        assertEquals(user.email(), updatedUser.email());
        assertEquals(user.createdAt(), updatedUser.createdAt());
        assertEquals(user.isDeleted(), updatedUser.isDeleted());
        
        // 원래 비밀번호는 검증 실패
        assertFalse(updatedUser.checkPassword(originalPassword));
        // 새 비밀번호는 검증 성공
        assertTrue(updatedUser.checkPassword(newPassword));
        
        // 암호화된 비밀번호는 다름
        assertNotEquals(user.password(), updatedUser.password());
        assertNotEquals(newPassword, updatedUser.password());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("비밀번호 변경 - 유효하지 않은 비밀번호로 실패")
    void changePassword_InvalidPassword_ThrowsException(String invalidPassword) {
        // given
        User user = User.create("testuser", "test@example.com", "password123!");

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
            user.changePassword(invalidPassword)
        );
    }

    @Test
    @DisplayName("복잡한 비밀번호 패턴 테스트")
    void createAndCheckPassword_ComplexPasswords() {
        // given
        String[] complexPasswords = {
            "MyComplex@Password123!",
            "특수문자포함된비밀번호!@#$%",
            "1234567890!@#$%^&*()",
            "MixedCase123!@#Korean한글"
        };

        for (String password : complexPasswords) {
            // when
            User user = User.create("testuser", "test@example.com", password);

            // then
            assertTrue(user.checkPassword(password), "복잡한 비밀번호 검증 실패: " + password);
            assertNotEquals(password, user.password(), "비밀번호가 암호화되지 않음: " + password);
        }
    }

    @Test
    @DisplayName("동일한 비밀번호라도 매번 다른 암호화 결과")
    void create_SamePassword_DifferentEncryption() {
        // given
        String password = "samePassword123!";

        // when
        User user1 = User.create("user1", "user1@example.com", password);
        User user2 = User.create("user2", "user2@example.com", password);

        // then
        assertNotEquals(user1.password(), user2.password());
        assertTrue(user1.checkPassword(password));
        assertTrue(user2.checkPassword(password));
    }

    @Test
    @DisplayName("사용자 상태 변경 시 비밀번호 유지")
    void statusChange_PasswordPreserved() {
        // given
        String password = "password123!";
        User user = User.create("testuser", "test@example.com", password);

        // when
        User deletedUser = user.withDeletedStatus();
        User restoredUser = deletedUser.withRestoredStatus();

        // then
        assertTrue(user.checkPassword(password));
        assertTrue(deletedUser.checkPassword(password));
        assertTrue(restoredUser.checkPassword(password));
        
        // 암호화된 비밀번호는 동일하게 유지
        assertEquals(user.password(), deletedUser.password());
        assertEquals(user.password(), restoredUser.password());
    }

    @Test
    @DisplayName("저장된 사용자 객체 생성 시 비밀번호 확인")
    void of_WithEncryptedPassword() {
        // given
        String rawPassword = "password123!";
        User originalUser = User.create("testuser", "test@example.com", rawPassword);
        
        UserId id = UserId.of(1L);
        String encryptedPassword = originalUser.password();

        // when
        User storedUser = User.of(id, "testuser", "test@example.com", encryptedPassword, 
                                 LocalDateTime.now(), false);

        // then
        assertNotNull(storedUser);
        assertEquals(encryptedPassword, storedUser.password());
        assertTrue(storedUser.checkPassword(rawPassword));
    }
}