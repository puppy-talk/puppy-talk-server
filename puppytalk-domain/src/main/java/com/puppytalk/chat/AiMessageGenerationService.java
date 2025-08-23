package com.puppytalk.chat;

import com.puppytalk.pet.PetId;
import java.util.List;

/**
 * AI 메시지 생성 서비스 인터페이스
 * 
 * 반려동물의 페르소나와 채팅 히스토리를 기반으로 AI 메시지를 생성합니다.
 * Backend 관점: 외부 AI 서비스 연동을 위한 도메인 인터페이스
 */
public interface AiMessageGenerationService {
    
    /**
     * 비활성 사용자에게 보낼 반려동물 메시지 생성
     * 
     * @param petId 반려동물 ID
     * @param chatHistory 최근 채팅 히스토리 (최대 10개)
     * @param petPersona 반려동물의 페르소나 정보
     * @return 생성된 메시지 결과
     */
    AiMessageResult generateInactivityMessage(PetId petId, List<Message> chatHistory, String petPersona);
    
    /**
     * 일반적인 반려동물 응답 메시지 생성
     * 
     * @param petId 반려동물 ID
     * @param userMessage 사용자 메시지
     * @param chatHistory 최근 채팅 히스토리 (최대 5개)
     * @param petPersona 반려동물의 페르소나 정보
     * @return 생성된 메시지 결과
     */
    AiMessageResult generateResponseMessage(PetId petId, String userMessage, 
                                          List<Message> chatHistory, String petPersona);
    
    /**
     * AI 메시지 생성 결과
     */
    record AiMessageResult(
        String title,
        String content,
        boolean success,
        String errorMessage
    ) {
        
        public static AiMessageResult success(String title, String content) {
            return new AiMessageResult(title, content, true, null);
        }
        
        public static AiMessageResult failure(String errorMessage) {
            return new AiMessageResult(null, null, false, errorMessage);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public boolean hasError() {
            return !success;
        }
    }
}