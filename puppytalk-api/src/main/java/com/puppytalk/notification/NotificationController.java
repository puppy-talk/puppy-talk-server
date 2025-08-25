package com.puppytalk.notification;

import com.puppytalk.notification.dto.request.InactivityNotificationRequest;
import com.puppytalk.notification.dto.request.NotificationCreateCommand;
import com.puppytalk.notification.dto.request.NotificationStatusRequest;
import com.puppytalk.notification.dto.request.NotificationStatusUpdateCommand;
import com.puppytalk.notification.dto.request.SystemNotificationRequest;
import com.puppytalk.notification.dto.response.NotificationListResponse;
import com.puppytalk.notification.dto.response.NotificationListResult;
import com.puppytalk.notification.dto.response.NotificationResponse;
import com.puppytalk.notification.dto.response.NotificationResult;
import com.puppytalk.support.ApiResponse;
import com.puppytalk.support.ApiSuccessMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<NotificationResponse>> createInactivityNotification(
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
        NotificationResponse response = NotificationResponse.from(result);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, ApiSuccessMessage.NOTIFICATION_CREATE_SUCCESS));
    }
    
    @Operation(summary = "시스템 알림 생성", description = "시스템 알림을 생성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "시스템 알림 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/system")
    public ResponseEntity<ApiResponse<NotificationResponse>> createSystemNotification(
        @Parameter(description = "시스템 알림 생성 요청", required = true)
        @Valid @RequestBody SystemNotificationRequest request
    ) {
        NotificationCreateCommand command = NotificationCreateCommand.forSystem(
            request.userId(),
            request.title(),
            request.content()
        );
        
        NotificationResult result = notificationFacade.createSystemNotification(command);
        NotificationResponse response = NotificationResponse.from(result);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, ApiSuccessMessage.NOTIFICATION_CREATE_SUCCESS));
    }
    
    @Operation(summary = "발송 대기 중인 알림 목록 조회", description = "스케줄러용 발송 대기 알림 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "발송 대기 알림 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<NotificationListResponse>> getPendingNotifications(
        @Parameter(description = "배치 크기", example = "100")
        @RequestParam(defaultValue = "100") int batchSize
    ) {
        NotificationListResult result = notificationFacade.getPendingNotifications(batchSize);
        NotificationListResponse response = NotificationListResponse.from(result);
        
        return ResponseEntity.ok(ApiResponse.success(response, ApiSuccessMessage.NOTIFICATION_LIST_SUCCESS));
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
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessMessage.NOTIFICATION_STATUS_UPDATE_SUCCESS));
    }
    
    @Operation(summary = "재시도 대상 알림 목록 조회", description = "발송 실패한 재시도 가능 알림 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재시도 대상 알림 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/retryable")
    public ResponseEntity<ApiResponse<NotificationListResponse>> getRetryableNotifications(
        @Parameter(description = "배치 크기", example = "50")
        @RequestParam(defaultValue = "50") int batchSize
    ) {
        NotificationListResult result = notificationFacade.getRetryableNotifications(batchSize);
        NotificationListResponse response = NotificationListResponse.from(result);
        
        return ResponseEntity.ok(ApiResponse.success(response, ApiSuccessMessage.NOTIFICATION_LIST_SUCCESS));
    }
    
    @Operation(summary = "사용자 미읽은 알림 목록 조회", description = "사용자의 미읽은 알림 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "미읽은 알림 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/users/{userId}/unread")
    public ResponseEntity<ApiResponse<NotificationListResponse>> getUnreadNotifications(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        NotificationListResult result = notificationFacade.getUnreadNotifications(userId);
        NotificationListResponse response = NotificationListResponse.from(result);
        
        return ResponseEntity.ok(ApiResponse.success(response, ApiSuccessMessage.NOTIFICATION_LIST_SUCCESS));
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
}