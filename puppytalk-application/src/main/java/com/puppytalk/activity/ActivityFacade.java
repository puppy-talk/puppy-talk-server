package com.puppytalk.activity;

import com.puppytalk.activity.dto.request.ActivityRecordCommand;
import com.puppytalk.activity.dto.response.ActivityResult;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ActivityFacade {
    
    private final ActivityDomainService activityDomainService;
    
    public ActivityFacade(ActivityDomainService activityDomainService) {
        this.activityDomainService = activityDomainService;
    }
    
    /**
     * 사용자 활동 기록
     */
    @Transactional
    public ActivityResult recordActivity(ActivityRecordCommand command) {
        Assert.notNull(command, "ActivityRecordCommand must not be null");
        Assert.hasText(command.activityType(), "ActivityType cannot be null or empty");

        UserId userId = UserId.from(command.userId());
        ChatRoomId chatRoomId = command.chatRoomId() != null ? ChatRoomId.from(command.chatRoomId()) : null;
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
     * 사용자의 비활성 상태 확인 (2시간 기준)
     */
    public boolean isUserInactive(Long userId) {
        Assert.notNull(userId, "UserId must not be null");
        
        UserId userIdObj = UserId.from(userId);
        List<UserId> inactiveUsers = activityDomainService.findInactiveUsers(2);
        
        return inactiveUsers.contains(userIdObj);
    }
}