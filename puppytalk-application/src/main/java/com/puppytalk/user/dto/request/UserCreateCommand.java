package com.puppytalk.user.dto.request;

/**
 * 사용자 생성 명령 DTO
 */
public record UserCreateCommand(
    String username,
    String email
) {
    public static UserCreateCommand of(String username, String email) {
        return new UserCreateCommand(username, email);
    }
}