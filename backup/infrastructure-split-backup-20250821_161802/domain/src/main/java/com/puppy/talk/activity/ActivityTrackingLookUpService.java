package com.puppy.talk.activity;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.user.UserIdentity;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 활동 추적 조회 서비스 인터페이스
 */
public interface ActivityTrackingLookUpService {
    
    /**
     * 사용자 활동을 기록합니다.
     * 
     * @param userId 사용자 식별자
     * @param chatRoomId 채팅방 식별자
     * @param activityTime 활동 시간
     */
    void recordActivity(UserIdentity userId, ChatRoomIdentity chatRoomId, LocalDateTime activityTime);
    
    /**
     * 사용자의 마지막 활동을 조회합니다.
     * 
     * @param userId 사용자 식별자
     * @return 마지막 활동 정보
     */
    Optional<UserActivity> getLastActivity(UserIdentity userId);
    
    /**
     * 채팅방에서 사용자의 마지막 활동을 조회합니다.
     * 
     * @param userId 사용자 식별자
     * @param chatRoomId 채팅방 식별자
     * @return 마지막 활동 정보
     */
    Optional<UserActivity> getLastActivityInChatRoom(UserIdentity userId, ChatRoomIdentity chatRoomId);
    
    /**
     * 비활성 알림을 설정합니다.
     * 
     * @param userId 사용자 식별자
     * @param chatRoomId 채팅방 식별자
     * @param scheduleTime 예약 시간
     */
    void scheduleInactivityNotification(UserIdentity userId, ChatRoomIdentity chatRoomId, LocalDateTime scheduleTime);
}