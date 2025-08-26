package com.puppytalk.activity;

import com.puppytalk.activity.dto.request.ActivityRecordCommand;
import com.puppytalk.activity.dto.response.ActivityResult;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

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
            .map(ActivityResult::from)
            .orElse(ActivityResult.notFound());
    }
}