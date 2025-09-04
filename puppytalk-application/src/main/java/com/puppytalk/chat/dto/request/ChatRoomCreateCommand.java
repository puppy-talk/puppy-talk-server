package com.puppytalk.chat.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 채팅방 생성 커맨드
 */
public record ChatRoomCreateCommand(
    @NotNull(message = "사용자 ID는 필수입니다")
    Long userId,
    
    @NotNull(message = "반려동물 ID는 필수입니다")
    Long petId
) {
    /**
     * 채팅방 생성 커맨드 정적 팩토리 메서드
     */
    public static ChatRoomCreateCommand of(Long userId, Long petId) {
        return new ChatRoomCreateCommand(userId, petId);
    }
}