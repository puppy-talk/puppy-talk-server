package com.puppytalk.config;

import com.puppytalk.pet.PersonaDomainService;
import com.puppytalk.pet.PersonaRepository;
import com.puppytalk.pet.PetDomainService;
import com.puppytalk.pet.PetRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain Service Bean 설정
 * Infrastructure 계층에서 Domain Service를 Bean으로 등록
 */
@Configuration
public class DomainServiceConfig {
    
    /**
     * PersonaDomainService Bean 등록
     */
    @Bean
    public PersonaDomainService personaDomainService(PersonaRepository personaRepository) {
        return new PersonaDomainService(personaRepository);
    }
    
    /**
     * PetDomainService Bean 등록
     */
    @Bean
    public PetDomainService petDomainService(PetRepository petRepository) {
        return new PetDomainService(petRepository);
    }
}