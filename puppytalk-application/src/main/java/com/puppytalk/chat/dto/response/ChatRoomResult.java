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
            chatRoom.getId().value(),
            chatRoom.getUserId().value(),
            chatRoom.getPetId().value(),
            chatRoom.getCreatedAt(),
            chatRoom.getLastMessageAt()
        );
    }
}