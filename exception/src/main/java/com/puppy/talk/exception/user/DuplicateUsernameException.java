package com.puppy.talk.exception;

public class DuplicateUsernameException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateUsernameException(String username) {
        super("Username already exists: " + username);
    }

    public DuplicateUsernameException(String message, Throwable cause) {
        super(message, cause);
    }
}