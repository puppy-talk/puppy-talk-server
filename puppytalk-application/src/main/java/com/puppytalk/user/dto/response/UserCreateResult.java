package com.puppytalk.user.dto.response;

import com.puppytalk.user.User;

/**
 * 사용자 생성 결과 DTO
 */
public record UserCreateResult(
    UserResult userResult
) {
    public static UserCreateResult from(User user) {
        return new UserCreateResult(UserResult.from(user));
    }
}