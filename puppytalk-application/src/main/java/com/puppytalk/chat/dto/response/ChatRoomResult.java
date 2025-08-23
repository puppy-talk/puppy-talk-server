package com.puppytalk.chat.dto.response;

import com.puppytalk.chat.ChatRoom;
import java.time.LocalDateTime;

/**
 * 채팅방 조회 결과
 */
public record ChatRoomResult(
    Long chatRoomId,
    Long userId,
    Long petId,
    LocalDateTime createdAt,
    LocalDateTime lastMessageAt
) {
    
    public static ChatRoomResult from(ChatRoom chatRoom) {
        return new ChatRoomResult(
            chatRoom.id().getValue(),
            chatRoom.userId().getValue(),
            chatRoom.petId().getValue(),
            chatRoom.createdAt(),
            chatRoom.lastMessageAt()
        );
    }
}