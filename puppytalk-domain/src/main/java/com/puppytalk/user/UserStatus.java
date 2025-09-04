package com.puppytalk.user;

/**
 * 사용자 상태
 */
public enum UserStatus {
    /**
     * 활성 사용자 - 정상적으로 서비스 이용 중
     */
    ACTIVE("활성"),
    
    /**
     * 휴면 사용자 - 4주간 미접속으로 휴면 상태
     */
    DORMANT("휴면"),
    
    /**
     * 삭제된 사용자 - 탈퇴 또는 삭제됨
     */
    DELETED("삭제됨");
    
    private final String description;
    
    UserStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 알림을 받을 수 있는 상태인지 확인
     */
    public boolean canReceiveNotifications() {
        return this == ACTIVE;
    }
}