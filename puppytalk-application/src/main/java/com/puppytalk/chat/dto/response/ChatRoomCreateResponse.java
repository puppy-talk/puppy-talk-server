package com.puppytalk.chat.dto.response;

import com.puppytalk.chat.ChatRoom;
import org.springframework.util.Assert;

/**
 * 채팅방 생성/조회 응답
 * Application Layer에서 사용하기 위한 응답 정보
 */
public record ChatRoomCreateResponse(
    ChatRoom chatRoom,
    boolean isNewlyCreated
) {
    public ChatRoomCreateResponse {
        Assert.notNull(chatRoom, "ChatRoom cannot be null");
    }
    
    /**
     * 새로 생성된 채팅방으로부터 응답 생성
     */
    public static ChatRoomCreateResponse created(ChatRoom chatRoom) {
        return new ChatRoomCreateResponse(chatRoom, true);
    }
    
    /**
     * 기존 채팅방으로부터 응답 생성
     */
    public static ChatRoomCreateResponse existing(ChatRoom chatRoom) {
        return new ChatRoomCreateResponse(chatRoom, false);
    }
}
