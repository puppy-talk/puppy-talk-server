package com.puppy.talk.service;

import com.puppy.talk.exception.user.DuplicateEmailException;
import com.puppy.talk.exception.user.DuplicateUsernameException;
import com.puppy.talk.exception.user.UserNotFoundException;
import com.puppy.talk.infrastructure.user.UserRepository;
import com.puppy.talk.model.user.User;
import com.puppy.talk.model.user.UserIdentity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLookUpService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User findUser(UserIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        return userRepository.findByIdentity(identity)
            .orElseThrow(() -> new UserNotFoundException(identity));
    }

    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        return userRepository.findByUsername(username)
            .orElseThrow(
                () -> new UserNotFoundException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (userRepository.findByUsername(user.username()).isPresent()) {
            throw new DuplicateUsernameException(user.username());
        }

        if (userRepository.findByEmail(user.email()).isPresent()) {
            throw new DuplicateEmailException(user.email());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UserIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        if (!userRepository.findByIdentity(identity).isPresent()) {
            throw new UserNotFoundException(identity);
        }
        userRepository.deleteByIdentity(identity);
    }
}