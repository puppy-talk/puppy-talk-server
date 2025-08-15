#!/bin/bash

# Puppy Talk 개발 환경 중지 스크립트
# 모든 개발 서비스를 안전하게 중지하고 정리합니다

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# 로고 출력
echo -e "${RED}"
echo "🐕 ======================================"
echo "   Puppy Talk 개발 환경 중지"  
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

# 프로젝트 디렉토리로 이동
cd_to_project() {
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
    cd "$PROJECT_DIR"
    print_status "프로젝트 디렉토리: $(pwd)"
}

# 현재 실행 중인 서비스 확인
check_running_services() {
    print_status "현재 실행 중인 개발 서비스 확인..."
    
    if docker-compose -f docker-compose.dev.enhanced.yml ps -q 2>/dev/null | grep -q .; then
        echo ""
        print_status "실행 중인 서비스:"
        docker-compose -f docker-compose.dev.enhanced.yml ps
        echo ""
        return 0
    else
        print_warning "실행 중인 개발 서비스가 없습니다."
        return 1
    fi
}

# 서비스 중지 옵션 선택
select_stop_option() {
    echo -e "${BLUE}중지 옵션을 선택하세요:${NC}"
    echo "1) 모든 서비스 중지 (데이터 보존)"
    echo "2) 모든 서비스 중지 및 볼륨 삭제 (데이터 삭제)"
    echo "3) 특정 서비스만 중지"
    echo "4) 취소"
    echo ""
    
    read -p "선택 (1-4): " choice
    
    case $choice in
        1)
            stop_all_services false
            ;;
        2)
            confirm_data_deletion
            ;;
        3)
            stop_specific_services
            ;;
        4)
            print_status "작업이 취소되었습니다."
            exit 0
            ;;
        *)
            print_error "잘못된 선택입니다. 다시 실행하세요."
            exit 1
            ;;
    esac
}

# 데이터 삭제 확인
confirm_data_deletion() {
    print_warning "⚠️  모든 데이터베이스 데이터와 캐시가 삭제됩니다!"
    print_warning "⚠️  이 작업은 되돌릴 수 없습니다!"
    echo ""
    print_status "정말로 모든 데이터를 삭제하시겠습니까? (DELETE 입력)"
    read -r response
    
    if [[ "$response" == "DELETE" ]]; then
        stop_all_services true
    else
        print_status "데이터 삭제가 취소되었습니다."
        stop_all_services false
    fi
}

# 모든 서비스 중지
stop_all_services() {
    local delete_volumes=$1
    
    print_status "모든 개발 서비스를 중지합니다..."
    
    # 환경 파일 확인
    ENV_FILE=""
    if [[ -f ".env.dev" ]]; then
        ENV_FILE="--env-file .env.dev"
    fi
    
    if [[ "$delete_volumes" == "true" ]]; then
        print_warning "볼륨과 함께 모든 서비스를 중지합니다..."
        docker-compose $ENV_FILE -f docker-compose.dev.enhanced.yml down -v --remove-orphans
        print_success "모든 서비스가 중지되고 데이터가 삭제되었습니다."
    else
        print_status "데이터를 보존하며 서비스를 중지합니다..."
        docker-compose $ENV_FILE -f docker-compose.dev.enhanced.yml down --remove-orphans
        print_success "모든 서비스가 중지되었습니다. (데이터 보존됨)"
    fi
    
    # 네트워크 정리
    cleanup_networks
}

