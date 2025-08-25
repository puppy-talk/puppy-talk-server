package com.puppytalk.unit.user;

import com.puppytalk.user.User;
import com.puppytalk.user.UserId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User 도메인 엔티티 테스트")
class UserTest {
    
    @DisplayName("사용자 생성 - 성공")
    @Test
    void create_Success() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        
        // when
        User user = User.create(username, email, password);
        
        // then
        assertNotNull(user);
        assertNotNull(user.id());
        assertEquals(username, user.username());
        assertEquals(email.toLowerCase(), user.email());
        assertEquals(password.trim(), user.password());
        assertNotNull(user.createdAt());
        assertTrue(user.createdAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @DisplayName("사용자 생성 - null 사용자명으로 실패")
    @Test
    void create_NullUsername_ThrowsException() {
        // given
        String username = null;
        String email = "test@example.com";
        String password = "password123";
        
        // when & then
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> User.create(username, email, password)
        );
        
        assertTrue(exception.getMessage().contains("username"));
    }
    
    @DisplayName("사용자 생성 - 빈 사용자명으로 실패")
    @Test
    void create_EmptyUsername_ThrowsException() {
        // given
        String username = "";
        String email = "test@example.com";
        String password = "password123";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, password)
        );
        
        assertEquals("사용자명은 필수입니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 공백만 있는 사용자명으로 실패")
    @Test
    void create_BlankUsername_ThrowsException() {
        // given
        String username = "   ";
        String email = "test@example.com";
        String password = "password123";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, password)
        );
        
        assertEquals("사용자명은 필수입니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 너무 짧은 사용자명으로 실패")
    @Test
    void create_TooShortUsername_ThrowsException() {
        // given
        String username = "ab"; // 2자 (최소 3자 필요)
        String email = "test@example.com";
        String password = "password123";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, password)
        );
        
        assertEquals("사용자명은 3-20자 사이여야 합니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 너무 긴 사용자명으로 실패")
    @Test
    void create_TooLongUsername_ThrowsException() {
        // given
        String username = "a".repeat(21); // 21자 (최대 20자)
        String email = "test@example.com";
        String password = "password123";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, password)
        );
        
        assertEquals("사용자명은 3-20자 사이여야 합니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - null 이메일로 실패")
    @Test
    void create_NullEmail_ThrowsException() {
        // given
        String username = "testuser";
        String email = null;
        String password = "password123";
        
        // when & then
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> User.create(username, email, password)
        );
        
        assertTrue(exception.getMessage().contains("email"));
    }
    
    @DisplayName("사용자 생성 - 빈 이메일로 실패")
    @Test
    void create_EmptyEmail_ThrowsException() {
        // given
        String username = "testuser";
        String email = "";
        String password = "password123";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, password)
        );
        
        assertEquals("이메일은 필수입니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 잘못된 이메일 형식으로 실패")
    @Test
    void create_InvalidEmailFormat_ThrowsException() {
        // given
        String username = "testuser";
        String email = "invalid-email";
        String password = "password123";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, password)
        );
        
        assertEquals("올바른 이메일 형식이 아닙니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 너무 긴 이메일로 실패")
    @Test
    void create_TooLongEmail_ThrowsException() {
        // given
        String username = "testuser";
        String email = "a".repeat(90) + "@example.com"; // 100자 초과
        String password = "password123";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, password)
        );
        
        assertEquals("올바른 이메일 형식이 아닙니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 이메일 자동 소문자 변환")
    @Test
    void create_EmailToLowerCase() {
        // given
        String username = "testuser";
        String email = "Test@EXAMPLE.COM";
        String password = "password123";
        
        // when
        User user = User.create(username, email, password);
        
        // then
        assertEquals("test@example.com", user.email());
    }
    
    @DisplayName("사용자 생성 - 사용자명 공백 제거")
    @Test
    void create_UsernameTrimsWhitespace() {
        // given
        String username = "  testuser  ";
        String email = "test@example.com";
        String password = "password123";
        
        // when
        User user = User.create(username, email, password);
        
        // then
        assertEquals("testuser", user.username());
    }
    
    @DisplayName("사용자 생성 - null 비밀번호로 실패")
    @Test
    void create_NullPassword_ThrowsException() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, password)
        );
        
        assertEquals("비밀번호는 필수입니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 빈 비밀번호로 실패")
    @Test
    void create_EmptyPassword_ThrowsException() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = "";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, password)
        );
        
        assertEquals("비밀번호는 필수입니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 공백만 있는 비밀번호로 실패")
    @Test
    void create_BlankPassword_ThrowsException() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = "   ";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, password)
        );
        
        assertEquals("비밀번호는 필수입니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 너무 짧은 비밀번호로 실패")
    @Test
    void create_TooShortPassword_ThrowsException() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = "1234567"; // 7자 (최소 8자 필요)
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, password)
        );
        
        assertEquals("비밀번호는 8-100자 사이여야 합니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 너무 긴 비밀번호로 실패")
    @Test
    void create_TooLongPassword_ThrowsException() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = "a".repeat(101); // 101자 (최대 100자)
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email, password)
        );
        
        assertEquals("비밀번호는 8-100자 사이여야 합니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 비밀번호 공백 제거")
    @Test
    void create_PasswordTrimsWhitespace() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = "  password123  ";
        
        // when
        User user = User.create(username, email, password);
        
        // then
        assertEquals("password123", user.password());
    }
    
    @DisplayName("비밀번호 검증 - 일치하는 경우")
    @Test
    void isPasswordMatch_CorrectPassword_ReturnsTrue() {
        // given
        User user = User.create("testuser", "test@example.com", "password123");
        
        // when & then
        assertTrue(user.isPasswordMatch("password123"));
    }
    
    @DisplayName("비밀번호 검증 - 일치하지 않는 경우")
    @Test
    void isPasswordMatch_WrongPassword_ReturnsFalse() {
        // given
        User user = User.create("testuser", "test@example.com", "password123");
        
        // when & then
        assertFalse(user.isPasswordMatch("wrongpassword"));
    }
    
    @DisplayName("비밀번호 검증 - null 비밀번호")
    @Test
    void isPasswordMatch_NullPassword_ReturnsFalse() {
        // given
        User user = User.create("testuser", "test@example.com", "password123");
        
        // when & then
        assertFalse(user.isPasswordMatch(null));
    }
    
    @DisplayName("비밀번호 변경 - 성공")
    @Test
    void withPassword_Success() {
        // given
        User user = User.create("testuser", "test@example.com", "password123");
        String newPassword = "newpassword456";
        
        // when
        User updatedUser = user.withPassword(newPassword);
        
        // then
        assertNotNull(updatedUser);
        assertEquals(user.id(), updatedUser.id());
        assertEquals(user.username(), updatedUser.username());
        assertEquals(user.email(), updatedUser.email());
        assertEquals(newPassword.trim(), updatedUser.password());
        assertEquals(user.createdAt(), updatedUser.createdAt());
    }
    
    @DisplayName("저장된 사용자 객체 생성 - 성공")
    @Test
    void of_Success() {
        // given
        UserId id = UserId.of(1L);
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        LocalDateTime createdAt = LocalDateTime.now();
        boolean isDeleted = false;
        
        // when
        User user = User.of(id, username, email, password, createdAt, isDeleted);
        
        // then
        assertNotNull(user);
        assertEquals(id, user.id());
        assertEquals(username, user.username());
        assertEquals(email, user.email());
        assertEquals(password, user.password());
        assertEquals(createdAt, user.createdAt());
    }
    
    @DisplayName("저장된 사용자 객체 생성 - null ID로 실패")
    @Test
    void of_NullId_ThrowsException() {
        // given
        UserId id = null;
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        LocalDateTime createdAt = LocalDateTime.now();
        boolean isDeleted = false;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.of(id, username, email, password, createdAt, isDeleted)
        );
        
        assertEquals("저장된 사용자 ID가 필요합니다", exception.getMessage());
    }
    
    @DisplayName("저장된 사용자 객체 생성 - 저장되지 않은 ID로 실패")
    @Test
    void of_NotStoredId_ThrowsException() {
        // given
        UserId id = UserId.create(); // 저장되지 않은 ID
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        LocalDateTime createdAt = LocalDateTime.now();
        boolean isDeleted = false;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.of(id, username, email, password, createdAt, isDeleted)
        );
        
        assertEquals("저장된 사용자 ID가 필요합니다", exception.getMessage());
    }
    
    @DisplayName("사용자 삭제 - 성공")
    @Test
    void withDeletedStatus_Success() {
        // given
        User user = User.create("testuser", "test@example.com", "password123");
        
        // when
        User deletedUser = user.withDeletedStatus();
        
        // then
        assertNotNull(deletedUser);
        assertEquals(user.id(), deletedUser.id());
        assertEquals(user.username(), deletedUser.username());
        assertEquals(user.email(), deletedUser.email());
        assertEquals(user.password(), deletedUser.password());
        assertEquals(user.createdAt(), deletedUser.createdAt());
        // isDeleted는 private이므로 직접 검증할 수 없음
    }
    
    @DisplayName("사용자 복구 - 성공")
    @Test
    void withRestoredStatus_Success() {
        // given
        User user = User.create("testuser", "test@example.com", "password123");
        User deletedUser = user.withDeletedStatus();
        
        // when
        User restoredUser = deletedUser.withRestoredStatus();
        
        // then
        assertNotNull(restoredUser);
        assertEquals(user.id(), restoredUser.id());
        assertEquals(user.username(), restoredUser.username());
        assertEquals(user.email(), restoredUser.email());
        assertEquals(user.password(), restoredUser.password());
        assertEquals(user.createdAt(), restoredUser.createdAt());
    }
    
    @DisplayName("equals 메서드 - 동일한 ID면 같은 객체")
    @Test
    void equals_SameId_ReturnsTrue() {
        // given
        UserId id = UserId.of(1L);
        User user1 = User.of(id, "user1", "user1@example.com", "password1", LocalDateTime.now(), false);
        User user2 = User.of(id, "user2", "user2@example.com", "password2", LocalDateTime.now(), false);
        
        // when & then
        assertEquals(user1, user2);
    }
    
    @DisplayName("equals 메서드 - 다른 ID면 다른 객체")
    @Test
    void equals_DifferentId_ReturnsFalse() {
        // given
        User user1 = User.of(UserId.of(1L), "user1", "user1@example.com", "password1", LocalDateTime.now(), false);
        User user2 = User.of(UserId.of(2L), "user2", "user2@example.com", "password2", LocalDateTime.now(), false);
        
        // when & then
        assertNotEquals(user1, user2);
    }
    
    @DisplayName("hashCode 메서드 - 동일한 ID면 같은 해시코드")
    @Test
    void hashCode_SameId_ReturnsSameHashCode() {
        // given
        UserId id = UserId.of(1L);
        User user1 = User.of(id, "user1", "user1@example.com", "password1", LocalDateTime.now(), false);
        User user2 = User.of(id, "user2", "user2@example.com", "password2", LocalDateTime.now(), false);
        
        // when & then
        assertEquals(user1.hashCode(), user2.hashCode());
    }
}