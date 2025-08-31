package com.puppytalk.notification;

import com.puppytalk.support.EntityId;

/**
 * 알림 ID 값 객체
 */
public class NotificationId extends EntityId {
    
    private NotificationId(Long value) {
        super(value);
    }
    
    /**
     * 하나의 매개변수를 받아 타입 변환 (데이터베이스에서 조회된 값용)
     */
    public static NotificationId from(Long value) {
        return new NotificationId(value);
    }
    
}