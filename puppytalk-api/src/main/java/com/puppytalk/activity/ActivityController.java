package com.puppytalk.activity;

import com.puppytalk.activity.dto.request.ActivityRecordCommand;
import com.puppytalk.activity.dto.request.ActivityRecordRequest;
import com.puppytalk.activity.dto.response.ActivityResponse;
import com.puppytalk.activity.dto.response.ActivityResult;
import com.puppytalk.support.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Activity", description = "사용자 활동 추적 API")
@RestController
@RequestMapping("/api/activities")
public class ActivityController {
    
    private final ActivityFacade activityFacade;
    
    public ActivityController(ActivityFacade activityFacade) {
        this.activityFacade = activityFacade;
    }
    
    @Operation(summary = "사용자 활동 기록", description = "사용자의 활동 기록을 저장합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "활동 기록 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 또는 채팅방을 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ActivityResponse>> recordActivity(
        @Parameter(description = "활동 기록 요청 정보", required = true)
        @Valid @RequestBody ActivityRecordRequest request
    ) {
        ActivityRecordCommand command = ActivityRecordCommand.of(
            request.userId(),
            request.chatRoomId(),
            request.activityType()
        );
        
        ActivityResult result = activityFacade.recordActivity(command);
        ActivityResponse response = ActivityResponse.from(result);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "사용자 최근 활동 조회", description = "사용자의 최근 활동을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "최근 활동 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/users/{userId}/latest")
    public ResponseEntity<ApiResponse<ActivityResponse>> getLatestActivity(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        ActivityResult result = activityFacade.getLatestActivity(userId);
        ActivityResponse response = ActivityResponse.from(result);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}