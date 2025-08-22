package com.puppytalk.config;

import com.puppytalk.pet.PetDomainService;
import com.puppytalk.pet.PetRepository;
import com.puppytalk.user.UserDomainService;
import com.puppytalk.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainAssemblyConfig {

    @Bean
    public PetDomainService petDomainService(PetRepository petRepository) {
        return new PetDomainService(petRepository);
    }

    @Bean
    public UserDomainService userDomainService(UserRepository userRepository) {
        return new UserDomainService(userRepository);
    }
}