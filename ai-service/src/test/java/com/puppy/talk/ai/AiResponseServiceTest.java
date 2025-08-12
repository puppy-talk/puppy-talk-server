package com.puppy.talk.ai;

import com.puppy.talk.ai.provider.AiProvider;
import com.puppy.talk.ai.provider.AiProviderFactory;
import com.puppy.talk.ai.provider.AiRequest;
import com.puppy.talk.ai.provider.AiResponse;
import com.puppy.talk.model.chat.Message;
import com.puppy.talk.model.chat.MessageIdentity;
import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.chat.SenderType;
import com.puppy.talk.model.pet.Pet;
import com.puppy.talk.model.pet.PetIdentity;
import com.puppy.talk.model.pet.Persona;
import com.puppy.talk.model.pet.PersonaIdentity;
import com.puppy.talk.model.user.UserIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AiResponseService 단위 테스트")
class AiResponseServiceTest {

    private AiResponseService aiResponseService;
    private MockAiProviderFactory mockProviderFactory;
    private MockAiProvider mockAiProvider;
    private PromptBuilder promptBuilder;
    
    private Pet testPet;
    private Persona testPersona;
    
    @BeforeEach
    void setUp() {
        mockAiProvider = new MockAiProvider();
        mockProviderFactory = new MockAiProviderFactory(mockAiProvider);
        promptBuilder = new PromptBuilder();
        
        // AiResponseService에 reflection으로 설정값 주입
        aiResponseService = new AiResponseService(mockProviderFactory, promptBuilder);
        setField(aiResponseService, "defaultModel", "mock-model");
        setField(aiResponseService, "maxTokens", 150);
        setField(aiResponseService, "temperature", 0.8);
        
        testPet = new Pet(
            PetIdentity.of(1L),
            UserIdentity.of(1L),
            PersonaIdentity.of(1L),
            "멍멍이",
            "골든리트리버",
            3,
            "http://example.com/image.jpg"
        );
        
        testPersona = new Persona(
            PersonaIdentity.of(1L),
            "친근한 골든리트리버",
            "활발하고 친근한 성격의 강아지",
            "밝고 긍정적, 에너지 넘침",
            "당신은 활발하고 친근한 골든리트리버입니다.",
            true
        );
    }
    
    @Test
    @DisplayName("성공: 정상적인 AI 응답 생성")
    void generatePetResponse_Success() {
        // Given
        String userMessage = "안녕 멍멍이!";
        String expectedResponse = "안녕하세요! 꼬리를 흔들흔들~ 🐕";
        mockAiProvider.setMockResponse(expectedResponse);
        
        // When
        String result = aiResponseService.generatePetResponse(testPet, testPersona, userMessage, List.of());
        
        // Then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(mockAiProvider.wasGenerateResponseCalled()).isTrue();
    }
    
    @Test
    @DisplayName("성공: 채팅 히스토리가 있는 경우 AI 응답 생성")
    void generatePetResponse_WithChatHistory() {
        // Given
        String userMessage = "오늘 기분이 어때?";
        List<Message> chatHistory = List.of(
            new Message(MessageIdentity.of(1L), ChatRoomIdentity.of(1L), SenderType.USER, "안녕!", true, LocalDateTime.now()),
            new Message(MessageIdentity.of(2L), ChatRoomIdentity.of(1L), SenderType.PET, "멍멍!", false, LocalDateTime.now())
        );
        String expectedResponse = "오늘 정말 기분 좋아요! 🐾";
        mockAiProvider.setMockResponse(expectedResponse);
        
        // When
        String result = aiResponseService.generatePetResponse(testPet, testPersona, userMessage, chatHistory);
        
        // Then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(mockAiProvider.getLastRequest().prompt()).contains("최근 대화 기록");
    }
    
    @Test
    @DisplayName("실패 시 대체 응답 생성: AI 서비스 오류")
    void generatePetResponse_FallbackOnError() {
        // Given
        String userMessage = "안녕!";
        mockAiProvider.setThrowException(true);
        
        // When
        String result = aiResponseService.generatePetResponse(testPet, testPersona, userMessage, List.of());
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("멍멍이");
        assertThat(result).matches(".*[🤔❤️🐕🐾].*"); // 이모지 포함 확인
    }
    
    @Test
    @DisplayName("실패: null pet으로 응답 생성 시도")
    void generatePetResponse_NullPet() {
        // When & Then
        assertThatThrownBy(() -> 
            aiResponseService.generatePetResponse(null, testPersona, "안녕!", List.of())
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Pet cannot be null");
    }
    
    @Test
    @DisplayName("실패: null persona로 응답 생성 시도")
    void generatePetResponse_NullPersona() {
        // When & Then
        assertThatThrownBy(() -> 
            aiResponseService.generatePetResponse(testPet, null, "안녕!", List.of())
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Persona cannot be null");
    }
    
    @Test
    @DisplayName("실패: null 또는 빈 메시지로 응답 생성 시도")
    void generatePetResponse_NullOrEmptyMessage() {
        // When & Then
        assertThatThrownBy(() -> 
            aiResponseService.generatePetResponse(testPet, testPersona, null, List.of())
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("User message cannot be null or empty");
        
        assertThatThrownBy(() -> 
            aiResponseService.generatePetResponse(testPet, testPersona, "", List.of())
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("User message cannot be null or empty");
        
        assertThatThrownBy(() -> 
            aiResponseService.generatePetResponse(testPet, testPersona, "   ", List.of())
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("User message cannot be null or empty");
    }

    // Reflection 헬퍼 메서드
    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    // Mock 구현체들
    static class MockAiProvider implements AiProvider {
        private String mockResponse = "기본 AI 응답";
        private boolean throwException = false;
        private boolean generateResponseCalled = false;
        private AiRequest lastRequest;
        
        @Override
        public String getProviderName() {
            return "mock-provider";
        }
        
        @Override
        public String[] getSupportedModels() {
            return new String[]{"mock-model"};
        }
        
        @Override
        public AiResponse generateResponse(AiRequest request) throws AiResponseException {
            generateResponseCalled = true;
            lastRequest = request;
            
            if (throwException) {
                throw new AiResponseException("Mock AI provider error");
            }
            
            return AiResponse.create(mockResponse, request.model(), getProviderName());
        }
        
        @Override
        public boolean isHealthy() {
            return !throwException;
        }
        
        @Override
        public boolean isEnabled() {
            return true;
        }
        
        public void setMockResponse(String response) {
            this.mockResponse = response;
        }
        
        public void setThrowException(boolean throwException) {
            this.throwException = throwException;
        }
        
        public boolean wasGenerateResponseCalled() {
            return generateResponseCalled;
        }
        
        public AiRequest getLastRequest() {
            return lastRequest;
        }
    }
    
    static class MockAiProviderFactory extends AiProviderFactory {
        private final AiProvider mockProvider;
        
        public MockAiProviderFactory(AiProvider mockProvider) {
            super(List.of(mockProvider));
            this.mockProvider = mockProvider;
        }
        
        @Override
        public AiProvider getAvailableProvider() {
            return mockProvider;
        }
    }
}
