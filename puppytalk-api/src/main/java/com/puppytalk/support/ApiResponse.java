package com.puppytalk.support;

public record ApiResponse<T>(
        boolean success,
        T data,
        String message
) {
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, data, message);
    }
    
    public static <T> ApiResponse<T> success(T data, ApiSuccessMessage message) {
        return new ApiResponse<>(true, data, message.getMessage());
    }

    public static <T> ApiResponse<T> success(ApiSuccessMessage message) {
        return new ApiResponse<>(true, null, message.getMessage());
    }
    
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, null, message);
    }
    
    public static <T> ApiResponse<T> fail(T data, String message) {
        return new ApiResponse<>(false, data, message);
    }
}