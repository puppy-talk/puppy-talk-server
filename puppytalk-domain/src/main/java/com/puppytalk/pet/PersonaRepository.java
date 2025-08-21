package com.puppytalk.pet;

import java.util.List;
import java.util.Optional;

/**
 * 페르소나 저장소 인터페이스
 */
public interface PersonaRepository {
    
    /**
     * 페르소나 저장
     */
    Persona save(Persona persona);
    
    /**
     * ID로 페르소나 조회
     */
    Optional<Persona> findById(PersonaId id);
    
    /**
     * 모든 사용 가능한 페르소나 조회
     */
    List<Persona> findAllAvailable();
    
    /**
     * 이름으로 페르소나 조회
     */
    Optional<Persona> findByName(String name);
    
    /**
     * 페르소나 존재 여부 확인
     */
    boolean existsById(PersonaId id);
    
    /**
     * 페르소나 이름 중복 확인
     */
    boolean existsByName(String name);
}