package com.puppytalk.notification;

/**
 * 알림 상태
 * 
 * Backend 관점: 신뢰성 있는 메시지 전달을 위한 상태 관리
 */
public enum NotificationStatus {
    
    /**
     * 생성됨 - 알림이 생성되었지만 아직 발송되지 않음
     */
    CREATED("생성됨"),
    
    /**
     * 발송 대기 중 - 발송 큐에 추가됨
     */
    QUEUED("발송 대기"),
    
    /**
     * 발송 중 - 현재 발송 중
     */
    SENDING("발송 중"),
    
    /**
     * 발송 완료 - 성공적으로 발송됨
     */
    SENT("발송 완료"),
    
    /**
     * 읽음 - 사용자가 알림을 읽음
     */
    READ("읽음"),
    
    /**
     * 발송 실패 - 발송에 실패함
     */
    FAILED("발송 실패"),
    
    /**
     * 만료됨 - 발송 기한이 지나 만료됨
     */
    EXPIRED("만료됨"),
    
    /**
     * 취소됨 - 발송이 취소됨
     */
    CANCELLED("취소됨");
    
    private final String description;
    
    NotificationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 재시도 가능한 상태인지 판단
     */
    public boolean isRetryable() {
        return this == CREATED || this == QUEUED || this == FAILED;
    }
    
    /**
     * 완료된 상태인지 판단 (성공/실패 포함)
     */
    public boolean isCompleted() {
        return this == SENT || this == READ || this == FAILED || this == EXPIRED || this == CANCELLED;
    }
    
    /**
     * 진행 중인 상태인지 판단
     */
    public boolean isInProgress() {
        return this == CREATED || this == QUEUED || this == SENDING;
    }
    
    /**
     * 성공적으로 전달된 상태인지 판단
     */
    public boolean isSuccessful() {
        return this == SENT || this == READ;
    }
}