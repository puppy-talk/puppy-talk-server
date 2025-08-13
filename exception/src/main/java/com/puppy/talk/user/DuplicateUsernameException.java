package com.puppy.talk.user;

public class DuplicateUsernameException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateUsernameException(String username) {
        super("Username already exists");
    }

    public DuplicateUsernameException(String message, Throwable cause) {
        super(message, cause);
    }
}