package com.puppytalk.user.dto.request;

/**
 * 사용자 조회 쿼리 DTO
 */
public record UserGetQuery(
    Long userId
) {
    public static UserGetQuery of(Long userId) {
        return new UserGetQuery(userId);
    }
}