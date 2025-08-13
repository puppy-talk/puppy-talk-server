package com.puppy.talk.model.push;

/**
 * 푸시 알림 타입
 */
public enum NotificationType {
    INACTIVITY_MESSAGE,    // 비활성 상태 메시지 알림
    NEW_MESSAGE,          // 새 메시지 도착 알림  
    SYSTEM_ANNOUNCEMENT,  // 시스템 공지 알림
    PET_STATUS_CHANGE     // 펫 상태 변화 알림
}