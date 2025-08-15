package com.puppy.talk.event;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.pet.PetIdentity;

import java.time.LocalDateTime;

/**
 * 사용자 비활성 상태 이벤트
 * 2시간 이상 비활성 시 발생
 */
public record UserInactivityEvent(
    ChatRoomIdentity chatRoomId,
    UserIdentity userId,
    PetIdentity petId,
    String petName,
    String personaPrompt,
    LocalDateTime lastActivityAt,
    LocalDateTime inactivityThreshold
) {
    
    public static UserInactivityEvent of(
        ChatRoomIdentity chatRoomId,
        UserIdentity userId, 
        PetIdentity petId,
        String petName,
        String personaPrompt,
        LocalDateTime lastActivityAt
    ) {
        return new UserInactivityEvent(
            chatRoomId,
            userId,
            petId, 
            petName,
            personaPrompt,
            lastActivityAt,
            lastActivityAt.plusHours(2)
        );
    }
}