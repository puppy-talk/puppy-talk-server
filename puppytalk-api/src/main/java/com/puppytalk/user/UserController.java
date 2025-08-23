package com.puppytalk.user;

import com.puppytalk.support.ApiResponse;
import com.puppytalk.user.dto.request.UserCreateCommand;
import com.puppytalk.user.dto.request.UserCreateRequest;
import com.puppytalk.user.dto.request.UserGetQuery;
import com.puppytalk.user.dto.response.UserCreateResult;
import com.puppytalk.user.dto.response.UserResult;
import com.puppytalk.user.dto.response.UserResponse;
import com.puppytalk.user.dto.response.UsersResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관리 API
 * 
 * Backend 관점: 안전하고 확장 가능한 사용자 관리 API
 */
@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    
    private final UserFacade userFacade;
    
    public UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }
    
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
        @Parameter(description = "사용자 생성 요청", required = true)
        @Valid @RequestBody UserCreateRequest request
    ) {
        UserCreateCommand command = UserCreateCommand.of(
            request.username(),
            request.email()
        );
        
        UserCreateResult result = userFacade.createUser(command);
        UserResponse response = UserResponse.from(result.userResult());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "사용자 생성 완료"));
    }
    
    @Operation(summary = "사용자 조회", description = "사용자 ID로 사용자 정보를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable @Positive(message = "사용자 ID는 양수여야 합니다") Long userId
    ) {
        UserGetQuery query = UserGetQuery.of(userId);
        UserResult result = userFacade.getUser(query);
        UserResponse response = UserResponse.from(result);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "사용자명으로 조회", description = "사용자명으로 사용자 정보를 조회합니다.")
    @GetMapping("/by-username/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(
        @Parameter(description = "사용자명", required = true, example = "john_doe")
        @PathVariable String username
    ) {
        UserResult result = userFacade.getUserByUsername(username);
        UserResponse response = UserResponse.from(result);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "이메일로 조회", description = "이메일로 사용자 정보를 조회합니다.")
    @GetMapping("/by-email")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
        @Parameter(description = "이메일", required = true, example = "john@example.com")
        @RequestParam String email
    ) {
        UserResult result = userFacade.getUserByEmail(email);
        UserResponse response = UserResponse.from(result);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "사용자 비활성화", description = "사용자를 비활성화합니다.")
    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable @Positive(message = "사용자 ID는 양수여야 합니다") Long userId
    ) {
        userFacade.deactivateUser(userId);
        
        return ResponseEntity.ok(ApiResponse.success("사용자 비활성화 완료"));
    }
    
    @Operation(summary = "사용자 활성화", description = "비활성화된 사용자를 다시 활성화합니다.")
    @PatchMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable @Positive(message = "사용자 ID는 양수여야 합니다") Long userId
    ) {
        userFacade.activateUser(userId);
        
        return ResponseEntity.ok(ApiResponse.success("사용자 활성화 완료"));
    }
}