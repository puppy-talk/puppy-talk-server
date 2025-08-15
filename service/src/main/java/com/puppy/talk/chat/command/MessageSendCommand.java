package com.puppy.talk.chat.command;

public record MessageSendCommand(
    String content
) {
    public MessageSendCommand {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }
        if (content.length() > 2000) {
            throw new IllegalArgumentException("Message content cannot exceed 2000 characters");
        }
        
        // Trim whitespace for consistency
        content = content.trim();
    }
    
    public static MessageSendCommand of(String content) {
        return new MessageSendCommand(content);
    }
}
