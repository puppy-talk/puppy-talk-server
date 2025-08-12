package com.puppy.talk.model.chat;

import com.puppy.talk.model.pet.PetIdentity;
import java.time.LocalDateTime;

public record ChatRoom(
    ChatRoomIdentity identity,
    PetIdentity petId,
    String roomName,
    LocalDateTime lastMessageAt
) {

    public ChatRoom {
        // identity can be null for new chat rooms before saving
        if (petId == null) {
            throw new IllegalArgumentException("PetId cannot be null");
        }
        if (roomName == null || roomName.trim().isEmpty()) {
            throw new IllegalArgumentException("Room name cannot be null or empty");
        }
        roomName = roomName.trim();
    }
}