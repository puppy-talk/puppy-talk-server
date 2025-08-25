package com.puppytalk.user;

import com.puppytalk.support.ApiResponse;
import com.puppytalk.support.ApiSuccessMessage;
import com.puppytalk.user.dto.request.UserCreateCommand;
import com.puppytalk.user.dto.request.UserCreateRequest;
import com.puppytalk.user.dto.request.UserGetQuery;
import com.puppytalk.user.dto.response.UserCreateResult;
import com.puppytalk.user.dto.response.UserResponse;
import com.puppytalk.user.dto.response.UserResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            request.email(),
            request.password()
        );
        
        UserCreateResult result = userFacade.createUser(command);
        UserResponse response = UserResponse.from(result.userResult());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, ApiSuccessMessage.USER_CREATE_SUCCESS));
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
        
        return ResponseEntity.ok(ApiResponse.success(response, ApiSuccessMessage.USER_GET_SUCCESS));
    }
}