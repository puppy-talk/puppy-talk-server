package com.puppytalk.user;

import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {
    
    @Override
    public void save(String user) {
        System.out.println("Saving user: " + user);
    }
    
    @Override
    public String findById(Long id) {
        return "User " + id;
    }
}