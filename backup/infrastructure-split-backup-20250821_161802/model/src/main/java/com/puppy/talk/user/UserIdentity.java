package com.puppy.talk.user;

public record UserIdentity(Long id) {

    public UserIdentity {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
    }

    public static UserIdentity of(Long id) {
        return new UserIdentity(id);
    }
}