package com.puppytalk.support;

import org.springframework.http.HttpStatus;

/**
 * 표준화된 오류 코드 정의
 * <p>
 * 애플리케이션에서 사용하는 모든 오류 코드를 중앙 집중 관리합니다.
 * HTTP 상태 코드, 오류 카테고리, 메시지와 함께 정의됩니다.
 * 
 * @author PuppyTalk Team
 * @since 1.0
 */
public enum ErrorCode {

    // === 일반적인 오류 ===
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", 
                         "내부 서버 오류가 발생했습니다", 
                         HttpStatus.INTERNAL_SERVER_ERROR, 
                         ErrorResponse.ErrorCategory.SERVER_ERROR),
    
    INVALID_REQUEST("INVALID_REQUEST", 
                   "잘못된 요청입니다", 
                   HttpStatus.BAD_REQUEST, 
                   ErrorResponse.ErrorCategory.CLIENT_ERROR),
    
    MISSING_PARAMETER("MISSING_PARAMETER", 
                     "필수 파라미터가 누락되었습니다", 
                     HttpStatus.BAD_REQUEST, 
                     ErrorResponse.ErrorCategory.CLIENT_ERROR),

    // === 검증 관련 오류 ===
    VALIDATION_FAILED("VALIDATION_FAILED", 
                     "입력값 검증에 실패했습니다", 
                     HttpStatus.BAD_REQUEST, 
                     ErrorResponse.ErrorCategory.VALIDATION_ERROR),
    
    SECURITY_VALIDATION_FAILED("SECURITY_VALIDATION_FAILED", 
                              "보안 검증에 실패했습니다", 
                              HttpStatus.BAD_REQUEST, 
                              ErrorResponse.ErrorCategory.SECURITY_ERROR),
    

    // === 채팅방 관련 오류 ===
    CHATROOM_NOT_FOUND("CHATROOM_NOT_FOUND", 
                      "채팅방을 찾을 수 없습니다", 
                      HttpStatus.NOT_FOUND, 
                      ErrorResponse.ErrorCategory.BUSINESS_ERROR),
    
    CHATROOM_ACCESS_DENIED("CHATROOM_ACCESS_DENIED", 
                          "채팅방에 접근할 권한이 없습니다", 
                          HttpStatus.FORBIDDEN, 
                          ErrorResponse.ErrorCategory.SECURITY_ERROR),
    
    CHATROOM_CREATION_FAILED("CHATROOM_CREATION_FAILED", 
                            "채팅방 생성에 실패했습니다", 
                            HttpStatus.BAD_REQUEST, 
                            ErrorResponse.ErrorCategory.BUSINESS_ERROR),

    // === 메시지 관련 오류 ===
    MESSAGE_NOT_FOUND("MESSAGE_NOT_FOUND", 
                     "메시지를 찾을 수 없습니다", 
                     HttpStatus.NOT_FOUND, 
                     ErrorResponse.ErrorCategory.BUSINESS_ERROR),
    
    MESSAGE_TOO_LONG("MESSAGE_TOO_LONG", 
                    "메시지가 너무 깁니다", 
                    HttpStatus.BAD_REQUEST, 
                    ErrorResponse.ErrorCategory.VALIDATION_ERROR),
    
    MESSAGE_EMPTY("MESSAGE_EMPTY", 
                 "메시지 내용이 비어있습니다", 
                 HttpStatus.BAD_REQUEST, 
                 ErrorResponse.ErrorCategory.VALIDATION_ERROR),
    
    MESSAGE_SEND_FAILED("MESSAGE_SEND_FAILED", 
                       "메시지 전송에 실패했습니다", 
                       HttpStatus.INTERNAL_SERVER_ERROR, 
                       ErrorResponse.ErrorCategory.SERVER_ERROR),

    // === 사용자 관련 오류 ===
    USER_NOT_FOUND("USER_NOT_FOUND", 
                  "사용자를 찾을 수 없습니다", 
                  HttpStatus.NOT_FOUND, 
                  ErrorResponse.ErrorCategory.BUSINESS_ERROR),
    
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", 
                       "이미 존재하는 사용자입니다", 
                       HttpStatus.CONFLICT, 
                       ErrorResponse.ErrorCategory.BUSINESS_ERROR),

    // === 반려동물 관련 오류 ===
    PET_NOT_FOUND("PET_NOT_FOUND", 
                 "반려동물을 찾을 수 없습니다", 
                 HttpStatus.NOT_FOUND, 
                 ErrorResponse.ErrorCategory.BUSINESS_ERROR),
    
    PET_CREATION_FAILED("PET_CREATION_FAILED", 
                       "반려동물 생성에 실패했습니다", 
                       HttpStatus.BAD_REQUEST, 
                       ErrorResponse.ErrorCategory.BUSINESS_ERROR),

    // === 알림 관련 오류 ===
    NOTIFICATION_NOT_FOUND("NOTIFICATION_NOT_FOUND", 
                          "알림을 찾을 수 없습니다", 
                          HttpStatus.NOT_FOUND, 
                          ErrorResponse.ErrorCategory.BUSINESS_ERROR),
    
    NOTIFICATION_FAILED("NOTIFICATION_FAILED", 
                       "알림 전송에 실패했습니다", 
                       HttpStatus.INTERNAL_SERVER_ERROR, 
                       ErrorResponse.ErrorCategory.EXTERNAL_ERROR),
    
    NOTIFICATION_SETTINGS_INVALID("NOTIFICATION_SETTINGS_INVALID", 
                                 "알림 설정이 올바르지 않습니다", 
                                 HttpStatus.BAD_REQUEST, 
                                 ErrorResponse.ErrorCategory.VALIDATION_ERROR),

    // === 활동 추적 관련 오류 ===
    ACTIVITY_TRACKING_FAILED("ACTIVITY_TRACKING_FAILED", 
                            "활동 추적에 실패했습니다", 
                            HttpStatus.INTERNAL_SERVER_ERROR, 
                            ErrorResponse.ErrorCategory.SERVER_ERROR),

    // === 보안 관련 오류 ===
    
    INVALID_SECURITY_HEADERS("INVALID_SECURITY_HEADERS", 
                           "보안 헤더가 올바르지 않습니다", 
                           HttpStatus.BAD_REQUEST, 
                           ErrorResponse.ErrorCategory.SECURITY_ERROR),

    // === JSON/HTTP 관련 오류 ===
    JSON_PARSE_ERROR("JSON_PARSE_ERROR", 
                    "JSON 형식이 올바르지 않습니다", 
                    HttpStatus.BAD_REQUEST, 
                    ErrorResponse.ErrorCategory.CLIENT_ERROR),
    
    INVALID_HTTP_MESSAGE("INVALID_HTTP_MESSAGE", 
                        "HTTP 메시지 형식이 올바르지 않습니다", 
                        HttpStatus.BAD_REQUEST, 
                        ErrorResponse.ErrorCategory.CLIENT_ERROR),
    
    METHOD_NOT_SUPPORTED("METHOD_NOT_SUPPORTED", 
                        "지원하지 않는 HTTP 메서드입니다", 
                        HttpStatus.METHOD_NOT_ALLOWED, 
                        ErrorResponse.ErrorCategory.CLIENT_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    private final ErrorResponse.ErrorCategory category;

    ErrorCode(String code, String message, HttpStatus httpStatus, ErrorResponse.ErrorCategory category) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
        this.category = category;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ErrorResponse.ErrorCategory getCategory() {
        return category;
    }

    /**
     * 오류 코드로 ErrorResponse 생성
     */
    public ErrorResponse toErrorResponse(String traceId, String path, String method) {
        return ErrorResponse.of(traceId, path, method, this.code, this.message, this.category);
    }

    /**
     * 메타데이터와 함께 ErrorResponse 생성
     */
    public ErrorResponse toErrorResponseWithMetadata(String traceId, String path, String method,
                                                   java.util.Map<String, Object> metadata) {
        return ErrorResponse.withMetadata(traceId, path, method, this.code, this.message, this.category, metadata);
    }
}