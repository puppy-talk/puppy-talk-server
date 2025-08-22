package com.puppytalk.chat.dto.request;

import org.springframework.util.Assert;

/**
 * 채팅방 생성 커맨드
 */
public record ChatRoomCreateCommand(
    Long userId,
    Long petId
) {
    public ChatRoomCreateCommand {
        Assert.notNull(userId, "UserId cannot be null");
        Assert.notNull(petId, "PetId cannot be null");
    }
    
    /**
     * 채팅방 생성 커맨드 정적 팩토리 메서드
     */
    public static ChatRoomCreateCommand of(Long userId, Long petId) {
        return new ChatRoomCreateCommand(userId, petId);
    }
}