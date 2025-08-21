package com.puppytalk.user;

public interface UserRepository {
    void save(String user);
    String findById(Long id);
}