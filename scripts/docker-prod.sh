#!/bin/bash

# Puppy Talk 프로덕션 환경 Docker 실행 스크립트

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_ROOT"

echo "🐶 Puppy Talk 프로덕션 환경 배포 시작..."

# 환경변수 파일 확인
if [ ! -f .env.prod ]; then
    echo "❌ .env.prod 파일이 필요합니다."
    echo "💡 .env.example을 참고하여 .env.prod를 생성해주세요."
    exit 1
fi

# SSL 인증서 확인 (프로덕션에서 필요한 경우)
if [ ! -d "nginx/ssl" ]; then
    echo "⚠️  nginx/ssl 디렉토리가 없습니다. SSL 설정을 확인해주세요."
fi

# 애플리케이션 빌드
echo "🏗️  애플리케이션 빌드 중..."
./gradlew clean application-api:bootJar

if [ ! -f "application-api/build/libs/application-api.jar" ]; then
    echo "❌ 빌드된 JAR 파일을 찾을 수 없습니다."
    exit 1
fi

# Docker 이미지 빌드
echo "🐳 Docker 이미지 빌드 중..."
BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
VCS_REF=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")

export BUILD_DATE VCS_REF

# 기존 컨테이너 정리 (주의: 프로덕션에서는 신중하게!)
echo "🧹 기존 프로덕션 환경 정리 중..."
read -p "기존 프로덕션 환경을 정리하시겠습니까? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker-compose -f docker-compose.prod.yml down
fi

# 프로덕션 환경 시작
echo "🚀 프로덕션 환경 시작 중..."
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d

# 서비스 상태 확인
echo "⏳ 서비스 시작 대기 중..."
sleep 30

echo "📊 서비스 상태 확인:"
docker-compose -f docker-compose.prod.yml ps

# 헬스체크 확인
echo "🔍 애플리케이션 헬스체크 중..."
for i in {1..60}; do
    if curl -s http://localhost/actuator/health | grep -q '"status":"UP"'; then
        echo "✅ 애플리케이션 헬스체크 성공!"
        break
    fi
    echo "⏳ 애플리케이션 시작 대기 중... ($i/60)"
    sleep 5
done

# 서비스 엔드포인트 확인
echo "🌐 서비스 엔드포인트 확인 중..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost/actuator/health | grep -q "200"; then
    echo "✅ API 서버: http://localhost"
    echo "✅ Swagger UI: http://localhost/swagger-ui.html"
    echo "✅ Health Check: http://localhost/actuator/health"
fi

echo ""
echo "🎉 프로덕션 환경 배포 완료!"
echo ""
echo "🔧 모니터링 도구:"
echo "  📊 Grafana: http://localhost:3000"
echo "  📈 Prometheus: http://localhost:9090"
echo ""
echo "🔍 로그 확인:"
echo "  docker-compose -f docker-compose.prod.yml logs -f [서비스명]"
echo ""
echo "📈 성능 모니터링:"
echo "  docker stats"
echo ""
echo "🛑 서비스 중지:"
echo "  docker-compose -f docker-compose.prod.yml down"