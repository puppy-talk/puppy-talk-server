package com.puppy.talk.ai;

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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PromptBuilder 단위 테스트")
class PromptBuilderTest {

    private PromptBuilder promptBuilder;
    private Pet testPet;
    private Persona testPersona;
    
    @BeforeEach
    void setUp() {
        promptBuilder = new PromptBuilder();
        
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
    @DisplayName("기본 프롬프트 생성 - 채팅 히스토리 없음")
    void buildPrompt_WithoutChatHistory() {
        // Given
        String userMessage = "안녕 멍멍이!";
        
        // When
        String prompt = promptBuilder.buildPrompt(testPet, testPersona, userMessage, List.of());
        
        // Then
        assertThat(prompt).contains("멍멍이");
        assertThat(prompt).contains("골든리트리버");
        assertThat(prompt).contains("3살");
        assertThat(prompt).contains("밝고 긍정적, 에너지 넘침");
        assertThat(prompt).contains("활발하고 친근한 성격의 강아지");
        assertThat(prompt).contains("당신은 활발하고 친근한 골든리트리버입니다.");
        assertThat(prompt).contains("사용자: 안녕 멍멍이!");
        assertThat(prompt).contains("멍멍이:");
        assertThat(prompt).doesNotContain("최근 대화 기록");
    }
    
    @Test
    @DisplayName("채팅 히스토리가 포함된 프롬프트 생성")
    void buildPrompt_WithChatHistory() {
        // Given
        String userMessage = "오늘 기분 어때?";
        List<Message> chatHistory = List.of(
            new Message(MessageIdentity.of(1L), ChatRoomIdentity.of(1L), SenderType.USER, "안녕!", true, LocalDateTime.now()),
            new Message(MessageIdentity.of(2L), ChatRoomIdentity.of(1L), SenderType.PET, "멍멍! 반가워!", false, LocalDateTime.now()),
            new Message(MessageIdentity.of(3L), ChatRoomIdentity.of(1L), SenderType.USER, "놀자!", true, LocalDateTime.now())
        );
        
        // When
        String prompt = promptBuilder.buildPrompt(testPet, testPersona, userMessage, chatHistory);
        
        // Then
        assertThat(prompt).contains("최근 대화 기록");
        assertThat(prompt).contains("사용자: 안녕!");
        assertThat(prompt).contains("멍멍이: 멍멍! 반가워!");
        assertThat(prompt).contains("사용자: 놀자!");
        assertThat(prompt).contains("사용자: 오늘 기분 어때?");
        assertThat(prompt).endsWith("멍멍이: ");
    }
    
    @Test
    @DisplayName("채팅 히스토리 제한 테스트 - 최근 5개만 포함")
    void buildPrompt_ChatHistoryLimit() {
        // Given
        String userMessage = "최신 메시지";
        List<Message> chatHistory = List.of(
            new Message(MessageIdentity.of(1L), ChatRoomIdentity.of(1L), SenderType.USER, "메시지1", true, LocalDateTime.now()),
            new Message(MessageIdentity.of(2L), ChatRoomIdentity.of(1L), SenderType.PET, "응답1", false, LocalDateTime.now()),
            new Message(MessageIdentity.of(3L), ChatRoomIdentity.of(1L), SenderType.USER, "메시지2", true, LocalDateTime.now()),
            new Message(MessageIdentity.of(4L), ChatRoomIdentity.of(1L), SenderType.PET, "응답2", false, LocalDateTime.now()),
            new Message(MessageIdentity.of(5L), ChatRoomIdentity.of(1L), SenderType.USER, "메시지3", true, LocalDateTime.now()),
            new Message(MessageIdentity.of(6L), ChatRoomIdentity.of(1L), SenderType.PET, "응답3", false, LocalDateTime.now()),
            new Message(MessageIdentity.of(7L), ChatRoomIdentity.of(1L), SenderType.USER, "메시지4", true, LocalDateTime.now())
        );
        
        // When
        String prompt = promptBuilder.buildPrompt(testPet, testPersona, userMessage, chatHistory);
        
        // Then
        // 최근 5개만 포함되어야 함 (메시지1, 응답1, 메시지2, 응답2, 메시지3 포함되고 응답3, 메시지4는 제외)
        assertThat(prompt).contains("메시지1");
        assertThat(prompt).contains("응답1");
        assertThat(prompt).contains("메시지2");
        assertThat(prompt).contains("응답2");
        assertThat(prompt).contains("메시지3");
        assertThat(prompt).doesNotContain("응답3");
        assertThat(prompt).doesNotContain("메시지4");
    }
    
    @Test
    @DisplayName("펫 정보가 null인 경우 기본값 처리")
    void buildPrompt_WithNullPetInfo() {
        // Given
        Pet petWithNullBreed = new Pet(
            PetIdentity.of(1L),
            UserIdentity.of(1L),
            PersonaIdentity.of(1L),
            "무명이",
            null, // breed가 null
            2,
            null
        );
        
        Persona personaWithNullInfo = new Persona(
            PersonaIdentity.of(1L),
            "기본 페르소나",
            null, // description이 null
            null, // personalityTraits가 null
            "기본 AI 프롬프트", // aiPromptTemplate은 필수
            true
        );
        
        String userMessage = "안녕!";
        
        // When
        String prompt = promptBuilder.buildPrompt(petWithNullBreed, personaWithNullInfo, userMessage, List.of());
        
        // Then
        assertThat(prompt).contains("무명이");
        assertThat(prompt).contains("귀여운 반려동물"); // 기본 breed
        assertThat(prompt).contains("친근하고 활발한"); // 기본 personalityTraits
        assertThat(prompt).contains("사랑스러운 반려동물"); // 기본 description
        assertThat(prompt).contains("기본 AI 프롬프트"); // aiPromptTemplate
        assertThat(prompt).doesNotContain("null");
    }
}
