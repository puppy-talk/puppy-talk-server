package com.puppytalk.config;

import com.puppytalk.pet.PersonaDomainService;
import com.puppytalk.pet.PersonaRepository;
import com.puppytalk.pet.PetDomainService;
import com.puppytalk.pet.PetRepository;
import com.puppytalk.user.UserDomainService;
import com.puppytalk.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainAssemblyConfig {

    @Bean
    public PersonaDomainService personaDomainService(PersonaRepository personaRepository) {
        return new PersonaDomainService(personaRepository);
    }

    @Bean
    public PetDomainService petDomainService(PetRepository petRepository) {
        return new PetDomainService(petRepository);
    }

    @Bean
    public UserDomainService userDomainService(UserRepository userRepository) {
        return new UserDomainService(userRepository);
    }
}