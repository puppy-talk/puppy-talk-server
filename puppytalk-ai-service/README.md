# PuppyTalk AI Service

AI 서비스는 Grok API를 사용하여 반려동물과의 채팅을 생성하는 Python 기반 마이크로서비스입니다.

## 🏗️ 아키텍처

```
puppytalk-ai-service/
├── app/
│   ├── api/v1/           # REST API 엔드포인트
│   │   ├── endpoints/    # 개별 엔드포인트 모듈
│   │   └── api.py        # API 라우터 구성
│   ├── core/             # 핵심 설정 및 예외 처리
│   ├── models/           # 요청/응답 모델 (Pydantic)
│   ├── services/         # 비즈니스 로직 (Grok API 통합)
│   ├── utils/           # 유틸리티 함수
│   └── main.py          # FastAPI 애플리케이션 진입점
├── requirements.txt     # Python 의존성
├── Dockerfile          # Docker 빌드 설정
├── docker-compose.yml  # 로컬 개발 환경
└── .env.example        # 환경 변수 예시
```

## 🚀 주요 기능

### 1. AI 펫 채팅 생성
- **엔드포인트**: `POST /api/v1/chat/generate`
- **기능**: 사용자 메시지를 바탕으로 반려동물의 응답 생성
- **특징**:
  - 페르소나 기반 응답 생성
  - 대화 히스토리 고려
  - 한국어 자연스러운 대화
  - 이모지 포함 감정 표현

### 2. 비활성 알림 생성
- **엔드포인트**: `POST /api/v1/chat/inactivity-notification`
- **기능**: 사용자가 오랫동안 접속하지 않았을 때 보내는 알림 메시지
- **특징**:
  - 시간대별 상황 고려
  - 마지막 대화 맥락 반영
  - 부담스럽지 않은 귀여운 메시지

### 3. 헬스체크 및 모니터링
- **엔드포인트**: `GET /api/v1/health`
- **기능**: 서비스 상태 및 의존성 확인
- **특징**:
  - Grok API 연결 상태 확인
  - 시스템 리소스 모니터링
  - 상세한 메트릭 제공

## 🛠️ 기술 스택

- **Framework**: FastAPI 0.104+
- **HTTP Client**: httpx (비동기)
- **Validation**: Pydantic v2
- **Logging**: structlog
- **Retry**: tenacity
- **Monitoring**: psutil
- **Runtime**: Python 3.11+

## 📦 설치 및 실행

### 1. 환경 설정

```bash
# 환경 변수 파일 생성
cp .env.example .env

# 필수 환경 변수 설정
export GROK_API_KEY="your-grok-api-key"
export SECRET_KEY="your-secret-key"
```

### 2. 로컬 개발 실행

```bash
# 의존성 설치
pip install -r requirements.txt

# 개발 서버 실행
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8001
```

### 3. Docker 실행

```bash
# Docker 빌드 및 실행
docker-compose up --build

# 백그라운드 실행
docker-compose up -d
```

### 4. 헬스체크 확인

```bash
curl http://localhost:8001/api/v1/health
```

## 🔧 API 문서

### 채팅 생성

```http
POST /api/v1/chat/generate
Content-Type: application/json

{
  "user_id": 1,
  "pet_id": 123,
  "chat_room_id": 456,
  "user_message": "안녕 멍멍이야!",
  "pet_persona": {
    "type": "playful",
    "name": "멍멍이",
    "breed": "골든리트리버",
    "age": 3,
    "personality_traits": ["활발한", "친근한", "장난끼많은"]
  },
  "conversation_history": [
    {
      "role": "user",
      "content": "오늘 날씨가 좋네"
    },
    {
      "role": "assistant", 
      "content": "정말요! 산책 나가고 싶어요 🐕"
    }
  ],
  "max_tokens": 150,
  "temperature": 0.8
}
```

### 응답

```json
{
  "success": true,
  "message_id": "msg_123",
  "content": "안녕하세요! 오늘도 함께 놀아요! 🐾✨",
  "model": "grok-beta",
  "tokens_used": 25,
  "generation_time_ms": 847,
  "conversation_id": "456_1703123456",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### 비활성 알림 생성

```http
POST /api/v1/chat/inactivity-notification
Content-Type: application/json

{
  "user_id": 1,
  "pet_id": 123, 
  "chat_room_id": 456,
  "pet_persona": {
    "type": "calm",
    "name": "냥이",
    "personality_traits": ["조용한", "다정한"]
  },
  "hours_since_last_activity": 6,
  "time_of_day": "evening",
  "last_messages": [
    {
      "role": "user",
      "content": "나중에 다시 와서 놀아줄게"
    }
  ]
}
```

## 🔒 보안

- API 키 환경 변수로 관리
- 요청/응답 로깅에서 민감 정보 마스킹
- 입력 값 검증
- CORS 설정으로 허용된 도메인만 접근

## 📊 로깅 및 헬스체크

### 로깅
- 구조화된 JSON 로깅
- 요청 추적 ID
- 에러 스택 트레이스

### 헬스체크
- Liveness Probe: `/api/v1/health/liveness`
- Readiness Probe: `/api/v1/health/readiness`
- 기본 헬스체크: `/api/v1/health`

## 🔗 Java 서비스 통합

이 Python AI 서비스는 기존 Java 기반 PuppyTalk 서비스와 REST API를 통해 통합됩니다:

```java
// Java 서비스에서 AI 서비스 호출 예시
@Service 
public class AiServiceClient {
    
    @Value("${ai-service.url}")
    private String aiServiceUrl;
    
    public String generatePetResponse(ChatRequest request) {
        return webClient.post()
            .uri(aiServiceUrl + "/api/v1/chat/generate")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ChatResponse.class)
            .block()
            .getContent();
    }
}
```

## 🚨 에러 처리

### 표준 에러 응답

```json
{
  "success": false,
  "error": {
    "error_code": "GROK_API_ERROR",
    "message": "AI service temporarily unavailable", 
    "details": {
      "status_code": 503,
      "upstream_error": "Service unavailable"
    },
    "timestamp": "2024-01-01T12:00:00Z"
  },
  "trace_id": "trace_123",
  "request_id": "req_456"
}
```

### 에러 코드
- `VALIDATION_ERROR`: 입력 값 검증 실패
- `GROK_API_ERROR`: Grok API 호출 실패
- `MESSAGE_GENERATION_ERROR`: 메시지 생성 실패
- `INTERNAL_ERROR`: 내부 서버 오류

## 📝 개발 가이드

### 코드 스타일
- PEP 8 Python 스타일 가이드 준수
- Type hints 사용 필수
- Docstring 작성 (Google 스타일)
- 비동기 프로그래밍 패턴 활용

### 테스트
```bash
# 단위 테스트 (TODO: 구현 필요)
pytest tests/

# 통합 테스트 (TODO: 구현 필요)  
pytest tests/integration/
```

### 배포
- Docker 컨테이너 기반 배포
- 환경별 설정 분리 (dev/staging/prod)
- Rolling update 지원
- Health check 기반 로드 밸런싱

## 🤝 기여 가이드

1. 이슈 생성 또는 기존 이슈 확인
2. Feature branch 생성 (`feature/새기능-이름`)  
3. 코드 작성 및 테스트
4. Pull Request 생성
5. 코드 리뷰 및 머지

## 📄 라이선스

이 프로젝트는 회사 내부 프로젝트로 외부 공개되지 않습니다.