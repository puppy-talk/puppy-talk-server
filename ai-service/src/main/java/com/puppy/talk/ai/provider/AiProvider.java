package com.puppy.talk.ai.provider;

import com.puppy.talk.ai.AiResponseException;

/**
 * AI 모델 제공업체의 공통 인터페이스
 * 
 * 다양한 AI 서비스 (gpt-oss, OpenAI, Claude, Gemini 등)를 
 * 통일된 인터페이스로 사용할 수 있도록 추상화합니다.
 */
public interface AiProvider {

    /**
     * AI 제공업체의 고유 이름을 반환합니다.
     * 
     * @return 제공업체 이름 (예: "gpt-oss", "openai", "claude", "gemini")
     */
    String getProviderName();

    /**
     * 사용 가능한 모델 목록을 반환합니다.
     * 
     * @return 모델 이름 배열
     */
    String[] getSupportedModels();

    /**
     * AI 모델을 사용하여 응답을 생성합니다.
     * 
     * @param request AI 요청 정보
     * @return AI 응답 결과
     * @throws AiResponseException AI 응답 생성 실패 시
     */
    AiResponse generateResponse(AiRequest request) throws AiResponseException;

    /**
     * AI 서비스의 상태를 확인합니다.
     * 
     * @return 서비스가 정상 동작하면 true, 그렇지 않으면 false
     */
    boolean isHealthy();

    /**
     * AI 제공업체가 현재 활성화되어 있는지 확인합니다.
     * 
     * @return 활성화 상태이면 true, 그렇지 않으면 false
     */
    boolean isEnabled();
}
