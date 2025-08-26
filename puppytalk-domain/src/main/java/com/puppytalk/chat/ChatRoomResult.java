package com.puppytalk.chat;

public record ChatRoomResult(
    ChatRoom chatRoom,
    boolean isCreated
) {
    public static ChatRoomResult created(ChatRoom chatRoom) {
        return new ChatRoomResult(chatRoom, true);
    }

    public static ChatRoomResult existing(ChatRoom chatRoom) {
        return new ChatRoomResult(chatRoom, false);
    }
}