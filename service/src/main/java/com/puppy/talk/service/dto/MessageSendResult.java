package com.puppy.talk.service.dto;

import com.puppy.talk.model.chat.ChatRoom;
import com.puppy.talk.model.chat.Message;

public record MessageSendResult(
    Message message,
    ChatRoom chatRoom
) {
}
