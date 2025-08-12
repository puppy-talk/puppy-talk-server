package com.puppy.talk.exception.user;

import com.puppy.talk.model.user.UserIdentity;

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