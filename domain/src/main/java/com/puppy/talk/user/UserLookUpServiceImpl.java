package com.puppy.talk.user;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLookUpServiceImpl implements UserLookUpService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User findUser(UserIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        return userRepository.findByIdentity(identity)
            .orElseThrow(() -> new UserNotFoundException(identity));
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        return userRepository.findByUsername(username)
            .orElseThrow(
                () -> new UserNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
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

    @Override
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
