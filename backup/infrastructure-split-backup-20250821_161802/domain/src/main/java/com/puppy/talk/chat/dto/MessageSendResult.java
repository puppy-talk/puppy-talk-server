package com.puppy.talk.chat.dto;

import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.chat.Message;

public record MessageSendResult(
    Message message,
    ChatRoom chatRoom
) {
}
