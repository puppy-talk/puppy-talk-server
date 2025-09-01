package com.puppytalk.auth.dto.request;

import com.puppytalk.user.UserId;

/**
 * 로그아웃 명령 DTO
 */
public record LogoutCommand(
    UserId userId,
    String accessToken,
    boolean logoutAll  // true면 모든 디바이스에서 로그아웃, false면 현재 토큰만
) {
    
    public static LogoutCommand singleLogout(UserId userId, String accessToken) {
        return new LogoutCommand(userId, accessToken, false);
    }
    
    public static LogoutCommand logoutAll(UserId userId) {
        return new LogoutCommand(userId, null, true);
    }
}