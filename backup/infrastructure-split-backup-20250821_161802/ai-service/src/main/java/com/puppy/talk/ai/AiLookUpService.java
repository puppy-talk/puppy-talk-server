package com.puppy.talk.ai;

import com.puppy.talk.pet.Persona;
import java.util.List;

/**
 * AI 관련 조회 서비스 인터페이스
 */
public interface AiLookUpService {
    
    /**
     * AI 응답을 생성합니다.
     * 
     * @param messages 대화 메시지 목록
     * @param persona 페르소나 정보
     * @return AI 생성 응답
     */
    String generateResponse(List<String> messages, Persona persona);
    
    /**
     * 비활성 알림용 메시지를 생성합니다.
     * 
     * @param messages 대화 컨텍스트
     * @param persona 페르소나 정보
     * @return 비활성 알림 메시지
     */
    String generateInactivityMessage(List<String> messages, Persona persona);
}