package com.puppytalk.support;

/**
 * 공통 API 응답 래퍼 클래스
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        String message
) {
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }
    
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, null, message);
    }
    
    public static <T> ApiResponse<T> fail(T data, String message) {
        return new ApiResponse<>(false, data, message);
    }
}