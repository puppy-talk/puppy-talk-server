package com.puppy.talk.pet.service;

import com.puppy.talk.pet.Persona;
import com.puppy.talk.pet.PersonaIdentity;
import java.util.List;

/**
 * 페르소나 관련 도메인 서비스
 * 
 * 페르소나 관리와 관련된 순수한 비즈니스 로직을 담당합니다.
 * 인프라스트럭처 세부사항에 의존하지 않습니다.
 */
public interface PersonaLookUpService {

    Persona findPersona(PersonaIdentity identity);

    List<Persona> findAllPersonas();

    List<Persona> findActivePersonas();

    Persona createPersona(Persona persona);

    void deletePersona(PersonaIdentity identity);
}