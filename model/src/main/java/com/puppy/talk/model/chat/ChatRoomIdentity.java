package com.puppy.talk.model.chat;

public record ChatRoomIdentity(Long id) {

    public ChatRoomIdentity {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
    }

    public static ChatRoomIdentity of(Long id) {
        return new ChatRoomIdentity(id);
    }
}