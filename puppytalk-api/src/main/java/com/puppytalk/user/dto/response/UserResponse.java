package com.puppytalk.user.dto.response;

import com.puppytalk.user.dto.response.UserResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 */
@Schema(description = "사용자 정보 응답")
public record UserResponse(
    @Schema(description = "사용자 ID", example = "1")
    Long userId,
    
    @Schema(description = "사용자명", example = "john_doe")
    String username,
    
    @Schema(description = "이메일", example = "john@example.com")
    String email,
    
    @Schema(description = "상태", example = "ACTIVE")
    String status,
    
    @Schema(description = "생성일시", example = "2023-12-01T10:00:00")
    LocalDateTime createdAt
) {
    
    public static UserResponse from(UserResult result) {
        return new UserResponse(
            result.userId(),
            result.username(),
            result.email(),
            result.status().name(),
            result.createdAt()
        );
    }
}