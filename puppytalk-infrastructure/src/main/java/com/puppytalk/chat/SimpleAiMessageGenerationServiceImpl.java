package com.puppytalk.chat;

import com.puppytalk.pet.PetId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 간단한 AI 메시지 생성 서비스 구현체
 * 
 * Backend 관점: 실제 AI 서비스 연동 전 테스트용 구현
 * 추후 OpenAI API, AWS Bedrock 등 실제 AI 서비스로 교체 예정
 */
@Service
public class SimpleAiMessageGenerationServiceImpl implements AiMessageGenerationService {
    
    private final Random random = new Random();
    
    // 비활성 사용자용 메시지 템플릿
    private static final String[] INACTIVITY_TITLES = {
        "보고 싶어요! 🐕",
        "어디 계세요? 🥺",
        "함께 놀아요! 🎾",
        "안녕하세요! 😊"
    };
    
    private static final String[] INACTIVITY_MESSAGES = {
        "주인님, 오랜만이에요! 오늘 하루 어떻게 보내셨나요?",
        "심심해서 기다리고 있었어요. 함께 대화해요!",
        "주인님이 보고 싶어서 메시지를 보냈어요. 안녕하세요!",
        "오늘도 좋은 하루 보내고 계신가요? 저는 주인님이 그리워요!",
        "혹시 바쁘신가요? 시간 되실 때 대화해요!"
    };
    
    @Override
    public AiMessageResult generateInactivityMessage(PetId petId, List<Message> chatHistory, String petPersona) {
        try {
            // 간단한 규칙 기반 메시지 생성
            String title = getRandomTitle();
            String content = generateInactivityContent(chatHistory, petPersona);
            
            return AiMessageResult.success(title, content);
            
        } catch (Exception e) {
            return AiMessageResult.failure("메시지 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public AiMessageResult generateResponseMessage(PetId petId, String userMessage, 
                                                  List<Message> chatHistory, String petPersona) {
        try {
            // 사용자 메시지에 대한 간단한 응답 생성
            String title = "답장이에요! 🐾";
            String content = generateResponseContent(userMessage, petPersona);
            
            return AiMessageResult.success(title, content);
            
        } catch (Exception e) {
            return AiMessageResult.failure("응답 메시지 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private String getRandomTitle() {
        return INACTIVITY_TITLES[random.nextInt(INACTIVITY_TITLES.length)];
    }
    
    private String generateInactivityContent(List<Message> chatHistory, String petPersona) {
        StringBuilder content = new StringBuilder();
        
        // 기본 인사 메시지
        String baseMessage = INACTIVITY_MESSAGES[random.nextInt(INACTIVITY_MESSAGES.length)];
        content.append(baseMessage);
        
        // 채팅 히스토리가 있으면 참고하여 개인화
        if (chatHistory != null && !chatHistory.isEmpty()) {
            Message lastMessage = chatHistory.get(0); // 최신 메시지
            String timeInfo = formatTimeAgo(lastMessage.createdAt());
            content.append(" ").append(timeInfo).append(" 이후로 소식이 없으셨네요!");
        }
        
        // 페르소나 반영 (간단한 키워드 매칭)
        if (petPersona != null && !petPersona.trim().isEmpty()) {
            if (petPersona.toLowerCase().contains("활발") || petPersona.toLowerCase().contains("energetic")) {
                content.append(" 저는 오늘도 에너지 넘쳐요! 🎾");
            } else if (petPersona.toLowerCase().contains("차분") || petPersona.toLowerCase().contains("calm")) {
                content.append(" 저는 조용히 기다리고 있었어요. 😌");
            } else if (petPersona.toLowerCase().contains("귀여운") || petPersona.toLowerCase().contains("cute")) {
                content.append(" 귀여운 저를 잊지 마세요! 🥰");
            }
        }
        
        return content.toString();
    }
    
    private String generateResponseContent(String userMessage, String petPersona) {
        // 사용자 메시지에 대한 간단한 규칙 기반 응답
        if (userMessage.contains("안녕")) {
            return "안녕하세요! 오늘도 좋은 하루 보내고 계신가요?";
        } else if (userMessage.contains("고마워") || userMessage.contains("감사")) {
            return "천만에요! 언제든지 저와 함께해주세요! 😊";
        } else if (userMessage.contains("놀자") || userMessage.contains("놀아")) {
            return "좋아요! 무엇을 하고 놀까요? 🎾";
        } else {
            return "그렇군요! 더 자세히 알려주세요. 저도 궁금해요! 🐕";
        }
    }
    
    private String formatTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long hoursAgo = java.time.Duration.between(dateTime, now).toHours();
        
        if (hoursAgo < 1) {
            return "조금 전";
        } else if (hoursAgo < 24) {
            return hoursAgo + "시간 전";
        } else {
            long daysAgo = hoursAgo / 24;
            return daysAgo + "일 전";
        }
    }
}