package com.puppy.talk.auth;

import com.puppy.talk.user.User;

/**
 * 인증 결과를 담는 레코드
 */
public record AuthResult(
    String token,
    User user
) {}