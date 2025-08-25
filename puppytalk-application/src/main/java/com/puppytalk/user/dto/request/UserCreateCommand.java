package com.puppytalk.user.dto.request;

/**
 * 사용자 생성 명령 DTO
 */
public record UserCreateCommand(
    String username,
    String email,
    String password
) {
    public static UserCreateCommand of(String username, String email, String password) {
        return new UserCreateCommand(username, email, password);
    }
}