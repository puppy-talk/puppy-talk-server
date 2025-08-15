package com.puppy.talk.event;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.pet.PetIdentity;

import java.time.LocalDateTime;

/**
 * 채팅방 생성 이벤트
 * Pet 등록 시 자동으로 채팅방이 생성될 때 발생
 */
public record ChatRoomCreatedEvent(
    ChatRoomIdentity chatRoomId,
    UserIdentity userId,
    PetIdentity petId,
    String chatRoomName,
    LocalDateTime createdAt
) {
    
    public static ChatRoomCreatedEvent of(
        ChatRoomIdentity chatRoomId,
        UserIdentity userId,
        PetIdentity petId,
        String chatRoomName
    ) {
        return new ChatRoomCreatedEvent(
            chatRoomId,
            userId,
            petId,
            chatRoomName,
            LocalDateTime.now()
        );
    }
}