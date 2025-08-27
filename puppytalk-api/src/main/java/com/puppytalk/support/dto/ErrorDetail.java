package com.puppytalk.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.puppytalk.support.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * 오류 상세 정보
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(
    @Schema(description = "오류 코드", example = "VALIDATION_FAILED")
    String code,
    
    @Schema(description = "오류 메시지", example = "입력값 검증에 실패했습니다")
    String message,
    
    @Schema(description = "오류 카테고리", example = "CLIENT_ERROR")
    ErrorResponse.ErrorCategory category,
    
    @Schema(description = "필드별 상세 오류 정보")
    Map<String, List<String>> fieldErrors,
    
    @Schema(description = "오류 발생 위치 정보")
    String location,
    
    @Schema(description = "추가 메타데이터")
    Map<String, Object> metadata
) {
    
    /**
     * 간단한 오류 생성
     */
    public static ErrorDetail of(String code, String message, ErrorResponse.ErrorCategory category) {
        return new ErrorDetail(code, message, category, null, null, null);
    }
    
    /**
     * 필드 오류가 있는 오류 생성
     */
    public static ErrorDetail withFieldErrors(String code, String message, 
                                            ErrorResponse.ErrorCategory category,
                                            Map<String, List<String>> fieldErrors) {
        return new ErrorDetail(code, message, category, fieldErrors, null, null);
    }
    
    /**
     * 메타데이터가 있는 오류 생성
     */
    public static ErrorDetail withMetadata(String code, String message,
                                         ErrorResponse.ErrorCategory category,
                                         Map<String, Object> metadata) {
        return new ErrorDetail(code, message, category, null, null, metadata);
    }
}