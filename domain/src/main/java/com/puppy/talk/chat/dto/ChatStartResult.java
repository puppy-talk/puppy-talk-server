package com.puppy.talk.chat.dto;

import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.chat.Message;
import com.puppy.talk.pet.Pet;

import java.util.List;

public record ChatStartResult(
    ChatRoom chatRoom,
    Pet pet,
    List<Message> recentMessages
) {
}
