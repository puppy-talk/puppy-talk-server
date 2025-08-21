package com.puppy.talk.user;

public record User(
    UserIdentity identity,
    String username,
    String email,
    String passwordHash
) {

    public User {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        username = username.trim();
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        email = email.trim().toLowerCase(java.util.Locale.ROOT);
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        passwordHash = passwordHash.trim();
    }
}