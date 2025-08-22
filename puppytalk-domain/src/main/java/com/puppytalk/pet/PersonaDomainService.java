package com.puppytalk.pet;

/**
 * 페르소나 도메인 서비스
 * 페르소나 관련 비즈니스 규칙과 정책을 담당
 */
public class PersonaDomainService {
    
    private final PersonaRepository personaRepository;
    
    public PersonaDomainService(PersonaRepository personaRepository) {
        if (personaRepository == null) {
            throw new IllegalArgumentException("PersonaRepository must not be null");
        }
        this.personaRepository = personaRepository;
    }
    
    /**
     * 페르소나 ID로 페르소나를 조회한다.
     * 
     * @param personaId 페르소나 ID
     * @return 페르소나
     * @throws PersonaNotFoundException 페르소나가 존재하지 않는 경우
     */
    public Persona findPersonaById(PersonaId personaId) {
        if (personaId == null) {
            throw new IllegalArgumentException("PersonaId must not be null");
        }
        
        return personaRepository.findById(personaId)
            .orElseThrow(() -> new PersonaNotFoundException(personaId));
    }
}