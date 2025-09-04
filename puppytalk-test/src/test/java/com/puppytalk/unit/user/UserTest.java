package com.puppytalk.unit.user;

import com.puppytalk.user.User;
import com.puppytalk.user.UserId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User 엔티티 단위 테스트")
class UserTest {

    @DisplayName("User 생성 - 성공")
    @Test
    void create_Success() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String encryptedPassword = "encrypted_password";

        // when
        User user = User.create(username, email, encryptedPassword);

        // then
        assertNotNull(user);
        assertNull(user.getId()); // 아직 저장되지 않음
        assertEquals(username.trim(), user.getUsername());
        assertEquals(email.trim(), user.getEmail());
        assertEquals(encryptedPassword, user.getPassword());
        assertFalse(user.isDeleted());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt()); // 생성 시점에는 동일
    }

    @DisplayName("User 생성 - null 사용자명으로 실패")
    @Test
    void create_NullUsername_ThrowsException() {
        // given
        String username = null;
        String email = "test@example.com";
        String encryptedPassword = "encrypted_password";

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, encryptedPassword)
        );

        assertTrue(exception.getMessage().contains("Username"));
    }

    @DisplayName("User 생성 - 빈 사용자명으로 실패")
    @Test
    void create_BlankUsername_ThrowsException() {
        // given
        String username = "   ";
        String email = "test@example.com";
        String encryptedPassword = "encrypted_password";

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, encryptedPassword)
        );

        assertTrue(exception.getMessage().contains("Username"));
    }

    @DisplayName("User 생성 - 사용자명 길이 초과로 실패")
    @Test
    void create_UsernameTooLong_ThrowsException() {
        // given
        String username = "a".repeat(21); // MAX_USERNAME_LENGTH(20) 초과
        String email = "test@example.com";
        String encryptedPassword = "encrypted_password";

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, encryptedPassword)
        );

        assertTrue(exception.getMessage().contains("Username"));
    }

    @DisplayName("User 생성 - null 이메일로 실패")
    @Test
    void create_NullEmail_ThrowsException() {
        // given
        String username = "testuser";
        String email = null;
        String encryptedPassword = "encrypted_password";

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, encryptedPassword)
        );

        assertTrue(exception.getMessage().contains("Email"));
    }

    @DisplayName("User 생성 - 이메일 길이 초과로 실패")
    @Test
    void create_EmailTooLong_ThrowsException() {
        // given
        String username = "testuser";
        String email = "a".repeat(101); // MAX_EMAIL_LENGTH(100) 초과
        String encryptedPassword = "encrypted_password";

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, encryptedPassword)
        );

        assertTrue(exception.getMessage().contains("Email"));
    }

    @DisplayName("User 생성 - null 암호화된 비밀번호로 실패")
    @Test
    void create_NullEncryptedPassword_ThrowsException() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String encryptedPassword = null;

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, encryptedPassword)
        );

        assertTrue(exception.getMessage().contains("Encrypted Password"));
    }

    @DisplayName("User.of 생성자 - 성공")
    @Test
    void of_Success() {
        // given
        UserId userId = UserId.from(1L);
        String username = "testuser";
        String email = "test@example.com";
        String encryptedPassword = "encrypted_password";
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        boolean isDeleted = false;

        // when
        LocalDateTime lastActiveAt = LocalDateTime.now();
        User user = User.of(userId, username, email, encryptedPassword, createdAt, updatedAt, lastActiveAt, isDeleted);

        // then
        assertEquals(userId, user.getId());
        assertEquals(username.trim(), user.getUsername());
        assertEquals(email.trim(), user.getEmail());
        assertEquals(encryptedPassword, user.getPassword());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(updatedAt, user.getUpdatedAt());
        assertEquals(isDeleted, user.isDeleted());
    }

    @DisplayName("User.of 생성자 - null ID로 실패")
    @Test
    void of_NullId_ThrowsException() {
        // given
        UserId userId = null;
        String username = "testuser";
        String email = "test@example.com";
        String encryptedPassword = "encrypted_password";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        LocalDateTime lastActiveAt = LocalDateTime.now();
        boolean isDeleted = false;

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.of(userId, username, email, encryptedPassword, createdAt, updatedAt, lastActiveAt, isDeleted)
        );

        assertTrue(exception.getMessage().contains("UserId"));
    }

    @DisplayName("User.of 생성자 - null createdAt으로 실패")
    @Test
    void of_NullCreatedAt_ThrowsException() {
        // given
        UserId userId = UserId.from(1L);
        String username = "testuser";
        String email = "test@example.com";
        String encryptedPassword = "encrypted_password";
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = LocalDateTime.now();
        LocalDateTime lastActiveAt = LocalDateTime.now();
        boolean isDeleted = false;

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.of(userId, username, email, encryptedPassword, createdAt, updatedAt, lastActiveAt, isDeleted)
        );

        assertEquals("CreatedAt must not be null", exception.getMessage());
    }

    @DisplayName("이메일 변경 - 성공")
    @Test
    void withEmail_Success() {
        // given
        User originalUser = User.create("testuser", "old@example.com", "password");
        String newEmail = "new@example.com";

        // when
        User updatedUser = originalUser.withEmail(newEmail);

        // then
        assertNotSame(originalUser, updatedUser); // 불변 객체이므로 다른 인스턴스
        assertEquals(originalUser.getId(), updatedUser.getId());
        assertEquals(originalUser.getUsername(), updatedUser.getUsername());
        assertEquals(newEmail.trim(), updatedUser.getEmail());
        assertEquals(originalUser.getPassword(), updatedUser.getPassword());
        assertEquals(originalUser.getCreatedAt(), updatedUser.getCreatedAt());
        assertEquals(originalUser.isDeleted(), updatedUser.isDeleted());
        assertTrue(updatedUser.getUpdatedAt().isAfter(originalUser.getUpdatedAt()) ||
                  updatedUser.getUpdatedAt().isEqual(originalUser.getUpdatedAt()));
    }

    @DisplayName("이메일 변경 - null 이메일로 실패")
    @Test
    void withEmail_NullEmail_ThrowsException() {
        // given
        User user = User.create("testuser", "test@example.com", "password");
        String newEmail = null;

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> user.withEmail(newEmail)
        );

        assertTrue(exception.getMessage().contains("Email"));
    }

    @DisplayName("비밀번호 변경 - 성공")
    @Test
    void withPassword_Success() {
        // given
        User originalUser = User.create("testuser", "test@example.com", "old_password");
        String newPassword = "new_encrypted_password";

        // when
        User updatedUser = originalUser.withPassword(newPassword);

        // then
        assertNotSame(originalUser, updatedUser);
        assertEquals(originalUser.getId(), updatedUser.getId());
        assertEquals(originalUser.getUsername(), updatedUser.getUsername());
        assertEquals(originalUser.getEmail(), updatedUser.getEmail());
        assertEquals(newPassword, updatedUser.getPassword());
        assertEquals(originalUser.getCreatedAt(), updatedUser.getCreatedAt());
        assertEquals(originalUser.isDeleted(), updatedUser.isDeleted());
        assertTrue(updatedUser.getUpdatedAt().isAfter(originalUser.getUpdatedAt()) ||
                  updatedUser.getUpdatedAt().isEqual(originalUser.getUpdatedAt()));
    }

    @DisplayName("비밀번호 변경 - null 비밀번호로 실패")
    @Test
    void withPassword_NullPassword_ThrowsException() {
        // given
        User user = User.create("testuser", "test@example.com", "password");
        String newPassword = null;

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> user.withPassword(newPassword)
        );

        assertTrue(exception.getMessage().contains("Encrypted Password"));
    }

    @DisplayName("사용자 삭제 - 성공")
    @Test
    void withDeletedStatus_Success() {
        // given
        User originalUser = User.create("testuser", "test@example.com", "password");

        // when
        User deletedUser = originalUser.withDeletedStatus();

        // then
        assertNotSame(originalUser, deletedUser);
        assertEquals(originalUser.getId(), deletedUser.getId());
        assertEquals(originalUser.getUsername(), deletedUser.getUsername());
        assertEquals(originalUser.getEmail(), deletedUser.getEmail());
        assertEquals(originalUser.getPassword(), deletedUser.getPassword());
        assertEquals(originalUser.getCreatedAt(), deletedUser.getCreatedAt());
        assertTrue(deletedUser.isDeleted());
        assertTrue(deletedUser.getUpdatedAt().isAfter(originalUser.getUpdatedAt()) ||
                  deletedUser.getUpdatedAt().isEqual(originalUser.getUpdatedAt()));
    }

    @DisplayName("삭제 여부 확인 - 활성 사용자")
    @Test
    void isDeleted_ActiveUser_ReturnsFalse() {
        // given
        User user = User.create("testuser", "test@example.com", "password");

        // when & then
        assertFalse(user.isDeleted());
    }

    @DisplayName("삭제 여부 확인 - 삭제된 사용자")
    @Test
    void isDeleted_DeletedUser_ReturnsTrue() {
        // given
        User user = User.create("testuser", "test@example.com", "password");
        User deletedUser = user.withDeletedStatus();

        // when & then
        assertTrue(deletedUser.isDeleted());
    }

    @DisplayName("공백 제거 확인 - 사용자명과 이메일")
    @Test
    void create_TrimsWhitespace() {
        // given
        String username = "  testuser  ";
        String email = "  test@example.com  ";
        String encryptedPassword = "password";

        // when
        User user = User.create(username, email, encryptedPassword);

        // then
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
    }

    @DisplayName("equals 메서드 - 같은 ID")
    @Test
    void equals_SameId_ReturnsTrue() {
        // given
        UserId userId = UserId.from(1L);
        LocalDateTime now = LocalDateTime.now();
        User user1 = User.of(userId, "user1", "user1@test.com", "pass1", 
                           now, now, now, false);
        User user2 = User.of(userId, "user2", "user2@test.com", "pass2", 
                           now, now, now, false);

        // when & then
        assertEquals(user1, user2); // 같은 ID이면 equals
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @DisplayName("equals 메서드 - 다른 ID")
    @Test
    void equals_DifferentId_ReturnsFalse() {
        // given
        UserId userId1 = UserId.from(1L);
        UserId userId2 = UserId.from(2L);
        LocalDateTime now = LocalDateTime.now();
        User user1 = User.of(userId1, "user1", "user1@test.com", "pass1", 
                           now, now, now, false);
        User user2 = User.of(userId2, "user1", "user1@test.com", "pass1", 
                           now, now, now, false);

        // when & then
        assertNotEquals(user1, user2); // 다른 ID이면 not equals
    }

    @DisplayName("toString 메서드")
    @Test
    void toString_ContainsExpectedFields() {
        // given
        UserId userId = UserId.from(1L);
        User user = User.of(userId, "testuser", "test@example.com", "password", 
                          LocalDateTime.now(), LocalDateTime.now(), false);

        // when
        String result = user.toString();

        // then
        assertTrue(result.contains("User{"));
        assertTrue(result.contains("id=" + userId));
        assertTrue(result.contains("username='testuser'"));
        assertTrue(result.contains("email='test@example.com'"));
        assertTrue(result.contains("isDeleted=false"));
    }
}