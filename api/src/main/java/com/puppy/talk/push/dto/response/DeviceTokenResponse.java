package com.puppy.talk.push.dto.response;

import com.puppy.talk.push.DeviceToken;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 디바이스 토큰 응답 DTO
 */
@Schema(description = "디바이스 토큰 응답")
public record DeviceTokenResponse(
    
    @Schema(description = "토큰 ID", example = "1")
    Long id,
    
    @Schema(description = "디바이스 토큰", example = "dGhpcyBpcyBhIG1vY2sgdG9rZW4=")
    String token,
    
    @Schema(description = "디바이스 ID", example = "device-12345")
    String deviceId,
    
    @Schema(description = "플랫폼", example = "android")
    String platform,
    
    @Schema(description = "활성 상태", example = "true")
    boolean isActive,
    
    @Schema(description = "마지막 사용 시간", example = "2024-01-01T10:30:00")
    LocalDateTime lastUsedAt,
    
    @Schema(description = "생성 시간", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt
) {
    
    public static DeviceTokenResponse from(DeviceToken deviceToken) {
        return new DeviceTokenResponse(
            deviceToken.identity() != null ? deviceToken.identity().id() : null,
            hideToken(deviceToken.token()),
            deviceToken.deviceId(),
            deviceToken.platform(),
            deviceToken.isActive(),
            deviceToken.lastUsedAt(),
            deviceToken.createdAt()
        );
    }
    
    /**
     * 보안을 위해 토큰의 일부만 표시합니다.
     */
    private static String hideToken(String token) {
        if (token == null || token.length() < 8) {
            return "****";
        }
        
        String prefix = token.substring(0, 4);
        String suffix = token.substring(token.length() - 4);
        return prefix + "****" + suffix;
    }
}