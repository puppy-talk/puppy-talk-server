package com.puppytalk.scheduler;

/**
 * 스케줄러 모듈 전용 로그 포맷 상수 클래스
 */
public final class LogFormats {
    
    // 스케줄러 공통 로그
    public static final String SCHEDULER_START = "SCHEDULER_START: jobName={}, startTime={}";
    public static final String SCHEDULER_COMPLETE = "SCHEDULER_COMPLETE: jobName={}, duration={}ms, processedCount={}";
    public static final String SCHEDULER_ERROR = "SCHEDULER_ERROR: jobName={}, error={}, duration={}ms";
    
    // 휴면 사용자 처리 스케줄러
    public static final String DORMANT_PROCESSING_START = "DORMANT_PROCESSING_START: cutoffDate={}, candidateCount={}";
    public static final String DORMANT_USER_PROCESSED = "DORMANT_USER_PROCESSED: userId={}, lastActiveAt={}";
    public static final String DORMANT_USER_SKIPPED = "DORMANT_USER_SKIPPED: userId={}, reason={}";
    public static final String DORMANT_PROCESSING_COMPLETE = "DORMANT_PROCESSING_COMPLETE: totalCandidates={}, processedCount={}, duration={}ms";
    
    // 알림 처리 스케줄러
    public static final String NOTIFICATION_SCHEDULER_START = "NOTIFICATION_SCHEDULER_START: jobType={}, batchSize={}";
    public static final String NOTIFICATION_BATCH_PROCESSED = "NOTIFICATION_BATCH_PROCESSED: jobType={}, processedCount={}, failedCount={}";
    public static final String NOTIFICATION_CLEANUP_COMPLETE = "NOTIFICATION_CLEANUP_COMPLETE: expiredCount={}, oldCount={}, duration={}ms";
    
    // 비활성 사용자 감지 스케줄러
    public static final String INACTIVE_USER_DETECTION_START = "INACTIVE_USER_DETECTION_START: cutoffHours={}, targetUserCount={}";
    public static final String INACTIVE_NOTIFICATION_CREATED = "INACTIVE_NOTIFICATION_CREATED: userId={}, petId={}, notificationType={}";
    public static final String INACTIVE_USER_DETECTION_COMPLETE = "INACTIVE_USER_DETECTION_COMPLETE: targetUsers={}, createdNotifications={}, duration={}ms";
    
    private LogFormats() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}