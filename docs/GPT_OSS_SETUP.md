# gpt-oss 로컬 모델 설정 가이드

이 문서는 [OpenAI gpt-oss](https://github.com/openai/gpt-oss) 모델을 로컬에서 실행하여 puppy-talk-server와 연동하는 방법을 설명합니다.

## 🎯 개요

gpt-oss는 OpenAI에서 공개한 오픈 소스 언어 모델로, 다음과 같은 장점이 있습니다:

- **무료 사용**: API 키나 요금 없이 로컬에서 실행
- **개인정보 보호**: 데이터가 외부로 전송되지 않음
- **커스터마이징**: 모델 파라미터 자유 조정 가능
- **오프라인 동작**: 인터넷 연결 없이도 사용 가능

## 📋 시스템 요구사항

### 최소 요구사항 (gpt-oss:20b)
- **RAM**: 16GB 이상
- **Storage**: 15GB 여유 공간
- **CPU**: Intel/AMD x64 또는 Apple Silicon

### 권장 요구사항 (gpt-oss:120b)
- **RAM**: 64GB 이상
- **Storage**: 80GB 여유 공간
- **GPU**: NVIDIA GPU (CUDA 지원) 또는 Apple Silicon

## 🚀 설치 방법

### 1. Ollama 설치

#### macOS
```bash
# Homebrew를 사용하여 설치
brew install ollama

# 또는 공식 웹사이트에서 다운로드
# https://ollama.ai/download
```

#### Linux
```bash
# 공식 설치 스크립트 사용
curl -fsSL https://ollama.ai/install.sh | sh
```

#### Windows
1. [Ollama 공식 웹사이트](https://ollama.ai/download)에서 Windows 설치 파일 다운로드
2. 설치 파일 실행하여 설치 완료

### 2. gpt-oss 모델 다운로드

```bash
# 20B 모델 다운로드 (권장 - 빠른 응답)
ollama pull gpt-oss:20b

# 또는 120B 모델 다운로드 (더 높은 품질)
ollama pull gpt-oss:120b
```

### 3. 모델 실행

```bash
# 20B 모델 실행
ollama run gpt-oss:20b

# 또는 120B 모델 실행
ollama run gpt-oss:120b
```

### 4. 서버 백그라운드 실행

```bash
# Ollama 서버를 백그라운드에서 실행
ollama serve

# 다른 터미널에서 모델 실행
ollama run gpt-oss:20b
```

## ⚙️ 설정

### 1. application.yml 설정

```yaml
gpt-oss:
  server:
    url: http://localhost:11434  # Ollama 서버 주소
  model: gpt-oss:20b            # 사용할 모델
  max-tokens: 150               # 최대 토큰 수
  temperature: 0.8              # 창의성 수준 (0.0-1.0)
  timeout: 30                   # 타임아웃 (초)
```

### 2. 환경 변수 설정 (선택사항)

```bash
export GPT_OSS_SERVER_URL="http://localhost:11434"
export GPT_OSS_MODEL="gpt-oss:20b"
export GPT_OSS_MAX_TOKENS="150"
export GPT_OSS_TEMPERATURE="0.8"
export GPT_OSS_TIMEOUT="30"
```

## 🧪 테스트

### 1. 모델 상태 확인

```bash
# 실행 중인 모델 확인
ollama ps

# 설치된 모델 목록 확인
ollama list

# 서버 상태 확인
curl http://localhost:11434/v1/models
```

### 2. 직접 테스트

```bash
# 간단한 채팅 테스트
curl -X POST http://localhost:11434/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-oss:20b",
    "messages": [
      {"role": "user", "content": "안녕하세요!"}
    ],
    "max_tokens": 100,
    "temperature": 0.8
  }'
```

### 3. Spring Boot 애플리케이션 테스트

```bash
# 서버 실행
./gradlew bootRun

# HTTP 요청 테스트 (http-requests/ai-chat-api.http 참조)
```

## 🔧 트러블슈팅

### 모델 다운로드 실패
```bash
# 네트워크 확인
ping ollama.ai

# 프록시 설정 (필요한 경우)
export HTTP_PROXY=http://proxy.company.com:8080
export HTTPS_PROXY=http://proxy.company.com:8080

# 재다운로드
ollama pull gpt-oss:20b --force
```

### 메모리 부족 오류
```bash
# 작은 모델 사용
ollama pull gpt-oss:20b

# 시스템 리소스 확인
htop
free -h
```

### 연결 오류
```bash
# Ollama 서버 재시작
ollama serve

# 포트 확인
netstat -tuln | grep 11434

# 방화벽 확인 (필요한 경우)
sudo ufw allow 11434
```

### 응답 속도 개선
```bash
# GPU 사용 확인 (NVIDIA)
nvidia-smi

# 모델 설정 조정
# temperature 낮추기 (0.1-0.5)
# max_tokens 줄이기 (50-100)
```

## 📊 성능 최적화

### 1. 하드웨어 최적화
- **Apple Silicon**: Metal 가속 자동 활용
- **NVIDIA GPU**: CUDA 드라이버 최신 버전 설치
- **AMD GPU**: ROCm 설정 (Linux)

### 2. 모델 선택 기준
- **gpt-oss:20b**: 빠른 응답, 일반적인 품질
- **gpt-oss:120b**: 높은 품질, 느린 응답

### 3. 파라미터 튜닝
```yaml
gpt-oss:
  max-tokens: 100        # 짧은 응답을 위해 줄이기
  temperature: 0.7       # 일관성을 위해 낮추기
  timeout: 20            # 빠른 타임아웃
```

## 🔄 모델 업데이트

```bash
# 새 버전 확인
ollama list

# 모델 업데이트
ollama pull gpt-oss:20b

# 이전 버전 삭제 (공간 절약)
ollama rm gpt-oss:20b-old
```

## 📝 추가 리소스

- [Ollama 공식 문서](https://ollama.ai/docs)
- [gpt-oss GitHub 저장소](https://github.com/openai/gpt-oss)
- [모델 성능 벤치마크](https://github.com/openai/gpt-oss#performance)

## ❓ 자주 묻는 질문

**Q: 상업적 사용이 가능한가요?**
A: 네, Apache 2.0 라이선스로 상업적 사용이 가능합니다.

**Q: 모델을 커스터마이징할 수 있나요?**
A: 네, 파인튜닝이나 프롬프트 엔지니어링을 통해 가능합니다.

**Q: 인터넷 연결이 필요한가요?**
A: 모델 다운로드 시에만 필요하며, 실행 시에는 불필요합니다.

**Q: 여러 모델을 동시에 실행할 수 있나요?**
A: 네, 메모리가 충분하다면 가능합니다.
