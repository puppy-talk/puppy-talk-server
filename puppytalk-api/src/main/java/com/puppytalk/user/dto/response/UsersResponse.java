package com.puppytalk.user.dto.response;

import com.puppytalk.user.dto.response.UserCreateResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 다중 사용자 응답 DTO
 */
@Schema(description = "사용자 목록 응답")
public record UsersResponse(
    @Schema(description = "사용자 목록")
    List<UserResponse> users,
    
    @Schema(description = "총 사용자 수")
    int totalCount
) {
    
    public static UsersResponse from(List<UserResponse> users) {
        return new UsersResponse(users, users.size());
    }
    
    /**
     * 사용자 생성 결과로부터 단일 사용자 목록 생성
     */
    public static UsersResponse fromCreateResult(UserCreateResult result) {
        UserResponse userResponse = UserResponse.from(result.userResult());
        return new UsersResponse(List.of(userResponse), 1);
    }
}