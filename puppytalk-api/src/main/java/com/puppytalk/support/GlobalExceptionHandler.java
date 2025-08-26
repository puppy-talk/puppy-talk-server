package com.puppytalk.support;

import com.puppytalk.activity.ActivityTrackingException;
import com.puppytalk.chat.exception.ChatRoomAccessDeniedException;
import com.puppytalk.chat.exception.ChatRoomNotFoundException;
import com.puppytalk.chat.exception.MessageNotFoundException;
import com.puppytalk.chat.exception.MessageValidationException;
import com.puppytalk.notification.NotificationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 * <p>
 * 애플리케이션에서 발생하는 모든 예외를 통합 처리하여 
 * 표준화된 오류 응답 형식을 제공합니다.
 * <p>
 * 처리하는 예외 유형:
 * - 도메인 예외 (채팅방, 메시지, 사용자, 반려동물 관련)
 * - 검증 예외 (입력값 유효성 검사, 보안 검증)
 * - HTTP 요청 예외 (파라미터, 메시지 형식, 메서드)
 * - 일반적인 런타임 예외
 * 
 * 각 예외는 추적 ID, 타임스탬프, 상세한 오류 정보를 포함한 
 * 표준화된 ErrorResponse 형식으로 반환됩니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TRACE_ID_HEADER = "X-Trace-ID";

    // === 검증 관련 예외 ===

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        Map<String, List<String>> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        });
        
        String traceId = getTraceId(request);
        log.warn("[{}] Validation failed: {}", traceId, fieldErrors);
        
        ErrorResponse errorResponse = ErrorResponse.withFieldErrors(
            traceId, 
            request.getRequestURI(), 
            request.getMethod(),
            ErrorCode.VALIDATION_FAILED.getCode(),
            ErrorCode.VALIDATION_FAILED.getMessage(),
            ErrorCode.VALIDATION_FAILED.getCategory(),
            fieldErrors
        );
        
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        Map<String, List<String>> fieldErrors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        }
        
        String traceId = getTraceId(request);
        log.warn("[{}] Constraint violation: {}", traceId, fieldErrors);
        
        ErrorResponse errorResponse = ErrorResponse.withFieldErrors(
            traceId, 
            request.getRequestURI(), 
            request.getMethod(),
            ErrorCode.VALIDATION_FAILED.getCode(),
            ErrorCode.VALIDATION_FAILED.getMessage(),
            ErrorCode.VALIDATION_FAILED.getCategory(),
            fieldErrors
        );
        
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MessageValidationException.class)
    public ResponseEntity<ErrorResponse> handleMessageValidationException(
            MessageValidationException ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.warn("[{}] Message validation failed: {} (Field: {}, Value: {})", 
                traceId, ex.getMessage(), ex.getFieldName(), ex.getRejectedValue());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fieldName", ex.getFieldName());
        metadata.put("rejectedValue", ex.getRejectedValue());
        metadata.put("validationType", ex.getValidationType());
        metadata.putAll(ex.getContext());
        
        ErrorCode errorCode = determineValidationErrorCode(ex.getValidationType());
        ErrorResponse errorResponse = ErrorResponse.withMetadata(
            traceId, 
            request.getRequestURI(), 
            request.getMethod(),
            errorCode.getCode(),
            ex.getMessage(),
            errorCode.getCategory(),
            metadata
        );
        
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    // === HTTP 요청 관련 예외 ===

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.warn("[{}] HTTP message not readable: {}", traceId, ex.getMessage());
        
        ErrorResponse errorResponse = ErrorCode.JSON_PARSE_ERROR.toErrorResponse(
            traceId, request.getRequestURI(), request.getMethod()
        );
        
        return ResponseEntity.status(ErrorCode.JSON_PARSE_ERROR.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.warn("[{}] Missing required parameter: {}", traceId, ex.getParameterName());
        
        Map<String, Object> metadata = Map.of(
            "parameterName", ex.getParameterName(),
            "parameterType", ex.getParameterType()
        );
        
        ErrorResponse errorResponse = ErrorCode.MISSING_PARAMETER.toErrorResponseWithMetadata(
            traceId, request.getRequestURI(), request.getMethod(), metadata
        );
        
        return ResponseEntity.status(ErrorCode.MISSING_PARAMETER.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.warn("[{}] Method argument type mismatch: {} (Expected: {}, Got: {})", 
                traceId, ex.getName(), ex.getRequiredType(), ex.getValue());
        
        Map<String, Object> metadata = Map.of(
            "parameterName", ex.getName(),
            "expectedType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
            "actualValue", ex.getValue() != null ? ex.getValue().toString() : "null"
        );
        
        ErrorResponse errorResponse = ErrorCode.INVALID_REQUEST.toErrorResponseWithMetadata(
            traceId, request.getRequestURI(), request.getMethod(), metadata
        );
        
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.warn("[{}] Method not supported: {} (Supported: {})", 
                traceId, ex.getMethod(), String.join(", ", ex.getSupportedMethods()));
        
        Map<String, Object> metadata = Map.of(
            "requestedMethod", ex.getMethod(),
            "supportedMethods", ex.getSupportedMethods()
        );
        
        ErrorResponse errorResponse = ErrorCode.METHOD_NOT_SUPPORTED.toErrorResponseWithMetadata(
            traceId, request.getRequestURI(), request.getMethod(), metadata
        );
        
        return ResponseEntity.status(ErrorCode.METHOD_NOT_SUPPORTED.getHttpStatus())
                .body(errorResponse);
    }

    // === 도메인 예외 ===

    @ExceptionHandler(ChatRoomNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChatRoomNotFoundException(
            ChatRoomNotFoundException ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.warn("[{}] ChatRoom not found: {}", traceId, ex.getMessage());
        
        Map<String, Object> metadata = new HashMap<>();
        if (ex.getChatRoomId() != null) {
            metadata.put("chatRoomId", ex.getChatRoomId().toString());
        }
        if (ex.getRequestedBy() != null) {
            metadata.put("requestedBy", ex.getRequestedBy().toString());
        }
        if (ex.getErrorCode() != null) {
            metadata.put("errorCode", ex.getErrorCode());
        }
        
        ErrorResponse errorResponse = ErrorCode.CHATROOM_NOT_FOUND.toErrorResponseWithMetadata(
            traceId, request.getRequestURI(), request.getMethod(), metadata
        );
        
        return ResponseEntity.status(ErrorCode.CHATROOM_NOT_FOUND.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(ChatRoomAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleChatRoomAccessDeniedException(
            ChatRoomAccessDeniedException ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.warn("[{}] ChatRoom access denied: {}", traceId, ex.toString());
        
        ErrorResponse errorResponse = ErrorCode.CHATROOM_ACCESS_DENIED.toErrorResponse(
            traceId, request.getRequestURI(), request.getMethod()
        );
        
        return ResponseEntity.status(ErrorCode.CHATROOM_ACCESS_DENIED.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MessageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotFoundException(
            MessageNotFoundException ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.warn("[{}] Message not found: {}", traceId, ex.getMessage());
        
        ErrorResponse errorResponse = ErrorCode.MESSAGE_NOT_FOUND.toErrorResponse(
            traceId, request.getRequestURI(), request.getMethod()
        );
        
        return ResponseEntity.status(ErrorCode.MESSAGE_NOT_FOUND.getHttpStatus())
                .body(errorResponse);
    }

    // === 외부 서비스 예외 ===

    @ExceptionHandler(ActivityTrackingException.class)
    public ResponseEntity<ErrorResponse> handleActivityTrackingException(
            ActivityTrackingException ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.error("[{}] Activity tracking failed: {}", traceId, ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorCode.ACTIVITY_TRACKING_FAILED.toErrorResponse(
            traceId, request.getRequestURI(), request.getMethod()
        );
        
        return ResponseEntity.status(ErrorCode.ACTIVITY_TRACKING_FAILED.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ErrorResponse> handleNotificationException(
            NotificationException ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.error("[{}] Notification failed: {}", traceId, ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorCode.NOTIFICATION_FAILED.toErrorResponse(
            traceId, request.getRequestURI(), request.getMethod()
        );
        
        return ResponseEntity.status(ErrorCode.NOTIFICATION_FAILED.getHttpStatus())
                .body(errorResponse);
    }

    // === 일반 예외 ===

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.warn("[{}] IllegalArgumentException: {}", traceId, ex.getMessage());
        
        Map<String, Object> metadata = Map.of("originalMessage", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorCode.INVALID_REQUEST.toErrorResponseWithMetadata(
            traceId, request.getRequestURI(), request.getMethod(), metadata
        );
        
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.warn("[{}] IllegalStateException: {}", traceId, ex.getMessage());
        
        Map<String, Object> metadata = Map.of("originalMessage", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorCode.INVALID_REQUEST.toErrorResponseWithMetadata(
            traceId, request.getRequestURI(), request.getMethod(), metadata
        );
        
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.error("[{}] Unexpected runtime exception: {}", traceId, ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorCode.INTERNAL_SERVER_ERROR.toErrorResponse(
            traceId, request.getRequestURI(), request.getMethod()
        );
        
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        String traceId = getTraceId(request);
        log.error("[{}] Unexpected exception: {}", traceId, ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorCode.INTERNAL_SERVER_ERROR.toErrorResponse(
            traceId, request.getRequestURI(), request.getMethod()
        );
        
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(errorResponse);
    }

    // === 유틸리티 메서드 ===

    /**
     * 요청에서 추적 ID를 추출하거나 생성합니다.
     */
    private String getTraceId(HttpServletRequest request) {
        // MDC에서 먼저 확인
        String traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isBlank()) {
            log.error("Trace ID: {}", traceId);
        }
        
        // 헤더에서 확인
        traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        
        // 새로 생성
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 검증 유형에 따라 적절한 ErrorCode를 결정합니다.
     */
    private ErrorCode determineValidationErrorCode(String validationType) {
        return switch (validationType.toUpperCase()) {
            case "SECURITY_VALIDATION" -> ErrorCode.SECURITY_VALIDATION_FAILED;
            default -> ErrorCode.VALIDATION_FAILED;
        };
    }
}