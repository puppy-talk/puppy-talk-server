# AI Multi-Provider 설정 가이드

이 문서는 puppy-talk-server에서 다양한 AI 모델 제공업체를 설정하고 사용하는 방법을 설명합니다.

## 🎯 개요

puppy-talk-server는 다음 AI 제공업체를 지원합니다:

- **gpt-oss**: OpenAI의 오픈소스 모델 (로컬 실행, 무료)
- **OpenAI**: ChatGPT 시리즈 (API 키 필요, 유료)
- **Anthropic Claude**: Claude 시리즈 (API 키 필요, 유료)
- **Google Gemini**: Gemini 시리즈 (API 키 필요, 유료)

## 🔧 설정 방법

### 1. 기본 설정

`application.yml`에서 기본 AI 제공업체와 모델을 설정합니다:

```yaml
ai:
  default-provider: gpt-oss        # 기본 제공업체
  default-model: gpt-oss:20b       # 기본 모델
  max-tokens: 150                  # 최대 토큰 수
  temperature: 0.8                 # 창의성 수준 (0.0-1.0)
  fallback-providers: openai,claude # 대체 제공업체 (순서대로 시도)
```

### 2. 제공업체별 설정

#### gpt-oss (로컬 모델)
```yaml
ai:
  providers:
    gpt-oss:
      enabled: true
      server-url: http://localhost:11434
      timeout: 30
```

**지원 모델:**
- `gpt-oss:20b` (권장, 빠른 응답)
- `gpt-oss:120b` (높은 품질, 느린 응답)

#### OpenAI ChatGPT
```yaml
ai:
  providers:
    openai:
      enabled: false
      api-key: your-openai-api-key
      base-url: https://api.openai.com
      timeout: 30
```

**지원 모델:**
- `gpt-4`, `gpt-4-turbo`, `gpt-4o`, `gpt-4o-mini`
- `gpt-3.5-turbo`, `gpt-3.5-turbo-16k`

#### Anthropic Claude
```yaml
ai:
  providers:
    claude:
      enabled: false
      api-key: your-claude-api-key
      base-url: https://api.anthropic.com
      timeout: 30
```

**지원 모델:**
- `claude-3-5-sonnet-20241022`, `claude-3-5-haiku-20241022`
- `claude-3-opus-20240229`, `claude-3-sonnet-20240229`, `claude-3-haiku-20240307`

#### Google Gemini
```yaml
ai:
  providers:
    gemini:
      enabled: false
      api-key: your-gemini-api-key
      base-url: https://generativelanguage.googleapis.com
      timeout: 30
```

**지원 모델:**
- `gemini-1.5-pro`, `gemini-1.5-flash`, `gemini-1.0-pro`

## 🌍 환경 변수 설정

설정 파일 대신 환경 변수로도 설정 가능합니다:

```bash
# 기본 설정
export AI_DEFAULT_PROVIDER=gpt-oss
export AI_DEFAULT_MODEL=gpt-oss:20b
export AI_MAX_TOKENS=150
export AI_TEMPERATURE=0.8
export AI_FALLBACK_PROVIDERS=openai,claude

# gpt-oss 설정
export AI_GPT_OSS_ENABLED=true
export AI_GPT_OSS_SERVER_URL=http://localhost:11434
export AI_GPT_OSS_TIMEOUT=30

# OpenAI 설정
export AI_OPENAI_ENABLED=true
export AI_OPENAI_API_KEY=your-openai-api-key
export AI_OPENAI_BASE_URL=https://api.openai.com
export AI_OPENAI_TIMEOUT=30

# Claude 설정
export AI_CLAUDE_ENABLED=true
export AI_CLAUDE_API_KEY=your-claude-api-key
export AI_CLAUDE_BASE_URL=https://api.anthropic.com
export AI_CLAUDE_TIMEOUT=30

# Gemini 설정
export AI_GEMINI_ENABLED=true
export AI_GEMINI_API_KEY=your-gemini-api-key
export AI_GEMINI_BASE_URL=https://generativelanguage.googleapis.com
export AI_GEMINI_TIMEOUT=30
```

## 🚀 사용 방법

### 1. 단일 제공업체 사용
기본 제공업체만 활성화하여 사용:

```yaml
ai:
  default-provider: gpt-oss
  providers:
    gpt-oss:
      enabled: true
    openai:
      enabled: false
    claude:
      enabled: false
    gemini:
      enabled: false
```

### 2. 다중 제공업체 + 자동 대체
기본 제공업체가 실패하면 자동으로 대체 제공업체 사용:

