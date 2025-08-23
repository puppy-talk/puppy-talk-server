package com.puppytalk.notification;

import com.puppytalk.notification.dto.request.InactivityNotificationRequest;
import com.puppytalk.notification.dto.request.NotificationCreateCommand;
import com.puppytalk.notification.dto.request.NotificationStatusRequest;
import com.puppytalk.notification.dto.request.NotificationStatusUpdateCommand;
import com.puppytalk.notification.dto.request.SystemNotificationRequest;
import com.puppytalk.notification.dto.response.NotificationListResult;
import com.puppytalk.notification.dto.response.NotificationResult;
import com.puppytalk.notification.dto.response.NotificationStatsResult;
import com.puppytalk.support.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 관리 API
 * 
 * Backend 관점: 안정적이고 확장 가능한 알림 API
 */
@Tag(name = "Notification", description = "알림 관리 API")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private final NotificationFacade notificationFacade;
    
    public NotificationController(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }
    
    @Operation(summary = "비활성 사용자 알림 생성", description = "비활성 사용자에게 반려동물 메시지 알림을 생성합니다.")
    @PostMapping("/inactivity")
    public ResponseEntity<ApiResponse<NotificationResult>> createInactivityNotification(
        @Parameter(description = "비활성 사용자 알림 생성 요청", required = true)
        @Valid @RequestBody InactivityNotificationRequest request
    ) {
        NotificationCreateCommand command = NotificationCreateCommand.forInactivity(
            request.userId(),
            request.petId(),
            request.chatRoomId(),
            request.title(),
            request.content()
        );
        
        NotificationResult result = notificationFacade.createInactivityNotification(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(result, "비활성 사용자 알림 생성 완료"));
    }
    
    @Operation(summary = "시스템 알림 생성", description = "시스템 알림을 생성합니다.")
    @PostMapping("/system")
    public ResponseEntity<ApiResponse<NotificationResult>> createSystemNotification(
        @Parameter(description = "시스템 알림 생성 요청", required = true)
        @Valid @RequestBody SystemNotificationRequest request
    ) {
        NotificationCreateCommand command = NotificationCreateCommand.forSystem(
            request.userId(),
            request.title(),
            request.content()
        );
        
        NotificationResult result = notificationFacade.createSystemNotification(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(result, "시스템 알림 생성 완료"));
    }
    
    @Operation(summary = "발송 대기 중인 알림 목록 조회", description = "스케줄러용 발송 대기 알림 목록을 조회합니다.")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<NotificationListResult>> getPendingNotifications(
        @Parameter(description = "배치 크기", example = "100")
        @RequestParam(defaultValue = "100") int batchSize
    ) {
        NotificationListResult result = notificationFacade.getPendingNotifications(batchSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @Operation(summary = "알림 상태 업데이트", description = "알림의 상태를 업데이트합니다.")
    @PutMapping("/{notificationId}/status")
    public ResponseEntity<ApiResponse<Void>> updateNotificationStatus(
        @Parameter(description = "알림 ID", required = true)
        @PathVariable Long notificationId,
        @Parameter(description = "상태 업데이트 요청", required = true)
        @Valid @RequestBody NotificationStatusRequest request
    ) {
        NotificationStatusUpdateCommand command = switch (request.status().toUpperCase()) {
            case "SENT" -> NotificationStatusUpdateCommand.sent(notificationId);
            case "READ" -> NotificationStatusUpdateCommand.read(notificationId);
            case "FAILED" -> NotificationStatusUpdateCommand.failed(notificationId, request.failureReason());
            default -> throw new IllegalArgumentException("Unsupported status: " + request.status());
        };
        
        notificationFacade.updateNotificationStatus(command);
        return ResponseEntity.ok(ApiResponse.success("상태 업데이트 완료"));
    }
    
    @Operation(summary = "재시도 대상 알림 목록 조회", description = "발송 실패한 재시도 가능 알림 목록을 조회합니다.")
    @GetMapping("/retryable")
    public ResponseEntity<ApiResponse<NotificationListResult>> getRetryableNotifications(
        @Parameter(description = "배치 크기", example = "50")
        @RequestParam(defaultValue = "50") int batchSize
    ) {
        NotificationListResult result = notificationFacade.getRetryableNotifications(batchSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @Operation(summary = "사용자 미읽은 알림 목록 조회", description = "사용자의 미읽은 알림 목록을 조회합니다.")
    @GetMapping("/users/{userId}/unread")
    public ResponseEntity<ApiResponse<NotificationListResult>> getUnreadNotifications(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    ) {
        NotificationListResult result = notificationFacade.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @Operation(summary = "사용자 미읽은 알림 개수 조회", description = "사용자의 미읽은 알림 개수를 조회합니다.")
    @GetMapping("/users/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    ) {
        long count = notificationFacade.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @Operation(summary = "비활성 사용자 목록 조회", description = "알림 대상 비활성 사용자 목록을 조회합니다.")
    @GetMapping("/inactive-users")
    public ResponseEntity<ApiResponse<List<Long>>> findInactiveUsersForNotification() {
        List<Long> userIds = notificationFacade.findInactiveUsersForNotification();
        return ResponseEntity.ok(ApiResponse.success(userIds));
    }
    
    @Operation(summary = "알림 통계 조회", description = "지정된 기간의 알림 발송 통계를 조회합니다.")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<NotificationStatsResult>> getNotificationStats(
        @Parameter(description = "시작일시 (ISO 8601 형식)", example = "2023-12-01T00:00:00")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
        LocalDateTime startDate,
        
        @Parameter(description = "종료일시 (ISO 8601 형식)", example = "2023-12-07T23:59:59")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
        LocalDateTime endDate
    ) {
        NotificationStatsResult result = notificationFacade.getNotificationStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @Operation(summary = "만료된 알림 정리", description = "만료된 알림을 정리합니다.")
    @DeleteMapping("/cleanup/expired")
    public ResponseEntity<ApiResponse<Integer>> cleanupExpiredNotifications() {
        int deletedCount = notificationFacade.cleanupExpiredNotifications();
        return ResponseEntity.ok(ApiResponse.success(deletedCount, "만료된 알림 정리 완료"));
    }
    
    @Operation(summary = "오래된 알림 정리", description = "완료된 오래된 알림을 정리합니다.")
    @DeleteMapping("/cleanup/old")
    public ResponseEntity<ApiResponse<Integer>> cleanupOldNotifications() {
        int deletedCount = notificationFacade.cleanupOldNotifications();
        return ResponseEntity.ok(ApiResponse.success(deletedCount, "오래된 알림 정리 완료"));
    }
    
    @Operation(summary = "사용자 비활성 상태 확인", description = "사용자가 현재 비활성 상태인지 확인합니다.")
    @GetMapping("/users/{userId}/inactive")
    public ResponseEntity<ApiResponse<Boolean>> isUserInactive(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    ) {
        boolean isInactive = notificationFacade.isUserInactive(userId);
        return ResponseEntity.ok(ApiResponse.success(isInactive));
    }
}