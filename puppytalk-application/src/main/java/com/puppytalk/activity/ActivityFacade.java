package com.puppytalk.activity;

import com.puppytalk.activity.dto.request.ActivityRecordCommand;
import com.puppytalk.activity.dto.response.ActivityResult;
import com.puppytalk.activity.dto.response.InactiveUsersResult;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.user.UserId;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 활동 추적 파사드
 * 
 * Backend 관점: 트랜잭션 관리와 흐름 제어
 */
@Service
@Transactional
public class ActivityFacade {
    
    private final ActivityDomainService activityDomainService;
    
    public ActivityFacade(ActivityDomainService activityDomainService) {
        this.activityDomainService = activityDomainService;
    }
    
    /**
     * 사용자 활동 기록
     */
    public ActivityResult recordActivity(ActivityRecordCommand command) {
        Assert.notNull(command, "ActivityRecordCommand must not be null");
        
        UserId userId = UserId.of(command.userId());
        ChatRoomId chatRoomId = command.chatRoomId() != null ? ChatRoomId.of(command.chatRoomId()) : null;
        ActivityType activityType = ActivityType.valueOf(command.activityType());
        
        ActivityId activityId;
        if (chatRoomId != null) {
            activityId = activityDomainService.recordActivity(userId, chatRoomId, activityType);
        } else {
            activityId = activityDomainService.recordGlobalActivity(userId, activityType);
        }
        
        return ActivityResult.created(activityId.getValue());
    }
    
    /**
     * 사용자 최근 활동 조회
     */
    @Transactional(readOnly = true)
    public ActivityResult getLatestActivity(Long userId) {
        Assert.notNull(userId, "UserId must not be null");
        
        UserId userIdObj = UserId.of(userId);
        
        return activityDomainService.getLatestActivity(userIdObj)
            .map(activity -> ActivityResult.from(activity))
            .orElse(ActivityResult.notFound());
    }
    
    /**
     * 비활성 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    public InactiveUsersResult getInactiveUsers() {
        return getInactiveUsers(2); // 기본 2시간
    }
    
    /**
     * 비활성 사용자 목록 조회 (시간 커스텀)
     */
    @Transactional(readOnly = true)
    public InactiveUsersResult getInactiveUsers(int inactiveHours) {
        if (inactiveHours <= 0) {
            throw new IllegalArgumentException("Inactive hours must be positive");
        }
        
        List<Long> userIds = activityDomainService.findInactiveUsers(inactiveHours)
            .stream()
            .map(UserId::getValue)
            .toList();
            
        return InactiveUsersResult.from(userIds);
    }
    
    /**
     * 사용자 활성 상태 확인
     */
    @Transactional(readOnly = true)
    public boolean isUserActive(Long userId, Integer inactiveHours) {
        Assert.notNull(userId, "UserId must not be null");
        
        UserId userIdObj = UserId.of(userId);
        int hours = inactiveHours != null ? inactiveHours : 2;
        
        return activityDomainService.isUserActive(userIdObj, hours);
    }
    
    /**
     * 오래된 활동 데이터 정리
     */
    public int cleanupOldActivities(Integer daysToKeep) {
        int days = daysToKeep != null ? daysToKeep : 30; // 기본 30일
        return activityDomainService.cleanupOldActivities(days);
    }
}