package com.puppytalk.user.dto.response;

import com.puppytalk.user.UserId;

/**
 * 사용자 생성 결과 DTO
 */
public record UserCreateResult(
    Long userId
) {
    public static UserCreateResult from(UserId userId) {
        return new UserCreateResult(userId.value());
    }
}