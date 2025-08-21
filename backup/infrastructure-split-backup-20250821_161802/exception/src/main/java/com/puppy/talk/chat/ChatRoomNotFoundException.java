package com.puppy.talk.chat;

public class ChatRoomNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ChatRoomNotFoundException(ChatRoomIdentity chatRoomIdentity) {
        super(chatRoomIdentity != null ? "ChatRoom not found with id: " + chatRoomIdentity.id()
            : "ChatRoom not found: invalid identity");
    }

    public ChatRoomNotFoundException(String message) {
        super(message);
    }
}