package com.puppy.talk.user;

public class UserNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UserNotFoundException(UserIdentity userIdentity) {
        super(userIdentity != null ? "User not found with id: " + userIdentity.id()
            : "User not found: invalid identity");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}