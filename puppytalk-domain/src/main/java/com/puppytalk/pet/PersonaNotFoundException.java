package com.puppytalk.pet;

/**
 * 페르소나를 찾을 수 없을 때 발생하는 예외
 */
public class PersonaNotFoundException extends RuntimeException {
    
    private final PersonaId personaId;
    
    public PersonaNotFoundException(PersonaId personaId) {
        super("페르소나를 찾을 수 없습니다. ID: " + personaId);
        this.personaId = personaId;
    }
    
    public PersonaNotFoundException(String message) {
        super(message);
        this.personaId = null;
    }
    
    public PersonaId getPersonaId() {
        return personaId;
    }
}