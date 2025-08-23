package com.puppytalk.chat.dto.response;

import com.puppytalk.chat.ChatRoom;
import org.springframework.util.Assert;

/**
 * 채팅방 생성 결과
 */
public record ChatRoomCreateResult(
    Long chatRoomId,
    Long userId,
    Long petId,
    boolean isNewlyCreated
) {
    public ChatRoomCreateResult {
        Assert.notNull(chatRoomId, "ChatRoomId cannot be null");
        Assert.notNull(userId, "UserId cannot be null");
        Assert.notNull(petId, "PetId cannot be null");
    }
    
    /**
     * 새로 생성된 채팅방으로부터 결과 생성
     */
    public static ChatRoomCreateResult created(ChatRoom chatRoom) {
        return new ChatRoomCreateResult(
            chatRoom.id().getValue(),
            chatRoom.userId().getValue(),
            chatRoom.petId().getValue(),
            true
        );
    }
    
    /**
     * 기존 채팅방으로부터 결과 생성
     */
    public static ChatRoomCreateResult existing(ChatRoom chatRoom) {
        return new ChatRoomCreateResult(
            chatRoom.id().getValue(),
            chatRoom.userId().getValue(),
            chatRoom.petId().getValue(),
            false
        );
    }
}