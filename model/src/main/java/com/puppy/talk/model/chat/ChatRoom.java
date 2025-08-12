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
    
    /**
     * 새로운 채팅방을 생성합니다.
     */
    public static ChatRoom of(
        ChatRoomIdentity identity,
        PetIdentity petId,
        String roomName,
        LocalDateTime lastMessageAt
    ) {
        return new ChatRoom(identity, petId, roomName, lastMessageAt);
    }
    
    /**
     * 마지막 메시지 시간을 지정하여 새로운 채팅방을 생성합니다.
     */
    public static ChatRoom of(
        PetIdentity petId,
        String roomName,
        LocalDateTime lastMessageAt
    ) {
        return new ChatRoom(null, petId, roomName, lastMessageAt);
    }
    
    /**
     * 현재 시각을 마지막 메시지 시간으로 하여 새로운 채팅방을 생성합니다.
     */
    public static ChatRoom of(
        PetIdentity petId,
        String roomName
    ) {
        return new ChatRoom(null, petId, roomName, LocalDateTime.now());
    }
}