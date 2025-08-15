#!/bin/bash

# Puppy Talk 개발 환경 시작 스크립트
# 개발에 필요한 모든 인프라 서비스를 시작합니다

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 로고 출력
echo -e "${PURPLE}"
echo "🐕 ======================================"
echo "   Puppy Talk 개발 환경 시작"  
echo "======================================${NC}"
echo ""

# 함수 정의
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Docker가 실행 중인지 확인
check_docker() {
    print_status "Docker 상태 확인 중..."
    if ! docker info >/dev/null 2>&1; then
        print_error "Docker가 실행되지 않았습니다. Docker를 시작하고 다시 시도하세요."
        exit 1
    fi
    print_success "Docker가 실행 중입니다."
}

# 프로젝트 디렉토리로 이동
cd_to_project() {
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
    cd "$PROJECT_DIR"
    print_status "프로젝트 디렉토리: $(pwd)"
}

# 기존 개발 환경 정리
cleanup_dev_env() {
    print_status "기존 개발 환경 정리 중..."
    
    # 기존 컨테이너 중지
    if docker-compose -f docker-compose.dev.enhanced.yml ps -q 2>/dev/null | grep -q .; then
        print_warning "기존 개발 환경을 중지합니다..."
        docker-compose -f docker-compose.dev.enhanced.yml down
    fi
    
    print_success "기존 환경 정리 완료"
}

# 환경변수 파일 확인
check_env_file() {
    print_status "환경 설정 파일 확인 중..."
    
    if [[ ! -f ".env.dev" ]]; then
        print_warning ".env.dev 파일이 없습니다. 기본 설정으로 진행합니다."
        return 0
    fi
    
    print_success ".env.dev 파일을 발견했습니다."
}

# 필수 서비스 시작 (MySQL, Redis)
start_core_services() {
    print_status "필수 서비스 시작 중 (MySQL, Redis)..."
    
    ENV_FILE=""
    if [[ -f ".env.dev" ]]; then
        ENV_FILE="--env-file .env.dev"
    fi
    
    docker-compose $ENV_FILE -f docker-compose.dev.enhanced.yml up -d mysql redis
    
    print_status "서비스 헬스체크 대기 중..."
    sleep 10
    
    # MySQL 헬스체크
    print_status "MySQL 연결 테스트 중..."
    for i in {1..30}; do
        if docker-compose -f docker-compose.dev.enhanced.yml exec -T mysql mysqladmin ping -h localhost --silent 2>/dev/null; then
            print_success "MySQL이 준비되었습니다."
            break
        fi
        if [[ $i -eq 30 ]]; then
            print_error "MySQL 연결 시간이 초과되었습니다."
            exit 1
        fi
        echo -n "."
        sleep 2
    done
    
    # Redis 헬스체크
    print_status "Redis 연결 테스트 중..."
    for i in {1..15}; do
        if docker-compose -f docker-compose.dev.enhanced.yml exec -T redis redis-cli ping 2>/dev/null | grep -q PONG; then
            print_success "Redis가 준비되었습니다."
            break
        fi
        if [[ $i -eq 15 ]]; then
            print_error "Redis 연결 시간이 초과되었습니다."
            exit 1
        fi
        echo -n "."
        sleep 2
    done
}

# 관리 도구 시작
start_admin_tools() {
    print_status "관리 도구 시작 중 (phpMyAdmin, Redis Commander)..."
    
    ENV_FILE=""
    if [[ -f ".env.dev" ]]; then
        ENV_FILE="--env-file .env.dev"
    fi
    
    docker-compose $ENV_FILE -f docker-compose.dev.enhanced.yml up -d phpmyadmin redis-commander dev-dashboard
    
    print_success "관리 도구가 시작되었습니다."
}

# 선택적 서비스 시작
start_optional_services() {
    print_status "모니터링 및 개발 도구를 시작하시겠습니까? (y/N)"
    read -r response
    
    if [[ "$response" =~ ^[Yy]$ ]]; then
        print_status "모니터링 및 개발 도구 시작 중..."
        
        ENV_FILE=""
        if [[ -f ".env.dev" ]]; then
            ENV_FILE="--env-file .env.dev"
        fi
        
        docker-compose $ENV_FILE -f docker-compose.dev.enhanced.yml up -d \
            prometheus grafana jaeger \
            mailhog wiremock localstack
        
        print_success "모니터링 및 개발 도구가 시작되었습니다."
        
        echo ""
        print_status "추가 서비스 접속 정보:"
        echo "  📊 Prometheus: http://localhost:9090"
        echo "  📈 Grafana: http://localhost:3000 (admin/dev123)"
        echo "  🔍 Jaeger: http://localhost:16686"
        echo "  📧 MailHog: http://localhost:8025"
        echo "  🔧 WireMock: http://localhost:8080"
        echo "  ☁️  LocalStack: http://localhost:4566"
    else
        print_status "선택적 서비스는 건너뜁니다."
    fi
}

# 애플리케이션 빌드 및 실행 안내
show_app_instructions() {
    echo ""
    print_success "🎉 개발 환경 설정이 완료되었습니다!"
    echo ""
    
    echo -e "${CYAN}📋 다음 단계:${NC}"
    echo ""
    echo "1. 애플리케이션 빌드:"
    echo -e "   ${YELLOW}./gradlew clean build${NC}"
    echo ""
    echo "2. 애플리케이션 실행:"
    echo -e "   ${YELLOW}./gradlew application-api:bootRun${NC}"
    echo ""
    echo "3. 또는 IDE에서 Application.java를 실행하세요."
    echo ""
    
    echo -e "${CYAN}🔗 접속 정보:${NC}"
    echo "  🐕 애플리케이션: http://localhost:8081 (실행 후)"
    echo "  📊 개발 대시보드: http://localhost:8000"
    echo "  🗄️  phpMyAdmin: http://localhost:8090"
    echo "  📦 Redis Commander: http://localhost:8091 (admin/dev123)"
    echo ""
    
    echo -e "${CYAN}🔧 개발 설정:${NC}"
    echo "  • MySQL: localhost:3306 (root/1234)"
    echo "  • Redis: localhost:6379"
    echo "  • Spring Profile: local"
    echo ""
    
    echo -e "${CYAN}💡 유용한 명령어:${NC}"
    echo "  • 서비스 상태 확인: docker-compose -f docker-compose.dev.enhanced.yml ps"
    echo "  • 로그 확인: docker-compose -f docker-compose.dev.enhanced.yml logs -f [service]"
    echo "  • 환경 중지: ./scripts/dev-stop.sh"
    echo ""
}

# 최종 상태 확인
check_final_status() {
    print_status "최종 서비스 상태 확인..."
    echo ""
    docker-compose -f docker-compose.dev.enhanced.yml ps
    echo ""
}

# 메인 실행 함수
main() {
    check_docker
    cd_to_project
    cleanup_dev_env
    check_env_file
    
    echo ""
    start_core_services
    
    echo ""
    start_admin_tools
    
    echo ""
    start_optional_services
    
    echo ""
    check_final_status
    show_app_instructions
}

# 스크립트 실행
main "$@"