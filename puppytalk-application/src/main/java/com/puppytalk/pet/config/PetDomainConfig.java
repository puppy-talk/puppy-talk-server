package com.puppytalk.pet.config;

import com.puppytalk.pet.PetDomainService;
import com.puppytalk.pet.PetRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Pet 도메인 관련 Bean 설정
 */
@Configuration
public class PetDomainConfig {
    
    /**
     * PetDomainService Bean 등록
     */
    @Bean
    public PetDomainService petDomainService(PetRepository petRepository) {
        return new PetDomainService(petRepository);
    }
}