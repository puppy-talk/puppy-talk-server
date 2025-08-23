package com.puppytalk.user;

import com.puppytalk.user.dto.request.UserCreateCommand;
import com.puppytalk.user.dto.request.UserGetQuery;
import com.puppytalk.user.dto.response.UserCreateResult;
import com.puppytalk.user.dto.response.UserResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 사용자 파사드
 * 사용자 관련 유스케이스를 조율하고 트랜잭션 경계를 관리한다.
 */
@Service
@Transactional
public class UserFacade {
    
    private final UserDomainService userDomainService;
    
    public UserFacade(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }
    
    /**
     * 새로운 사용자를 등록한다.
     */
    public UserCreateResult createUser(UserCreateCommand command) {
        Assert.notNull(command, "UserCreateCommand must not be null");
        Assert.hasText(command.username(), "Username must not be null or empty");
        Assert.hasText(command.email(), "Email must not be null or empty");
        
        UserId userId = userDomainService.registerUser(
            command.username(), 
            command.email()
        );
        
        // 생성된 사용자 정보를 조회하여 완전한 결과 반환
        User createdUser = userDomainService.findUserById(userId);
        return UserCreateResult.from(createdUser);
    }
    
    /**
     * 사용자 ID로 사용자를 조회한다.
     */
    @Transactional(readOnly = true)
    public UserResult getUser(UserGetQuery query) {
        Assert.notNull(query, "UserGetQuery must not be null");
        Assert.notNull(query.userId(), "UserId must not be null");
        
        UserId userId = UserId.of(query.userId());
        User user = userDomainService.findUserById(userId);
        
        return UserResult.from(user);
    }
    
    /**
     * 사용자명으로 사용자를 조회한다.
     */
    @Transactional(readOnly = true)
    public UserResult getUserByUsername(String username) {
        Assert.hasText(username, "Username must not be null or empty");
        
        User user = userDomainService.findUserByUsername(username);
        return UserResult.from(user);
    }
    
    /**
     * 이메일로 사용자를 조회한다.
     */
    @Transactional(readOnly = true)
    public UserResult getUserByEmail(String email) {
        Assert.hasText(email, "Email must not be null or empty");
        
        User user = userDomainService.findUserByEmail(email);
        return UserResult.from(user);
    }
    
    /**
     * 사용자를 비활성화한다.
     */
    public void deactivateUser(Long userId) {
        Assert.notNull(userId, "UserId must not be null");
        
        UserId id = UserId.of(userId);
        userDomainService.deactivateUser(id);
    }
    
    /**
     * 사용자를 활성화한다.
     */
    public void activateUser(Long userId) {
        Assert.notNull(userId, "UserId must not be null");
        
        UserId id = UserId.of(userId);
        userDomainService.activateUser(id);
    }
}