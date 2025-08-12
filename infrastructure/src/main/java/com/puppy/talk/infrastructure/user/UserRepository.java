package com.puppy.talk.infrastructure.user;

import com.puppy.talk.model.user.User;
import com.puppy.talk.model.user.UserIdentity;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByIdentity(UserIdentity identity);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    User save(User user);

    void deleteByIdentity(UserIdentity identity);
}