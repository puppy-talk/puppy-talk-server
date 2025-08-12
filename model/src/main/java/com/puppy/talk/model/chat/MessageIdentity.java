package com.puppy.talk.model.chat;

public record MessageIdentity(Long id) {

    public MessageIdentity {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
    }

    public static MessageIdentity of(Long id) {
        return new MessageIdentity(id);
    }
}