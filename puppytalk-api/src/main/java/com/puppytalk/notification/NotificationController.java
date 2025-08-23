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
import com.puppytalk.support.ApiSuccessMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "알림 관리 API")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private final NotificationFacade notificationFacade;
    
    public NotificationController(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }
    
    @Operation(summary = "비활성 사용자 알림 생성", description = "비활성 사용자에게 반려동물 메시지 알림을 생성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "비활성 사용자 알림 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
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
            .body(ApiResponse.success(result, ApiSuccessMessage.NOTIFICATION_CREATE_SUCCESS.getMessage()));
    }
    
    @Operation(summary = "시스템 알림 생성", description = "시스템 알림을 생성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "시스템 알림 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
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
            .body(ApiResponse.success(result, ApiSuccessMessage.NOTIFICATION_CREATE_SUCCESS.getMessage()));
    }
    
    @Operation(summary = "발송 대기 중인 알림 목록 조회", description = "스케줄러용 발송 대기 알림 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "발송 대기 알림 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<NotificationListResult>> getPendingNotifications(
        @Parameter(description = "배치 크기", example = "100")
        @RequestParam(defaultValue = "100") int batchSize
    ) {
        NotificationListResult result = notificationFacade.getPendingNotifications(batchSize);
        return ResponseEntity.ok(ApiResponse.success(result, ApiSuccessMessage.NOTIFICATION_LIST_SUCCESS.getMessage()));
    }
    
    @Operation(summary = "알림 상태 업데이트", description = "알림의 상태를 업데이트합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "알림 상태 업데이트 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "지원하지 않는 상태값")
    })
    @PutMapping("/{notificationId}/status")
    public ResponseEntity<ApiResponse<Void>> updateNotificationStatus(
        @Parameter(description = "알림 ID", required = true, example = "1")
        @PathVariable Long notificationId,
        @Parameter(description = "상태 업데이트 요청", required = true)
        @Valid @RequestBody NotificationStatusRequest request
    ) {
        NotificationStatusUpdateCommand command = NotificationStatusUpdateCommand.of(
            notificationId,
            request.status(),
            request.failureReason()
        );
        
        notificationFacade.updateNotificationStatus(command);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessMessage.NOTIFICATION_STATUS_UPDATE_SUCCESS.getMessage()));
    }
    
    @Operation(summary = "재시도 대상 알림 목록 조회", description = "발송 실패한 재시도 가능 알림 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재시도 대상 알림 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/retryable")
    public ResponseEntity<ApiResponse<NotificationListResult>> getRetryableNotifications(
        @Parameter(description = "배치 크기", example = "50")
        @RequestParam(defaultValue = "50") int batchSize
    ) {
        NotificationListResult result = notificationFacade.getRetryableNotifications(batchSize);
        return ResponseEntity.ok(ApiResponse.success(result, ApiSuccessMessage.NOTIFICATION_LIST_SUCCESS.getMessage()));
    }
    
    @Operation(summary = "사용자 미읽은 알림 목록 조회", description = "사용자의 미읽은 알림 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "미읽은 알림 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/users/{userId}/unread")
    public ResponseEntity<ApiResponse<NotificationListResult>> getUnreadNotifications(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        NotificationListResult result = notificationFacade.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(result, ApiSuccessMessage.NOTIFICATION_LIST_SUCCESS.getMessage()));
    }
    
    @Operation(summary = "사용자 미읽은 알림 개수 조회", description = "사용자의 미읽은 알림 개수를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "미읽은 알림 개수 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/users/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        long count = notificationFacade.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @Operation(summary = "비활성 사용자 목록 조회", description = "알림 대상 비활성 사용자 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비활성 사용자 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/inactive-users")
    public ResponseEntity<ApiResponse<List<Long>>> findInactiveUsersForNotification() {
        List<Long> userIds = notificationFacade.findInactiveUsersForNotification();
        return ResponseEntity.ok(ApiResponse.success(userIds));
    }
    
    @Operation(summary = "알림 통계 조회", description = "지정된 기간의 알림 발송 통계를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "알림 통계 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
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
        return ResponseEntity.ok(ApiResponse.success(result, ApiSuccessMessage.NOTIFICATION_LIST_SUCCESS.getMessage()));
    }
    
    @Operation(summary = "만료된 알림 정리", description = "만료된 알림을 정리합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "만료된 알림 정리 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @DeleteMapping("/cleanup/expired")
    public ResponseEntity<ApiResponse<Integer>> cleanupExpiredNotifications() {
        int deletedCount = notificationFacade.cleanupExpiredNotifications();
        return ResponseEntity.ok(ApiResponse.success(deletedCount, ApiSuccessMessage.NOTIFICATION_CLEANUP_SUCCESS.getMessage()));
    }
    
    @Operation(summary = "오래된 알림 정리", description = "완료된 오래된 알림을 정리합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "오래된 알림 정리 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @DeleteMapping("/cleanup/old")
    public ResponseEntity<ApiResponse<Integer>> cleanupOldNotifications() {
        int deletedCount = notificationFacade.cleanupOldNotifications();
        return ResponseEntity.ok(ApiResponse.success(deletedCount, ApiSuccessMessage.NOTIFICATION_CLEANUP_SUCCESS.getMessage()));
    }
    
    @Operation(summary = "사용자 비활성 상태 확인", description = "사용자가 현재 비활성 상태인지 확인합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용자 비활성 상태 확인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/users/{userId}/inactive")
    public ResponseEntity<ApiResponse<Boolean>> isUserInactive(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        boolean isInactive = notificationFacade.isUserInactive(userId);
        return ResponseEntity.ok(ApiResponse.success(isInactive));
    }
}