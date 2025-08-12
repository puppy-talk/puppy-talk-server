package com.puppy.talk.exception.user;

public class DuplicateEmailException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateEmailException(String email) {
        super("Email already exists");
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}