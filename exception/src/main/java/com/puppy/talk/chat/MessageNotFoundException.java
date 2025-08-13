package com.puppy.talk.chat;

public class MessageNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MessageNotFoundException(MessageIdentity messageIdentity) {
        super(messageIdentity != null ? "Message not found with id: " + messageIdentity.id()
            : "Message not found: invalid identity");
    }

    public MessageNotFoundException(String message) {
        super(message);
    }
}