package com.puppy.talk.chat.dto.response;

import com.puppy.talk.pet.dto.response.PetResponse;
import com.puppy.talk.dto.ChatStartResult;

import java.time.LocalDateTime;
import java.util.List;

public record ChatStartResponse(
    Long chatRoomId,
    String chatRoomName,
    LocalDateTime lastMessageAt,
    PetResponse pet,
    List<MessageResponse> recentMessages
) {

    public static ChatStartResponse from(ChatStartResult result) {
        return new ChatStartResponse(
            result.chatRoom().identity().id(),
            result.chatRoom().roomName(),
            result.chatRoom().lastMessageAt(),
            PetResponse.from(result.pet()),
            result.recentMessages().stream()
                .map(MessageResponse::from)
                .toList()
        );
    }
}
