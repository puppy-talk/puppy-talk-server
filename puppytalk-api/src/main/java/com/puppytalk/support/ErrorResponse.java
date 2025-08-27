package com.puppytalk.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.puppytalk.support.dto.ErrorDetail;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 표준화된 오류 응답 형식
 * <p>
 * 모든 API 오류 응답에 사용되는 표준 형식입니다.
 * 오류 추적, 디버깅, 클라이언트 오류 처리를 위한 정보를 포함합니다.
 * 
 * @author PuppyTalk Team
 * @since 1.0
 */
@Schema(description = "표준화된 오류 응답")
@JsonPropertyOrder({"success", "timestamp", "traceId", "error", "path", "method"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    
    @Schema(description = "성공 여부", example = "false")
    boolean success,
    
    @Schema(description = "오류 발생 시간")
    LocalDateTime timestamp,
    
    @Schema(description = "요청 추적 ID", example = "a1b2c3d4e5f6g7h8")
    String traceId,
    
    @Schema(description = "오류 정보")
    ErrorDetail error,
    
    @Schema(description = "요청 경로", example = "/api/chat/rooms/123/messages")
    String path,
    
    @Schema(description = "HTTP 메서드", example = "POST")
    String method
) {

    
    /**
     * 오류 카테고리 분류
     */
    public enum ErrorCategory {
        @Schema(description = "클라이언트 오류 (400번대)")
        CLIENT_ERROR,
        
        @Schema(description = "서버 오류 (500번대)")
        SERVER_ERROR,
        
        @Schema(description = "인증/권한 오류")
        SECURITY_ERROR,
        
        @Schema(description = "비즈니스 로직 오류")
        BUSINESS_ERROR,
        
        @Schema(description = "외부 시스템 오류")
        EXTERNAL_ERROR,
        
        @Schema(description = "검증 오류")
        VALIDATION_ERROR
    }
    
    /**
     * 기본 오류 응답 생성
     */
    public static ErrorResponse of(String traceId, String path, String method,
                                  String code, String message, ErrorCategory category) {
        return new ErrorResponse(
            false,
            LocalDateTime.now(),
            traceId,
            ErrorDetail.of(code, message, category),
            path,
            method
        );
    }
    
    /**
     * 필드 오류가 있는 응답 생성
     */
    public static ErrorResponse withFieldErrors(String traceId, String path, String method,
                                              String code, String message, ErrorCategory category,
                                              Map<String, List<String>> fieldErrors) {
        return new ErrorResponse(
            false,
            LocalDateTime.now(),
            traceId,
            ErrorDetail.withFieldErrors(code, message, category, fieldErrors),
            path,
            method
        );
    }
    
    /**
     * 메타데이터가 있는 응답 생성
     */
    public static ErrorResponse withMetadata(String traceId, String path, String method,
                                           String code, String message, ErrorCategory category,
                                           Map<String, Object> metadata) {
        return new ErrorResponse(
            false,
            LocalDateTime.now(),
            traceId,
            ErrorDetail.withMetadata(code, message, category, metadata),
            path,
            method
        );
    }
}