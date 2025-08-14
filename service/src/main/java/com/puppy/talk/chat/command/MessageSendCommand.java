package com.puppy.talk.chat.command;

public record MessageSendCommand(
    String content
) {
    public static MessageSendCommand of(String content) {
        return new MessageSendCommand(content);
    }
}
