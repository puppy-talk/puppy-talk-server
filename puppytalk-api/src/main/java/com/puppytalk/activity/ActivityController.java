package com.puppytalk.activity;

import com.puppytalk.activity.dto.request.ActivityRecordCommand;
import com.puppytalk.activity.dto.request.ActivityRecordRequest;
import com.puppytalk.activity.dto.response.ActivityResult;
import com.puppytalk.activity.dto.response.InactiveUsersResult;
import com.puppytalk.support.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 활동 관리 API
 * 
 * Backend 관점: 성능과 안정성 중심의 API 설계
 */
@Tag(name = "Activity", description = "사용자 활동 추적 API")
@RestController
@RequestMapping("/api/activities")
public class ActivityController {
    
    private final ActivityFacade activityFacade;
    
    public ActivityController(ActivityFacade activityFacade) {
        this.activityFacade = activityFacade;
    }
    
    @Operation(summary = "사용자 활동 기록", description = "사용자의 활동을 추적하여 기록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ActivityResult>> recordActivity(
        @Parameter(description = "활동 기록 요청", required = true)
        @Valid @RequestBody ActivityRecordRequest request
    ) {
        ActivityRecordCommand command = ActivityRecordCommand.of(
            request.userId(),
            request.chatRoomId(),
            request.activityType()
        );
        
        ActivityResult result = activityFacade.recordActivity(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @Operation(summary = "사용자 최근 활동 조회", description = "사용자의 최근 활동을 조회합니다.")
    @GetMapping("/users/{userId}/latest")
    public ResponseEntity<ApiResponse<ActivityResult>> getLatestActivity(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    ) {
        ActivityResult result = activityFacade.getLatestActivity(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @Operation(summary = "비활성 사용자 목록 조회", description = "지정된 시간 동안 활동이 없는 사용자 목록을 조회합니다.")
    @GetMapping("/inactive-users")
    public ResponseEntity<ApiResponse<InactiveUsersResult>> getInactiveUsers(
        @Parameter(description = "비활성 기준 시간(시간)", example = "2")
        @RequestParam(defaultValue = "2") int hours
    ) {
        InactiveUsersResult result = activityFacade.getInactiveUsers(hours);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @Operation(summary = "사용자 활성 상태 확인", description = "사용자가 현재 활성 상태인지 확인합니다.")
    @GetMapping("/users/{userId}/active")
    public ResponseEntity<ApiResponse<Boolean>> isUserActive(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId,
        @Parameter(description = "비활성 기준 시간(시간)", example = "2")
        @RequestParam(required = false) Integer hours
    ) {
        boolean isActive = activityFacade.isUserActive(userId, hours);
        return ResponseEntity.ok(ApiResponse.success(isActive));
    }
    
    @Operation(summary = "오래된 활동 데이터 정리", description = "지정된 일수보다 오래된 활동 데이터를 정리합니다.")
    @DeleteMapping("/cleanup")
    public ResponseEntity<ApiResponse<Integer>> cleanupOldActivities(
        @Parameter(description = "보관 기간(일)", example = "30")
        @RequestParam(required = false) Integer daysToKeep
    ) {
        int deletedCount = activityFacade.cleanupOldActivities(daysToKeep);
        return ResponseEntity.ok(ApiResponse.success(deletedCount, "정리 완료"));
    }
}