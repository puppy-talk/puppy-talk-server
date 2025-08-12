package com.puppy.talk.service.dto;

import com.puppy.talk.model.chat.ChatRoom;
import com.puppy.talk.model.chat.Message;
import com.puppy.talk.model.pet.Pet;

import java.util.List;

public record ChatStartResult(
    ChatRoom chatRoom,
    Pet pet,
    List<Message> recentMessages
) {
}
