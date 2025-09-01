package com.puppytalk.auth.dto.request;

/**
 * 로그인 명령 DTO
 */
public record LoginCommand(
    String username,
    String password
) {
}