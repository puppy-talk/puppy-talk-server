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
        
        // when
        User user = User.create(username, email);
        
        // then
        assertNotNull(user);
        assertNotNull(user.id());
        assertEquals(username, user.username());
        assertEquals(email.toLowerCase(), user.email());
        assertNotNull(user.createdAt());
        assertTrue(user.createdAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @DisplayName("사용자 생성 - null 사용자명으로 실패")
    @Test
    void create_NullUsername_ThrowsException() {
        // given
        String username = null;
        String email = "test@example.com";
        
        // when & then
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> User.create(username, email)
        );
        
        assertTrue(exception.getMessage().contains("username"));
    }
    
    @DisplayName("사용자 생성 - 빈 사용자명으로 실패")
    @Test
    void create_EmptyUsername_ThrowsException() {
        // given
        String username = "";
        String email = "test@example.com";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email)
        );
        
        assertEquals("사용자명은 필수입니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 공백만 있는 사용자명으로 실패")
    @Test
    void create_BlankUsername_ThrowsException() {
        // given
        String username = "   ";
        String email = "test@example.com";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email)
        );
        
        assertEquals("사용자명은 필수입니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 너무 짧은 사용자명으로 실패")
    @Test
    void create_TooShortUsername_ThrowsException() {
        // given
        String username = "ab"; // 2자 (최소 3자 필요)
        String email = "test@example.com";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email)
        );
        
        assertEquals("사용자명은 3-20자 사이여야 합니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 너무 긴 사용자명으로 실패")
    @Test
    void create_TooLongUsername_ThrowsException() {
        // given
        String username = "a".repeat(21); // 21자 (최대 20자)
        String email = "test@example.com";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email)
        );
        
        assertEquals("사용자명은 3-20자 사이여야 합니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - null 이메일로 실패")
    @Test
    void create_NullEmail_ThrowsException() {
        // given
        String username = "testuser";
        String email = null;
        
        // when & then
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> User.create(username, email)
        );
        
        assertTrue(exception.getMessage().contains("email"));
    }
    
    @DisplayName("사용자 생성 - 빈 이메일로 실패")
    @Test
    void create_EmptyEmail_ThrowsException() {
        // given
        String username = "testuser";
        String email = "";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email)
        );
        
        assertEquals("이메일은 필수입니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 잘못된 이메일 형식으로 실패")
    @Test
    void create_InvalidEmailFormat_ThrowsException() {
        // given
        String username = "testuser";
        String email = "invalid-email";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email)
        );
        
        assertEquals("올바른 이메일 형식이 아닙니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 너무 긴 이메일로 실패")
    @Test
    void create_TooLongEmail_ThrowsException() {
        // given
        String username = "testuser";
        String email = "a".repeat(90) + "@example.com"; // 100자 초과
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.create(username, email)
        );
        
        assertEquals("올바른 이메일 형식이 아닙니다", exception.getMessage());
    }
    
    @DisplayName("사용자 생성 - 이메일 자동 소문자 변환")
    @Test
    void create_EmailToLowerCase() {
        // given
        String username = "testuser";
        String email = "Test@EXAMPLE.COM";
        
        // when
        User user = User.create(username, email);
        
        // then
        assertEquals("test@example.com", user.email());
    }
    
    @DisplayName("사용자 생성 - 사용자명 공백 제거")
    @Test
    void create_UsernameTrimsWhitespace() {
        // given
        String username = "  testuser  ";
        String email = "test@example.com";
        
        // when
        User user = User.create(username, email);
        
        // then
        assertEquals("testuser", user.username());
    }
    
    @DisplayName("저장된 사용자 객체 생성 - 성공")
    @Test
    void of_Success() {
        // given
        UserId id = UserId.of(1L);
        String username = "testuser";
        String email = "test@example.com";
        LocalDateTime createdAt = LocalDateTime.now();
        boolean isDeleted = false;
        
        // when
        User user = User.of(id, username, email, createdAt, isDeleted);
        
        // then
        assertNotNull(user);
        assertEquals(id, user.id());
        assertEquals(username, user.username());
        assertEquals(email, user.email());
        assertEquals(createdAt, user.createdAt());
    }
    
    @DisplayName("저장된 사용자 객체 생성 - null ID로 실패")
    @Test
    void of_NullId_ThrowsException() {
        // given
        UserId id = null;
        String username = "testuser";
        String email = "test@example.com";
        LocalDateTime createdAt = LocalDateTime.now();
        boolean isDeleted = false;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.of(id, username, email, createdAt, isDeleted)
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
        LocalDateTime createdAt = LocalDateTime.now();
        boolean isDeleted = false;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.of(id, username, email, createdAt, isDeleted)
        );
        
        assertEquals("저장된 사용자 ID가 필요합니다", exception.getMessage());
    }
    
    @DisplayName("사용자 삭제 - 성공")
    @Test
    void withDeletedStatus_Success() {
        // given
        User user = User.create("testuser", "test@example.com");
        
        // when
        User deletedUser = user.withDeletedStatus();
        
        // then
        assertNotNull(deletedUser);
        assertEquals(user.id(), deletedUser.id());
        assertEquals(user.username(), deletedUser.username());
        assertEquals(user.email(), deletedUser.email());
        assertEquals(user.createdAt(), deletedUser.createdAt());
        // isDeleted는 private이므로 직접 검증할 수 없음
    }
    
    @DisplayName("사용자 복구 - 성공")
    @Test
    void withRestoredStatus_Success() {
        // given
        User user = User.create("testuser", "test@example.com");
        User deletedUser = user.withDeletedStatus();
        
        // when
        User restoredUser = deletedUser.withRestoredStatus();
        
        // then
        assertNotNull(restoredUser);
        assertEquals(user.id(), restoredUser.id());
        assertEquals(user.username(), restoredUser.username());
        assertEquals(user.email(), restoredUser.email());
        assertEquals(user.createdAt(), restoredUser.createdAt());
    }
    
    @DisplayName("equals 메서드 - 동일한 ID면 같은 객체")
    @Test
    void equals_SameId_ReturnsTrue() {
        // given
        UserId id = UserId.of(1L);
        User user1 = User.of(id, "user1", "user1@example.com", LocalDateTime.now(), false);
        User user2 = User.of(id, "user2", "user2@example.com", LocalDateTime.now(), false);
        
        // when & then
        assertEquals(user1, user2);
    }
    
    @DisplayName("equals 메서드 - 다른 ID면 다른 객체")
    @Test
    void equals_DifferentId_ReturnsFalse() {
        // given
        User user1 = User.of(UserId.of(1L), "user1", "user1@example.com", LocalDateTime.now(), false);
        User user2 = User.of(UserId.of(2L), "user2", "user2@example.com", LocalDateTime.now(), false);
        
        // when & then
        assertNotEquals(user1, user2);
    }
    
    @DisplayName("hashCode 메서드 - 동일한 ID면 같은 해시코드")
    @Test
    void hashCode_SameId_ReturnsSameHashCode() {
        // given
        UserId id = UserId.of(1L);
        User user1 = User.of(id, "user1", "user1@example.com", LocalDateTime.now(), false);
        User user2 = User.of(id, "user2", "user2@example.com", LocalDateTime.now(), false);
        
        // when & then
        assertEquals(user1.hashCode(), user2.hashCode());
    }
}