package com.puppy.talk.infrastructure.activity;

import com.puppy.talk.model.activity.ActivityType;
import com.puppy.talk.model.activity.UserActivity;
import com.puppy.talk.model.activity.UserActivityIdentity;
import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.user.UserIdentity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 활동 저장소 인터페이스
 */
public interface UserActivityRepository {
    
    /**
     * 사용자 활동을 저장합니다.
     */
    UserActivity save(UserActivity userActivity);
    
    /**
     * 사용자 활동을 식별자로 조회합니다.
     */
    Optional<UserActivity> findByIdentity(UserActivityIdentity identity);
    
    /**
     * 특정 사용자의 활동을 조회합니다.
     */
    List<UserActivity> findByUserId(UserIdentity userId);
    
    /**
     * 특정 채팅방의 활동을 조회합니다.
     */
    List<UserActivity> findByChatRoomId(ChatRoomIdentity chatRoomId);
    
    /**
     * 특정 사용자의 마지막 활동을 조회합니다.
     */
    Optional<UserActivity> findLastActivityByUserId(UserIdentity userId);
    
    /**
     * 특정 채팅방의 마지막 활동을 조회합니다.
     */
    Optional<UserActivity> findLastActivityByChatRoomId(ChatRoomIdentity chatRoomId);
    
    /**
     * 지정된 시간 이전의 마지막 활동을 가진 채팅방들을 조회합니다.
     */
    List<ChatRoomIdentity> findChatRoomsWithLastActivityBefore(LocalDateTime beforeTime);
    
    /**
     * 특정 활동 유형별로 활동을 조회합니다.
     */
    List<UserActivity> findByActivityType(ActivityType activityType);
    
    /**
     * 특정 기간 내의 활동을 조회합니다.
     */
    List<UserActivity> findByActivityAtBetween(LocalDateTime startTime, LocalDateTime endTime);
}