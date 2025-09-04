package com.puppytalk.user;

import com.puppytalk.user.dto.request.UserCreateCommand;
import com.puppytalk.user.dto.request.UserGetQuery;
import com.puppytalk.user.dto.response.UserCreateResult;
import com.puppytalk.user.dto.response.UserResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserFacade {
    
    private final UserDomainService userDomainService;
    
    public UserFacade(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }
    
    /**
     * 새로운 사용자를 등록한다.
     */
    @Transactional
    public UserCreateResult createUser(UserCreateCommand command) {
        Assert.notNull(command, "UserCreateCommand must not be null");
        Assert.hasText(command.username(), "Username must not be null or empty");
        Assert.hasText(command.email(), "Email must not be null or empty");
        Assert.hasText(command.password(), "Password must not be null or empty");
        
        UserId userId = userDomainService.registerUser(
            command.username(), 
            command.email(),
            command.password()
        );
        
        User createdUser = userDomainService.getUserById(userId);
        return UserCreateResult.from(createdUser);
    }
    
    /**
     * 사용자 ID로 사용자를 조회한다.
     */
    public UserResult getUser(UserGetQuery query) {
        Assert.notNull(query, "UserGetQuery must not be null");
        Assert.notNull(query.userId(), "UserId must not be null");
        
        UserId userId = UserId.from(query.userId());
        User user = userDomainService.getUserById(userId);
        
        return UserResult.from(user);
    }
    
    /**
     * 알림 수신 가능한 사용자 ID 목록을 조회한다.
     * 휴면 계정 및 삭제된 계정은 제외된다.
     */
    public List<UserId> findNotificationEligibleUsers(List<UserId> candidateUserIds) {
        Assert.notNull(candidateUserIds, "Candidate user IDs must not be null");
        
        if (candidateUserIds.isEmpty()) {
            return List.of();
        }
        
        return candidateUserIds.stream()
            .map(userDomainService::getUserById)
            .filter(User::canReceiveNotifications)
            .map(User::getId)
            .toList();
    }
    
    /**
     * 특정 시간 이전에 활동한 비활성 사용자 ID 목록을 조회한다.
     */
    public List<UserId> findInactiveUserIds(LocalDateTime cutoffTime) {
        Assert.notNull(cutoffTime, "Cutoff time must not be null");
        
        return userDomainService.findInactiveUsers(cutoffTime);
    }
    
    /**
     * 휴면 사용자 배치 처리를 수행한다.
     * 
     * @return 처리된 휴면 사용자 수
     */
    @Transactional
    public int processDormantUsers() {
        return userDomainService.processDormantUsers();
    }
    
    /**
     * 사용자를 활성 상태로 전환한다 (로그인 시 호출).
     * 
     * @param userId 사용자 ID
     */
    @Transactional
    public void activateUser(Long userId) {
        Assert.notNull(userId, "User ID must not be null");
        
        UserId userIdObj = UserId.from(userId);
        userDomainService.activateUser(userIdObj);
    }
}