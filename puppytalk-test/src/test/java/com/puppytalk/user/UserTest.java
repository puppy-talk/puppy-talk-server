package com.puppytalk.user;

import com.puppytalk.support.validation.Preconditions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User 도메인 모델 테스트")
class UserTest {

    @Nested
    @DisplayName("사용자 생성 테스트")
    class CreateUserTest {

        @Test
        @DisplayName("유효한 정보로 사용자 생성에 성공한다")
        void createUser_WithValidInfo_Success() {
            // given
            String username = "testuser";
            String email = "test@example.com";
            String password = "encrypted_password_123";

            // when
            User user = User.create(username, email, password);

            // then
            assertThat(user.getUsername()).isEqualTo(username);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getId()).isNull(); // 새로 생성된 사용자는 ID가 null
            assertThat(user.isDeleted()).isFalse();
            assertThat(user.getCreatedAt()).isNotNull();
            assertThat(user.getUpdatedAt()).isNotNull();
            assertThat(user.getLastActiveAt()).isNotNull();
        }

        @Test
        @DisplayName("username에 공백이 있으면 trim된다")
        void createUser_WithWhitespaceUsername_TrimmedSuccess() {
            // given
            String username = "  testuser  ";
            String email = "test@example.com";
            String password = "encrypted_password_123";

            // when
            User user = User.create(username, email, password);

            // then
            assertThat(user.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("email에 공백이 있으면 trim된다")
        void createUser_WithWhitespaceEmail_TrimmedSuccess() {
            // given
            String username = "testuser";
            String email = "  test@example.com  ";
            String password = "encrypted_password_123";

            // when
            User user = User.create(username, email, password);

            // then
            assertThat(user.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("username이 null이면 예외가 발생한다")
        void createUser_WithNullUsername_ThrowsException() {
            // given
            String email = "test@example.com";
            String password = "encrypted_password_123";

            // when & then
            assertThatThrownBy(() -> User.create(null, email, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
        }

        @Test
        @DisplayName("username이 빈 문자열이면 예외가 발생한다")
        void createUser_WithEmptyUsername_ThrowsException() {
            // given
            String email = "test@example.com";
            String password = "encrypted_password_123";

            // when & then
            assertThatThrownBy(() -> User.create("", email, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
        }

        @Test
        @DisplayName("username이 최대 길이를 초과하면 예외가 발생한다")
        void createUser_WithTooLongUsername_ThrowsException() {
            // given
            String longUsername = "a".repeat(User.MAX_USERNAME_LENGTH + 1);
            String email = "test@example.com";
            String password = "encrypted_password_123";

            // when & then
            assertThatThrownBy(() -> User.create(longUsername, email, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
        }

        @Test
        @DisplayName("email이 null이면 예외가 발생한다")
        void createUser_WithNullEmail_ThrowsException() {
            // given
            String username = "testuser";
            String password = "encrypted_password_123";

            // when & then
            assertThatThrownBy(() -> User.create(username, null, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
        }

        @Test
        @DisplayName("password가 null이면 예외가 발생한다")
        void createUser_WithNullPassword_ThrowsException() {
            // given
            String username = "testuser";
            String email = "test@example.com";

            // when & then
            assertThatThrownBy(() -> User.create(username, email, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Encrypted Password");
        }
    }

    @Nested
    @DisplayName("기존 사용자 데이터로부터 객체 생성 테스트")
    class OfUserTest {

        @Test
        @DisplayName("유효한 데이터로 사용자 객체 생성에 성공한다")
        void ofUser_WithValidData_Success() {
            // given
            UserId userId = UserId.from(1L);
            String username = "testuser";
            String email = "test@example.com";
            String password = "encrypted_password_123";
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
            LocalDateTime updatedAt = LocalDateTime.now();
            LocalDateTime lastActiveAt = LocalDateTime.now().minusHours(1);

            // when
            User user = User.of(userId, username, email, password, createdAt, updatedAt, lastActiveAt, false);

            // then
            assertThat(user.getId()).isEqualTo(userId);
            assertThat(user.getUsername()).isEqualTo(username);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getCreatedAt()).isEqualTo(createdAt);
            assertThat(user.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(user.getLastActiveAt()).isEqualTo(lastActiveAt);
            assertThat(user.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("userId가 null이면 예외가 발생한다")
        void ofUser_WithNullUserId_ThrowsException() {
            // given
            String username = "testuser";
            String email = "test@example.com";
            String password = "encrypted_password_123";
            LocalDateTime createdAt = LocalDateTime.now();

            // when & then
            assertThatThrownBy(() -> User.of(null, username, email, password, createdAt, null, null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId");
        }

        @Test
        @DisplayName("createdAt이 null이면 예외가 발생한다")
        void ofUser_WithNullCreatedAt_ThrowsException() {
            // given
            UserId userId = UserId.from(1L);
            String username = "testuser";
            String email = "test@example.com";
            String password = "encrypted_password_123";

            // when & then
            assertThatThrownBy(() -> User.of(userId, username, email, password, null, null, null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreatedAt must not be null");
        }
    }

    @Nested
    @DisplayName("사용자 정보 업데이트 테스트")
    class UpdateUserTest {

        private User createTestUser() {
            UserId userId = UserId.from(1L);
            LocalDateTime baseTime = LocalDateTime.now().minusDays(1);
            return User.of(userId, "testuser", "test@example.com", "password", 
                baseTime, baseTime, baseTime, false);
        }

        @Test
        @DisplayName("이메일 변경에 성공한다")
        void withEmail_WithValidEmail_Success() {
            // given
            User originalUser = createTestUser();
            String newEmail = "newemail@example.com";

            // when
            User updatedUser = originalUser.withEmail(newEmail);

            // then
            assertThat(updatedUser.getEmail()).isEqualTo(newEmail);
            assertThat(updatedUser.getUpdatedAt()).isAfter(originalUser.getUpdatedAt());
            // 다른 필드들은 변경되지 않음
            assertThat(updatedUser.getId()).isEqualTo(originalUser.getId());
            assertThat(updatedUser.getUsername()).isEqualTo(originalUser.getUsername());
            assertThat(updatedUser.getPassword()).isEqualTo(originalUser.getPassword());
            assertThat(updatedUser.getCreatedAt()).isEqualTo(originalUser.getCreatedAt());
        }

        @Test
        @DisplayName("비밀번호 변경에 성공한다")
        void withPassword_WithValidPassword_Success() {
            // given
            User originalUser = createTestUser();
            String newPassword = "new_encrypted_password_456";

            // when
            User updatedUser = originalUser.withPassword(newPassword);

            // then
            assertThat(updatedUser.getPassword()).isEqualTo(newPassword);
            assertThat(updatedUser.getUpdatedAt()).isAfter(originalUser.getUpdatedAt());
        }

        @Test
        @DisplayName("사용자 삭제 상태로 변경에 성공한다")
        void withDeletedStatus_Success() {
            // given
            User originalUser = createTestUser();

            // when
            User deletedUser = originalUser.withDeletedStatus();

            // then
            assertThat(deletedUser.isDeleted()).isTrue();
            assertThat(deletedUser.getUpdatedAt()).isAfter(originalUser.getUpdatedAt());
        }

        @Test
        @DisplayName("마지막 활동 시간 업데이트에 성공한다")
        void updateLastActiveTime_Success() {
            // given
            User originalUser = createTestUser();

            // when
            User updatedUser = originalUser.updateLastActiveTime();

            // then
            assertThat(updatedUser.getLastActiveAt()).isAfter(originalUser.getLastActiveAt());
            assertThat(updatedUser.getUpdatedAt()).isAfter(originalUser.getUpdatedAt());
        }

        @Test
        @DisplayName("잘못된 이메일로 변경 시 예외가 발생한다")
        void withEmail_WithInvalidEmail_ThrowsException() {
            // given
            User user = createTestUser();

            // when & then
            assertThatThrownBy(() -> user.withEmail(null))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> user.withEmail(""))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("동등성 및 해시코드 테스트")
    class EqualsAndHashCodeTest {

        @Test
        @DisplayName("같은 ID를 가진 사용자는 동등하다")
        void equals_WithSameId_ReturnsTrue() {
            // given
            UserId userId = UserId.from(1L);
            LocalDateTime now = LocalDateTime.now();
            
            User user1 = User.of(userId, "user1", "user1@example.com", "password1", now, now, now, false);
            User user2 = User.of(userId, "user2", "user2@example.com", "password2", now, now, now, false);

            // when & then
            assertThat(user1).isEqualTo(user2);
            assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 사용자는 동등하지 않다")
        void equals_WithDifferentId_ReturnsFalse() {
            // given
            LocalDateTime now = LocalDateTime.now();
            
            User user1 = User.of(UserId.from(1L), "user", "user@example.com", "password", now, now, now, false);
            User user2 = User.of(UserId.from(2L), "user", "user@example.com", "password", now, now, now, false);

            // when & then
            assertThat(user1).isNotEqualTo(user2);
        }

        @Test
        @DisplayName("ID가 null인 사용자끼리는 참조 동등성으로 비교된다")
        void equals_WithNullId_ReturnsReferenceEquality() {
            // given
            User user1 = User.create("user1", "user1@example.com", "password1");
            User user2 = User.create("user2", "user2@example.com", "password2");

            // when & then
            assertThat(user1).isNotEqualTo(user2); // 다른 객체 참조
            assertThat(user1).isEqualTo(user1); // 같은 객체 참조
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString에 중요 필드들이 포함된다")
        void toString_ContainsImportantFields() {
            // given
            UserId userId = UserId.from(1L);
            LocalDateTime now = LocalDateTime.now();
            User user = User.of(userId, "testuser", "test@example.com", "password", now, now, now, false);

            // when
            String result = user.toString();

            // then
            assertThat(result)
                .contains("User{")
                .contains("id=" + userId)
                .contains("username='testuser'")
                .contains("email='test@example.com'")
                .contains("isDeleted=false");
            
            // 비밀번호는 포함되지 않아야 함
            assertThat(result).doesNotContain("password");
        }
    }
}