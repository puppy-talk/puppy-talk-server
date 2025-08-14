# AI 펫 채팅 API 테스트 가이드

이 디렉토리에는 AI 펫 응답 기능을 테스트하기 위한 HTTP 요청 파일들이 포함되어 있습니다.

## 📁 파일 설명

### `ai-chat-api.http`
AI 펫과의 채팅 기능을 테스트하는 메인 파일입니다.
- 대화 시작, 메시지 전송, 히스토리 조회 등
- AI 자동 응답 기능 테스트
- 다양한 메시지 유형 및 감정 표현 테스트
- 에러 케이스 및 예외 상황 테스트

### `pet-management.http`
AI 채팅 테스트를 위한 펫 등록 및 관리 API입니다.
- 다양한 페르소나의 펫 등록
- 나이대별, 품종별 펫 설정
- 유효성 검증 테스트

## 🚀 사용 방법

### 1. 환경 설정
먼저 다음 환경 변수들을 설정하세요:

```http
@baseUrl = http://localhost:8080  # 서버 주소
@userId = 1                       # 테스트 사용자 ID
@personaId = 1                    # 페르소나 ID
@petId = 1                        # 펫 ID
@chatRoomId = 1                   # 채팅방 ID
```

### 2. AI 제공업체 설정
서버는 다양한 AI 제공업체를 지원합니다. 최소 하나는 설정해야 합니다:

#### 옵션 1: gpt-oss (무료, 로컬)
```bash
# Ollama 설치 (macOS)
brew install ollama

# gpt-oss 모델 다운로드 및 실행
ollama pull gpt-oss:20b
ollama run gpt-oss:20b
```

#### 옵션 2: OpenAI ChatGPT (유료)
```bash
export AI_OPENAI_ENABLED=true
export AI_OPENAI_API_KEY="your-openai-api-key"
```

#### 옵션 3: Anthropic Claude (유료)
```bash
export AI_CLAUDE_ENABLED=true
export AI_CLAUDE_API_KEY="your-claude-api-key"
```

#### 옵션 4: Google Gemini (유료)
```bash
export AI_GEMINI_ENABLED=true
export AI_GEMINI_API_KEY="your-gemini-api-key"
```

#### 다중 제공업체 설정 (권장)
```bash
# 기본: gpt-oss, 대체: OpenAI
export AI_DEFAULT_PROVIDER=gpt-oss
export AI_FALLBACK_PROVIDERS=openai,claude
export AI_GPT_OSS_ENABLED=true
export AI_OPENAI_ENABLED=true
export AI_OPENAI_API_KEY="your-api-key"
```
### 3. 테스트 순서

#### 3.1 기본 설정
1. **서버 시작**: `./gradlew bootRun`
2. **데이터베이스 초기화**: Liquibase가 자동으로 스키마 생성
3. **펫 등록**: `pet-management.http`에서 펫 등록 요청 실행

#### 3.2 AI 채팅 테스트
1. **대화 시작**: 펫과의 채팅방 생성
2. **메시지 전송**: 사용자 메시지 전송 (AI 응답 자동 생성됨)
3. **히스토리 확인**: 대화 내역에서 AI 응답 확인

## 🧪 테스트 시나리오

### 기본 대화 테스트
- 인사말 및 첫 만남
- 일상 대화 및 감정 표현
- 놀이 제안 및 활동 이야기

### AI 능력 테스트
- 복잡한 질문에 대한 응답
- 창의적 상황 대처
- 감정 공감 및 위로

### 페르소나별 차이 확인
- 활발한 성격 vs 차분한 성격
- 나이대별 응답 스타일
- 품종별 특성 반영

### 에러 처리 테스트
- 빈 메시지, 너무 긴 메시지
- 존재하지 않는 펫/채팅방
- 유효하지 않은 입력값

## 📊 응답 예시

### 성공적인 AI 응답
```json
{
  "success": true,
  "data": {
    "messageId": 123,
    "content": "안녕하세요! 꼬리를 흔들흔들~ 오늘 정말 기분 좋아요! 🐕✨",
    "senderType": "PET",
    "sentAt": "2024-01-15T10:30:00"
  },
  "message": "Message sent successfully"
}
```

### 채팅 히스토리 확인
```json
{
  "success": true,
  "data": [
    {
      "messageId": 122,
      "content": "안녕하세요! 처음 뵙겠습니다 🐕",
      "senderType": "USER",
      "createdAt": "2024-01-15T10:29:00"
    },
    {
      "messageId": 123,
      "content": "안녕하세요! 꼬리를 흔들흔들~ 오늘 정말 기분 좋아요! 🐕✨",
      "senderType": "PET",
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

## 🔧 디버깅 팁

### AI 응답이 생성되지 않는 경우
1. gpt-oss 서버 실행 상태 확인 (`ollama ps`)
2. 모델 다운로드 완료 여부 확인 (`ollama list`)
3. 네트워크 연결 상태 확인 (localhost:11434)
4. 서버 로그에서 에러 메시지 확인

### 응답 품질이 낮은 경우
1. 페르소나 설정 확인
2. 프롬프트 템플릿 조정
3. gpt-oss 모델 파라미터 튜닝 (temperature, max-tokens)
4. 더 큰 모델 사용 고려 (gpt-oss:120b)

### 성능 문제가 있는 경우
1. gpt-oss 모델 응답 시간 모니터링
2. 채팅 히스토리 제한 조정
3. 비동기 처리 검토
4. GPU/CPU 사용률 확인

## 📈 모니터링 포인트

- AI 응답 생성 성공률
- 평균 응답 시간
- 사용자 만족도 (응답 품질)
- 로컬 서버 리소스 사용량
- 모델 메모리 사용량
