package com.puppy.talk.controller.push.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 디바이스 토큰 등록 요청 DTO
 */
@Schema(description = "디바이스 토큰 등록 요청")
public record DeviceTokenRequest(
    
    @Schema(description = "디바이스 토큰", example = "dGhpcyBpcyBhIG1vY2sgdG9rZW4=", required = true)
    String token,
    
    @Schema(description = "디바이스 ID", example = "device-12345")
    String deviceId,
    
    @Schema(description = "플랫폼", example = "android", allowableValues = {"ios", "android", "web"}, required = true)
    String platform
) {}