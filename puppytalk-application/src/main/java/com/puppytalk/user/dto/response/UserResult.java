package com.puppytalk.user.dto.response;

import com.puppytalk.user.User;
import java.time.LocalDateTime;

/**
 * 사용자 결과 DTO
 */
public record UserResult(
    Long userId,
    String username,
    String email,
    boolean isDeleted,
    LocalDateTime createdAt
) {
    public static UserResult from(User user) {
        return new UserResult(
            user.id().value(),
            user.username(),
            user.email(),
            user.isDeleted(),
            user.createdAt()
        );
    }
}