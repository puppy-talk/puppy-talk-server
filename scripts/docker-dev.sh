#!/bin/bash

# Puppy Talk 개발 환경 Docker 실행 스크립트

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_ROOT"

echo "🐶 Puppy Talk 개발 환경 설정 중..."

# 환경변수 파일 확인
if [ ! -f .env ]; then
    if [ -f .env.example ]; then
        echo "📋 .env 파일이 없습니다. .env.example을 복사합니다..."
        cp .env.example .env
        echo "⚠️  .env 파일을 확인하고 필요한 값들을 수정해주세요."
    else
        echo "❌ .env.example 파일을 찾을 수 없습니다."
        exit 1
    fi
fi

# 기존 컨테이너 정리
echo "🧹 기존 개발 환경 정리 중..."
docker-compose -f docker-compose.dev.yml down -v 2>/dev/null || true

# 네트워크 정리
echo "🌐 네트워크 정리 중..."
docker network prune -f

# 볼륨 상태 확인
echo "💾 볼륨 상태 확인 중..."
docker volume ls | grep puppy-talk || true

# 개발 환경 시작
echo "🚀 개발 환경 시작 중..."
docker-compose -f docker-compose.dev.yml up -d

# 서비스 상태 확인
echo "⏳ 서비스 시작 대기 중..."
sleep 10

echo "📊 서비스 상태 확인:"
docker-compose -f docker-compose.dev.yml ps

# MySQL 연결 테스트
echo "🔍 MySQL 연결 테스트 중..."
for i in {1..30}; do
    if docker-compose -f docker-compose.dev.yml exec mysql mysqladmin ping -h localhost --silent; then
        echo "✅ MySQL 연결 성공!"
        break
    fi
    echo "⏳ MySQL 연결 대기 중... ($i/30)"
    sleep 2
done

# Redis 연결 테스트  
echo "🔍 Redis 연결 테스트 중..."
if docker-compose -f docker-compose.dev.yml exec redis redis-cli ping | grep -q PONG; then
    echo "✅ Redis 연결 성공!"
else
    echo "❌ Redis 연결 실패"
fi

echo ""
echo "🎉 개발 환경 준비 완료!"
echo ""
echo "📱 애플리케이션 실행:"
echo "  ./gradlew application-api:bootRun"
echo ""
echo "🔧 관리 도구:"
echo "  📊 phpMyAdmin: http://localhost:8090"
echo "  🔴 Redis Commander: http://localhost:8091 (admin/admin123)"
echo ""
echo "🔍 로그 확인:"
echo "  docker-compose -f docker-compose.dev.yml logs -f [서비스명]"
echo ""
echo "🛑 환경 정리:"
echo "  docker-compose -f docker-compose.dev.yml down -v"