```yaml
ai:
  default-provider: gpt-oss
  fallback-providers: openai,claude
  providers:
    gpt-oss:
      enabled: true
    openai:
      enabled: true
      api-key: your-api-key
    claude:
      enabled: true
      api-key: your-api-key
    gemini:
      enabled: false
```

### 3. 비용 최적화 설정
무료 로컬 모델을 우선 사용하고, 필요시에만 유료 서비스 사용:

```yaml
ai:
  default-provider: gpt-oss           # 무료 로컬 모델 우선
  fallback-providers: openai          # 로컬 모델 실패시 OpenAI 사용
  default-model: gpt-oss:20b          # 빠른 로컬 모델
  max-tokens: 100                     # 토큰 사용량 최소화
  temperature: 0.7                    # 일관성 있는 응답
```

## 📊 모니터링 및 상태 확인

### 1. 제공업체 상태 확인
각 제공업체의 상태를 확인할 수 있는 엔드포인트:

```http
GET /api/ai/providers/status
```

응답 예시:
```json
{
  "gpt-oss": {
    "name": "gpt-oss",
    "enabled": true,
    "healthy": true,
    "supportedModels": ["gpt-oss:20b", "gpt-oss:120b"]
  },
  "openai": {
    "name": "openai",
    "enabled": true,
    "healthy": false,
    "supportedModels": ["gpt-4", "gpt-3.5-turbo"]
  }
}
```

### 2. 로그 모니터링
AI 제공업체 선택 및 전환 로그:

```
DEBUG: Using default AI provider: gpt-oss
WARN:  Using fallback AI provider: openai (default gpt-oss is unavailable)
ERROR: No available AI providers found
```

## 🔧 트러블슈팅

### 1. "No available AI providers found" 오류
**원인**: 모든 제공업체가 비활성화되거나 실패
**해결**: 최소 하나의 제공업체를 활성화하고 설정 확인

```bash
# gpt-oss 서버 상태 확인
curl http://localhost:11434/v1/models

# OpenAI API 키 확인
curl -H "Authorization: Bearer $AI_OPENAI_API_KEY" https://api.openai.com/v1/models
```

### 2. 특정 제공업체 실패
**원인**: API 키 오류, 네트워크 문제, 서버 오류
**해결**: 로그 확인 및 대체 제공업체 설정

```yaml
ai:
  fallback-providers: gpt-oss,openai,claude  # 다양한 대체 옵션
```

### 3. 응답 품질 문제
**원인**: 부적절한 모델 또는 파라미터 설정
**해결**: 모델 및 파라미터 조정

```yaml
ai:
  default-model: gpt-4                # 더 좋은 모델 사용
  max-tokens: 200                     # 더 긴 응답 허용
  temperature: 0.6                    # 더 일관성 있는 응답
```

## 💰 비용 최적화 팁

### 1. 계층적 사용
```yaml
ai:
  default-provider: gpt-oss           # 무료 로컬 모델
  fallback-providers: openai          # 필요시에만 유료 서비스
```

### 2. 토큰 제한
```yaml
ai:
  max-tokens: 100                     # 짧은 응답으로 비용 절약
  temperature: 0.5                    # 더 결정적인 응답
```

### 3. 모델 선택
- **개발/테스트**: `gpt-oss:20b` (무료)
- **일반 사용**: `gpt-3.5-turbo` (저렴)
- **고품질 필요**: `gpt-4` 또는 `claude-3-sonnet` (비싸지만 고품질)

## 📈 성능 최적화

### 1. 타임아웃 설정
```yaml
ai:
  providers:
    gpt-oss:
      timeout: 10      # 로컬 모델은 빠른 타임아웃
    openai:
      timeout: 30      # 외부 API는 여유 있는 타임아웃
```

### 2. 모델별 특성 활용
- **빠른 응답**: `gpt-oss:20b`, `claude-3-haiku`, `gemini-1.5-flash`
- **높은 품질**: `gpt-4`, `claude-3-opus`, `gemini-1.5-pro`
- **균형**: `gpt-3.5-turbo`, `claude-3-sonnet`

### 3. 캐싱 활용
동일한 프롬프트에 대한 응답을 캐싱하여 비용과 응답 시간 절약 (추후 구현 예정)

## 🔒 보안 고려사항

### 1. API 키 관리
- 환경 변수 사용 (설정 파일에 직접 입력 금지)
- 정기적인 API 키 로테이션
- 최소 권한 원칙 적용

### 2. 네트워크 보안
- HTTPS 사용 강제
- 방화벽 설정 (gpt-oss는 로컬 네트워크만)
- API 요청 로깅 및 모니터링

### 3. 데이터 프라이버시
- gpt-oss: 데이터가 외부로 전송되지 않음 (로컬 처리)
- 상용 API: 각 제공업체의 데이터 정책 확인 필요
