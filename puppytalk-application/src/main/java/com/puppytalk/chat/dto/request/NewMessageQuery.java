package com.puppytalk.chat.dto.request;

import java.time.LocalDateTime;

/**
 * 새 메시지 폴링 쿼리
 */
public record NewMessageQuery(
    Long chatRoomId,
    Long userId,
    LocalDateTime since
) {
    public static NewMessageQuery of(Long chatRoomId, Long userId, LocalDateTime since) {
        return new NewMessageQuery(chatRoomId, userId, since);
    }
    
    public NewMessageQuery {
        if (chatRoomId == null || chatRoomId <= 0) {
            throw new IllegalArgumentException("채팅방 ID는 필수이며 양수여야 합니다");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("사용자 ID는 필수이며 양수여야 합니다");
        }
        if (since == null) {
            throw new IllegalArgumentException("기준 시간은 필수입니다");
        }
    }
}