package com.puppytalk.unit.user;

import com.puppytalk.user.*;
import com.puppytalk.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserDomainService 휴면 처리 테스트")
class UserDomainServiceDormantTest {

    private UserDomainService userDomainService;
    private MockUserRepository mockUserRepository;
    private MockPasswordEncoder mockPasswordEncoder;

    @BeforeEach
    void setUp() {
        mockUserRepository = new MockUserRepository();
        mockPasswordEncoder = new MockPasswordEncoder();
        userDomainService = new UserDomainService(mockUserRepository, mockPasswordEncoder);
    }

    @Test
    @DisplayName("휴면 처리 - 4주 이상 비활성 사용자 처리")
    void processDormantUsers_processes_inactive_users_successfully() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dormantDate = now.minusDays(User.DORMANT_DAYS + 1); // 29일 전
        
        UserId userId1 = UserId.from(1L);
        UserId userId2 = UserId.from(2L);
        UserId userId3 = UserId.from(3L);
        
        // 휴면 대상 사용자들 설정
        User dormantUser1 = User.of(userId1, "user1", "user1@test.com", "pass1", 
                                  now, now, dormantDate, false);
        User dormantUser2 = User.of(userId2, "user2", "user2@test.com", "pass2", 
                                  now, now, dormantDate, false);
        User deletedUser = User.of(userId3, "user3", "user3@test.com", "pass3", 
                                 now, now, dormantDate, true); // 삭제된 사용자
        
        // Mock 설정
        mockUserRepository.setInactiveUserIds(List.of(1L, 2L, 3L));
        mockUserRepository.addUser(userId1, dormantUser1);
        mockUserRepository.addUser(userId2, dormantUser2);
        mockUserRepository.addUser(userId3, deletedUser);

        // when
        int processedCount = userDomainService.processDormantUsers();

        // then
        assertEquals(2, processedCount); // 삭제된 사용자 제외하고 2명 처리
        assertTrue(mockUserRepository.isInactiveUsersCalled());
    }

    @Test
    @DisplayName("휴면 처리 - 4주 미만 비활성 사용자는 처리 제외")
    void processDormantUsers_excludes_users_not_meeting_dormant_criteria() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recentDate = now.minusDays(20); // 20일 전 (휴면 기준 미달)
        
        UserId userId1 = UserId.from(1L);
        User activeUser = User.of(userId1, "user1", "user1@test.com", "pass1", 
                                now, now, recentDate, false);
        
        // Mock 설정
        mockUserRepository.setInactiveUserIds(List.of(1L));
        mockUserRepository.addUser(userId1, activeUser);

        // when
        int processedCount = userDomainService.processDormantUsers();

        // then
        assertEquals(0, processedCount); // 휴면 기준에 미달하므로 0명 처리
    }

    @Test
    @DisplayName("휴면 처리 - 존재하지 않는 사용자는 스킵")
    void processDormantUsers_skips_non_existent_users() {
        // given
        // Mock 설정 - 존재하지 않는 사용자 ID 반환
        mockUserRepository.setInactiveUserIds(List.of(999L));
        // findById 호출 시 Optional.empty() 반환하도록 설정됨

        // when
        int processedCount = userDomainService.processDormantUsers();

        // then
        assertEquals(0, processedCount); // 존재하지 않는 사용자는 스킵
    }

    @Test
    @DisplayName("사용자 활성화 - 마지막 활동 시간 업데이트")
    void activateUser_updates_last_active_time() {
        // given
        UserId userId = UserId.from(1L);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oldDate = now.minusDays(30);
        
        User dormantUser = User.of(userId, "user1", "user1@test.com", "pass1", 
                                 now, now, oldDate, false);
        mockUserRepository.addUser(userId, dormantUser);

        // when
        userDomainService.activateUser(userId);

        // then
        assertTrue(mockUserRepository.isSaveCalled());
        User savedUser = mockUserRepository.getLastSavedUser();
        assertNotNull(savedUser);
        // lastActiveAt이 최근으로 업데이트되었는지 확인
        assertTrue(savedUser.getLastActiveAt().isAfter(oldDate));
    }

    @Test
    @DisplayName("사용자 활성화 - 존재하지 않는 사용자에 대해 예외 발생")
    void activateUser_throws_exception_for_non_existent_user() {
        // given
        UserId nonExistentUserId = UserId.from(999L);

        // when & then
        assertThrows(UserNotFoundException.class, () -> {
            userDomainService.activateUser(nonExistentUserId);
        });
    }

    /**
     * Mock UserRepository 구현체
     */
    private static class MockUserRepository implements UserRepository {
        private boolean inactiveUsersCalled = false;
        private boolean saveCalled = false;
        private List<Long> inactiveUserIds = List.of();
        private java.util.Map<UserId, User> users = new java.util.HashMap<>();
        private User lastSavedUser;

        public void setInactiveUserIds(List<Long> ids) {
            this.inactiveUserIds = ids;
        }

        public void addUser(UserId userId, User user) {
            this.users.put(userId, user);
        }

        public boolean isInactiveUsersCalled() {
            return inactiveUsersCalled;
        }

        public boolean isSaveCalled() {
            return saveCalled;
        }

        public User getLastSavedUser() {
            return lastSavedUser;
        }

        @Override
        public List<Long> findInactiveUsers(LocalDateTime cutoffTime) {
            this.inactiveUsersCalled = true;
            return inactiveUserIds;
        }

        @Override
        public Optional<User> findById(UserId userId) {
            return Optional.ofNullable(users.get(userId));
        }

        @Override
        public UserId save(User user) {
            this.saveCalled = true;
            this.lastSavedUser = user;
            return user.getId();
        }

        // 나머지 메서드들은 테스트에서 사용하지 않으므로 기본 구현
        @Override
        public Optional<User> findByUsername(String username) { return Optional.empty(); }

        @Override
        public Optional<User> findByEmail(String email) { return Optional.empty(); }

        @Override
        public List<User> findActiveUsers() { return List.of(); }

        @Override
        public List<User> findDeletedUsers() { return List.of(); }

        @Override
        public boolean existsByUsername(String username) { return false; }

        @Override
        public boolean existsByEmail(String email) { return false; }
    }

    /**
     * Mock PasswordEncoder 구현체
     */
    private static class MockPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(String rawPassword) {
            return "encoded_" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            return encodedPassword.equals("encoded_" + rawPassword);
        }
        
        @Override
        public String getAlgorithm() {
            return "MOCK";
        }
    }
}