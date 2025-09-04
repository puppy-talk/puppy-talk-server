package com.puppytalk.unit.user;

import com.puppytalk.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserFacade 휴면 처리 테스트")
class UserFacadeDormantTest {

    private UserFacade userFacade;
    private MockUserDomainService mockUserDomainService;

    @BeforeEach
    void setUp() {
        mockUserDomainService = new MockUserDomainService();
        userFacade = new UserFacade(mockUserDomainService);
    }

    @Test
    @DisplayName("휴면 사용자 배치 처리 - 성공적으로 처리됨")
    void processDormantUsers_processes_successfully() {
        // given
        mockUserDomainService.setProcessDormantUsersResult(5);

        // when
        int processedCount = userFacade.processDormantUsers();

        // then
        assertEquals(5, processedCount);
        assertTrue(mockUserDomainService.isProcessDormantUsersCalled());
    }

    @Test
    @DisplayName("사용자 활성화 - 정상적으로 활성화됨")
    void activateUser_activates_successfully() {
        // given
        Long userId = 1L;

        // when
        userFacade.activateUser(userId);

        // then
        assertTrue(mockUserDomainService.isActivateUserCalled());
        assertEquals(UserId.from(userId), mockUserDomainService.getLastActivatedUserId());
    }

    @Test
    @DisplayName("사용자 활성화 - null userId로 예외 발생")
    void activateUser_throws_exception_for_null_user_id() {
        // given
        Long userId = null;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            userFacade.activateUser(userId);
        });
    }

    @Test
    @DisplayName("알림 수신 가능한 사용자 필터링 - 휴면 사용자 제외")
    void findNotificationEligibleUsers_filters_dormant_users() {
        // given
        UserId activeUserId = UserId.from(1L);
        UserId dormantUserId = UserId.from(2L);
        UserId deletedUserId = UserId.from(3L);
        
        List<UserId> candidateUsers = List.of(activeUserId, dormantUserId, deletedUserId);
        
        // 활성 사용자만 알림 수신 가능하도록 Mock 설정
        LocalDateTime now = LocalDateTime.now();
        User activeUser = User.of(activeUserId, "active", "active@test.com", "pass", 
                                now, now, now, false);
        User dormantUser = User.of(dormantUserId, "dormant", "dormant@test.com", "pass", 
                                 now, now, now.minusDays(30), false);
        User deletedUser = User.of(deletedUserId, "deleted", "deleted@test.com", "pass", 
                                 now, now, now, true);
        
        mockUserDomainService.addUser(activeUserId, activeUser);
        mockUserDomainService.addUser(dormantUserId, dormantUser);
        mockUserDomainService.addUser(deletedUserId, deletedUser);

        // when
        List<UserId> eligibleUsers = userFacade.findNotificationEligibleUsers(candidateUsers);

        // then
        assertEquals(1, eligibleUsers.size());
        assertEquals(activeUserId, eligibleUsers.get(0));
    }

    @Test
    @DisplayName("알림 수신 가능한 사용자 필터링 - 빈 목록 처리")
    void findNotificationEligibleUsers_handles_empty_list() {
        // given
        List<UserId> candidateUsers = List.of();

        // when
        List<UserId> eligibleUsers = userFacade.findNotificationEligibleUsers(candidateUsers);

        // then
        assertTrue(eligibleUsers.isEmpty());
    }

    @Test
    @DisplayName("알림 수신 가능한 사용자 필터링 - null 목록으로 예외 발생")
    void findNotificationEligibleUsers_throws_exception_for_null_list() {
        // given
        List<UserId> candidateUsers = null;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            userFacade.findNotificationEligibleUsers(candidateUsers);
        });
    }

    @Test
    @DisplayName("비활성 사용자 ID 조회 - 정상 처리")
    void findInactiveUserIds_returns_inactive_users() {
        // given
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(2);
        List<UserId> inactiveUsers = List.of(UserId.from(1L), UserId.from(2L));
        mockUserDomainService.setFindInactiveUsersResult(inactiveUsers);

        // when
        List<UserId> result = userFacade.findInactiveUserIds(cutoffTime);

        // then
        assertEquals(2, result.size());
        assertEquals(inactiveUsers, result);
        assertTrue(mockUserDomainService.isFindInactiveUsersCalled());
    }

    @Test
    @DisplayName("비활성 사용자 ID 조회 - null cutoffTime으로 예외 발생")
    void findInactiveUserIds_throws_exception_for_null_cutoff_time() {
        // given
        LocalDateTime cutoffTime = null;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            userFacade.findInactiveUserIds(cutoffTime);
        });
    }

    /**
     * Mock UserDomainService 구현체
     */
    private static class MockUserDomainService extends UserDomainService {
        private boolean processDormantUsersCalled = false;
        private boolean activateUserCalled = false;
        private boolean findInactiveUsersCalled = false;
        
        private int processDormantUsersResult = 0;
        private UserId lastActivatedUserId;
        private List<UserId> findInactiveUsersResult = List.of();
        private java.util.Map<UserId, User> users = new java.util.HashMap<>();

        public MockUserDomainService() {
            super(null, null); // Mock이므로 null로 초기화
        }

        public void setProcessDormantUsersResult(int result) {
            this.processDormantUsersResult = result;
        }

        public void setFindInactiveUsersResult(List<UserId> result) {
            this.findInactiveUsersResult = result;
        }

        public void addUser(UserId userId, User user) {
            this.users.put(userId, user);
        }

        public boolean isProcessDormantUsersCalled() {
            return processDormantUsersCalled;
        }

        public boolean isActivateUserCalled() {
            return activateUserCalled;
        }

        public boolean isFindInactiveUsersCalled() {
            return findInactiveUsersCalled;
        }

        public UserId getLastActivatedUserId() {
            return lastActivatedUserId;
        }

        @Override
        public int processDormantUsers() {
            this.processDormantUsersCalled = true;
            return processDormantUsersResult;
        }

        @Override
        public void activateUser(UserId userId) {
            this.activateUserCalled = true;
            this.lastActivatedUserId = userId;
        }

        @Override
        public List<UserId> findInactiveUsers(LocalDateTime cutoffTime) {
            this.findInactiveUsersCalled = true;
            return findInactiveUsersResult;
        }

        @Override
        public User getUserById(UserId userId) {
            User user = users.get(userId);
            if (user == null) {
                throw UserNotFoundException.byId(userId);
            }
            return user;
        }
    }
}