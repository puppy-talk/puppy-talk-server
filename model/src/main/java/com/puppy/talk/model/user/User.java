package com.puppy.talk.model.user;

public record User(
    UserIdentity identity,
    String username,
    String email,
    String password
) {

    public User {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
    }
}