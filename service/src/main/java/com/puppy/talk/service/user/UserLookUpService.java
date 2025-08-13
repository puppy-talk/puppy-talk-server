package com.puppy.talk.service.user;

import com.puppy.talk.model.user.User;
import com.puppy.talk.model.user.UserIdentity;
import java.util.List;

public interface UserLookUpService {

    User findUser(UserIdentity identity);

    User findUserByUsername(String username);

    User findUserByEmail(String email);

    List<User> findAllUsers();

    User createUser(User user);

    void deleteUser(UserIdentity identity);
}