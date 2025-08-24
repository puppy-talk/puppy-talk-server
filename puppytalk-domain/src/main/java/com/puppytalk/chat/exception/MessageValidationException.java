package com.puppytalk.chat.exception;

import java.util.List;
import java.util.Map;

/**
 * 메시지 유효성 검사 실패 시 발생하는 예외
 * <p>
 * 메시지 내용, 길이 검증 등에서 실패했을 때 발생합니다.
 * 검증 실패에 대한 상세한 컨텍스트 정보를 제공하여 디버깅을 돕습니다.
 * 
 * @author PuppyTalk Team
 * @since 1.0
 */
public class MessageValidationException extends RuntimeException {

    private final String fieldName;
    private final Object rejectedValue;
    private final String validationType;
    private final List<String> validationErrors;
    private final Map<String, Object> context;

    /**
     * 단일 검증 실패로 예외 생성
     */
    public MessageValidationException(String fieldName, Object rejectedValue, String message) {
        this(fieldName, rejectedValue, "CONTENT_VALIDATION", message, List.of(message), Map.of());
    }

    /**
     * 검증 타입을 포함한 예외 생성
     */
    public MessageValidationException(String fieldName, Object rejectedValue, 
                                    String validationType, String message) {
        this(fieldName, rejectedValue, validationType, message, List.of(message), Map.of());
    }

    /**
     * 다중 검증 오류로 예외 생성
     */
    public MessageValidationException(String fieldName, Object rejectedValue, 
                                    String validationType, List<String> validationErrors) {
        this(fieldName, rejectedValue, validationType, 
             String.join(", ", validationErrors), validationErrors, Map.of());
    }

    /**
     * 전체 컨텍스트 정보를 포함한 예외 생성
     */
    public MessageValidationException(String fieldName, Object rejectedValue, String validationType, 
                                    String message, List<String> validationErrors, 
                                    Map<String, Object> context) {
        super(buildMessage(fieldName, rejectedValue, validationType, message));
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
        this.validationType = validationType;
        this.validationErrors = List.copyOf(validationErrors);
        this.context = Map.copyOf(context);
    }

    private static String buildMessage(String fieldName, Object rejectedValue, 
                                     String validationType, String baseMessage) {
        return String.format("메시지 검증 실패 [field=%s, type=%s, value=%s]: %s", 
                           fieldName, validationType, 
                           rejectedValue != null ? truncateValue(rejectedValue.toString()) : "null", 
                           baseMessage);
    }

    private static String truncateValue(String value) {
        if (value.length() <= 50) {
            return value;
        }
        return value.substring(0, 47) + "...";
    }

    /**
     * 검증 실패한 필드명
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * 거부된 값
     */
    public Object getRejectedValue() {
        return rejectedValue;
    }

    /**
     * 검증 타입 (CONTENT_VALIDATION, LENGTH_VALIDATION 등)
     */
    public String getValidationType() {
        return validationType;
    }

    /**
     * 모든 검증 오류 메시지 목록
     */
    public List<String> getValidationErrors() {
        return validationErrors;
    }

    /**
     * 추가 컨텍스트 정보
     */
    public Map<String, Object> getContext() {
        return context;
    }


    /**
     * 길이 검증 실패 예외 생성 헬퍼
     */
    public static MessageValidationException lengthViolation(String fieldName, String content, 
                                                           int actualLength, int maxLength) {
        return new MessageValidationException(
            fieldName,
            content,
            "LENGTH_VALIDATION",
            String.format("메시지 길이가 초과되었습니다 (현재: %d자, 최대: %d자)", actualLength, maxLength),
            List.of(),
            Map.of("actualLength", actualLength, "maxLength", maxLength)
        );
    }


    /**
     * 디버깅용 상세 정보 반환
     */
    public String getDetailedInfo() {
        return String.format(
            "MessageValidationException[field=%s, type=%s, rejectedValue=%s, errors=%s, context=%s]",
            fieldName, validationType, rejectedValue, validationErrors, context
        );
    }
}