# 특정 서비스 중지
stop_specific_services() {
    print_status "중지할 서비스를 선택하세요:"
    echo ""
    echo "핵심 서비스:"
    echo "  1) mysql"
    echo "  2) redis"
    echo ""
    echo "관리 도구:"
    echo "  3) phpmyadmin"
    echo "  4) redis-commander"
    echo "  5) dev-dashboard"
    echo ""
    echo "모니터링 도구:"
    echo "  6) prometheus"
    echo "  7) grafana"
    echo "  8) jaeger"
    echo ""
    echo "개발 도구:"
    echo "  9) mailhog"
    echo "  10) wiremock"
    echo "  11) localstack"
    echo "  12) postgres"
    echo "  13) pgadmin"
    echo ""
    
    read -p "서비스 번호를 입력하세요 (공백으로 구분): " service_numbers
    
    # 서비스 이름 배열
    declare -a services=("" "mysql" "redis" "phpmyadmin" "redis-commander" "dev-dashboard" 
                        "prometheus" "grafana" "jaeger" "mailhog" "wiremock" "localstack" 
                        "postgres" "pgadmin")
    
    # 선택된 서비스들 중지
    ENV_FILE=""
    if [[ -f ".env.dev" ]]; then
        ENV_FILE="--env-file .env.dev"
    fi
    
    for num in $service_numbers; do
        if [[ $num -ge 1 && $num -le 13 ]]; then
            service_name=${services[$num]}
            print_status "${service_name} 서비스를 중지합니다..."
            docker-compose $ENV_FILE -f docker-compose.dev.enhanced.yml stop "$service_name"
            docker-compose $ENV_FILE -f docker-compose.dev.enhanced.yml rm -f "$service_name"
            print_success "${service_name} 서비스가 중지되었습니다."
        else
            print_warning "잘못된 서비스 번호: $num"
        fi
    done
}

# 네트워크 정리
cleanup_networks() {
    print_status "개발 네트워크 정리 중..."
    
    # 사용되지 않는 네트워크 확인 및 제거
    if docker network ls | grep -q "puppy-talk-dev"; then
        if ! docker network inspect puppy-talk-dev | grep -q "Containers"; then
            docker network rm puppy-talk-dev 2>/dev/null || true
            print_success "개발 네트워크가 정리되었습니다."
        else
            print_status "네트워크가 아직 사용 중입니다."
        fi
    fi
}

# Docker 리소스 정리 (선택사항)
cleanup_docker_resources() {
    print_status "Docker 리소스 정리를 수행하시겠습니까? (y/N)"
    read -r response
    
    if [[ "$response" =~ ^[Yy]$ ]]; then
        print_status "사용되지 않는 Docker 리소스를 정리합니다..."
        
        # 중지된 컨테이너 제거
        print_status "중지된 컨테이너 제거 중..."
        docker container prune -f
        
        # 사용되지 않는 이미지 제거
        print_status "사용되지 않는 이미지 제거 중..."
        docker image prune -f
        
        # 사용되지 않는 네트워크 제거
        print_status "사용되지 않는 네트워크 제거 중..."
        docker network prune -f
        
        print_success "Docker 리소스 정리가 완료되었습니다."
    else
        print_status "Docker 리소스 정리를 건너뜁니다."
    fi
}

# 최종 상태 표시
show_final_status() {
    echo ""
    print_status "현재 Docker 컨테이너 상태:"
    
    # Puppy Talk 관련 컨테이너만 표시
    if docker ps -a --filter "name=puppy-talk" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | tail -n +2 | grep -q .; then
        docker ps -a --filter "name=puppy-talk" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    else
        print_success "모든 Puppy Talk 개발 서비스가 완전히 중지되었습니다."
    fi
    
    echo ""
    print_success "🎉 개발 환경 정리가 완료되었습니다!"
    echo ""
    
    echo -e "${BLUE}💡 유용한 정보:${NC}"
    echo "  • 개발 환경 재시작: ./scripts/dev-start.sh"
    echo "  • Docker 상태 확인: docker ps -a"
    echo "  • 볼륨 확인: docker volume ls"
    echo "  • 네트워크 확인: docker network ls"
    echo ""
}

# 메인 실행 함수
main() {
    cd_to_project
    
    if check_running_services; then
        echo ""
        select_stop_option
        echo ""
        cleanup_docker_resources
        show_final_status
    else
        echo ""
        print_status "정리할 개발 서비스가 없습니다."
        cleanup_docker_resources
        echo ""
        print_success "정리 작업이 완료되었습니다."
    fi
}

# 스크립트 실행
main "$@"