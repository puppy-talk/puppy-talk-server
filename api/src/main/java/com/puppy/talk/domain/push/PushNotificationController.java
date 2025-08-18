package com.puppy.talk.domain.push;

import com.puppy.talk.global.support.ApiResponse;
import com.puppy.talk.domain.push.dto.request.DeviceTokenRequest;
import com.puppy.talk.domain.push.dto.response.DeviceTokenResponse;
import com.puppy.talk.domain.push.dto.response.PushNotificationResponse;
import com.puppy.talk.domain.push.dto.response.PushStatisticsResponse;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.chat.DeviceTokenService;
import com.puppy.talk.notification.PushNotificationService;
import com.puppy.talk.push.command.DeviceTokenRegistrationCommand;
import com.puppy.talk.push.DeviceToken;
import com.puppy.talk.push.PushNotification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 푸시 알림 관련 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
@Tag(name = "Push Notifications", description = "푸시 알림 관리 API")
public class PushNotificationController {
    
    private final DeviceTokenService deviceTokenService;
    private final PushNotificationService pushNotificationService;
    
    /**
     * 디바이스 토큰 등록
     */
    @PostMapping("/device-tokens")
    @Operation(summary = "디바이스 토큰 등록", description = "사용자의 디바이스 토큰을 등록합니다.")
    public ApiResponse<DeviceTokenResponse> registerDeviceToken(
        @RequestBody DeviceTokenRequest request,
        @Parameter(description = "사용자 ID") @RequestHeader("X-User-Id") Long userId
    ) {
        log.info("Registering device token for user: {}", userId);
        
        DeviceTokenRegistrationCommand command = DeviceTokenRegistrationCommand.of(
            userId,
            request.token(),
            request.deviceId(),
            request.platform()
        );
        
        DeviceToken deviceToken = deviceTokenService.registerOrUpdateToken(command);
        
        DeviceTokenResponse response = DeviceTokenResponse.from(deviceToken);
        return ApiResponse.ok(response);
    }
    
    /**
     * 사용자의 활성 디바이스 토큰 목록 조회
     */
    @GetMapping("/device-tokens")
    @Operation(summary = "활성 디바이스 토큰 조회", description = "사용자의 활성 디바이스 토큰 목록을 조회합니다.")
    public ApiResponse<List<DeviceTokenResponse>> getActiveDeviceTokens(
        @Parameter(description = "사용자 ID") @RequestHeader("X-User-Id") Long userId
    ) {
        log.debug("Getting active device tokens for user: {}", userId);
        
        List<DeviceToken> activeTokens = deviceTokenService.getActiveTokensByUserId(UserIdentity.of(userId));
        List<DeviceTokenResponse> responses = activeTokens.stream()
            .map(DeviceTokenResponse::from)
            .toList();
            
        return ApiResponse.ok(responses);
    }
    
    /**
     * 디바이스 토큰 비활성화
     */
    @PostMapping("/device-tokens/{token}/deactivate")
    @Operation(summary = "디바이스 토큰 비활성화", description = "특정 디바이스 토큰을 비활성화합니다.")
    public ApiResponse<Void> deactivateDeviceToken(
        @Parameter(description = "디바이스 토큰") @PathVariable String token
    ) {
        log.info("Deactivating device token: {}", token);
        
        deviceTokenService.deactivateToken(token);
        
        return ApiResponse.ok();
    }
    
    /**
     * 모든 디바이스 토큰 비활성화
     */
    @PostMapping("/device-tokens/deactivate-all")
    @Operation(summary = "모든 디바이스 토큰 비활성화", description = "사용자의 모든 디바이스 토큰을 비활성화합니다.")
    public ApiResponse<Void> deactivateAllDeviceTokens(
        @Parameter(description = "사용자 ID") @RequestHeader("X-User-Id") Long userId
    ) {
        log.info("Deactivating all device tokens for user: {}", userId);
        
        deviceTokenService.deactivateAllTokensByUserId(UserIdentity.of(userId));
        
        return ApiResponse.ok();
    }
    
    /**
     * 푸시 알림 히스토리 조회
     */
    @GetMapping("/notifications")
    @Operation(summary = "푸시 알림 히스토리 조회", description = "사용자의 푸시 알림 히스토리를 조회합니다.")
    public ApiResponse<List<PushNotificationResponse>> getNotificationHistory(
        @Parameter(description = "사용자 ID") @RequestHeader("X-User-Id") Long userId,
        @Parameter(description = "조회할 알림 개수") @RequestParam(defaultValue = "20") int limit
    ) {
        log.debug("Getting notification history for user: {}, limit: {}", userId, limit);
        
        List<PushNotification> notifications = pushNotificationService.getNotificationHistory(
            UserIdentity.of(userId), limit
        );
        
        List<PushNotificationResponse> responses = notifications.stream()
            .map(PushNotificationResponse::from)
            .toList();
            
        return ApiResponse.ok(responses);
    }
    
    /**
     * 푸시 알림 수신 확인
     */
    @PostMapping("/notifications/{notificationId}/received")
    @Operation(summary = "푸시 알림 수신 확인", description = "푸시 알림을 수신 확인 처리합니다.")
    public ApiResponse<Void> markNotificationAsReceived(
        @Parameter(description = "알림 ID") @PathVariable Long notificationId
    ) {
        log.debug("Marking notification as received: {}", notificationId);
        
        pushNotificationService.markAsReceived(notificationId);
        
        return ApiResponse.ok();
    }
    
    /**
     * 푸시 알림 통계 조회
     */
    @GetMapping("/statistics")
    @Operation(summary = "푸시 알림 통계 조회", description = "푸시 알림 전송 통계를 조회합니다.")
    public ApiResponse<PushStatisticsResponse> getStatistics() {
        log.debug("Getting push notification statistics");
        
        PushNotificationService.NotificationStatistics stats = 
            pushNotificationService.getStatistics();
            
        PushStatisticsResponse response = PushStatisticsResponse.from(stats);
        
        return ApiResponse.ok(response);
    }
    
    /**
     * 토큰 사용 시간 업데이트 (앱 사용 시 호출)
     */
    @PostMapping("/device-tokens/{token}/ping")
    @Operation(summary = "토큰 사용 시간 업데이트", description = "디바이스 토큰의 마지막 사용 시간을 업데이트합니다.")
    public ApiResponse<Void> pingDeviceToken(
        @Parameter(description = "디바이스 토큰") @PathVariable String token
    ) {
        log.debug("Updating token usage: {}", token);
        
        deviceTokenService.updateTokenUsage(token);
        
        return ApiResponse.ok();
    }
}