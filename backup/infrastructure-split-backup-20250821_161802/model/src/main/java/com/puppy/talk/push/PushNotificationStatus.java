package com.puppy.talk.push;

/**
 * 푸시 알림 상태
 */
public enum PushNotificationStatus {
    PENDING,    // 대기 중
    SENT,       // 전송 완료
    FAILED,     // 전송 실패
    RECEIVED    // 수신 확인
}