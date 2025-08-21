package com.puppytalk.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserFacade {
    
    private final UserRepository userRepository;
    
    public UserFacade(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public void createUser(String name) {
        userRepository.save(name);
    }
    
    public String getUser(Long id) {
        return userRepository.findById(id);
    }
}