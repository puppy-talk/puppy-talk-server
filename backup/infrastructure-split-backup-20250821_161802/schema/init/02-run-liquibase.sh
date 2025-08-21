#!/bin/bash

# Liquibase 마이그레이션 실행 스크립트
# 이 스크립트는 애플리케이션이 시작된 후 실행됩니다.

echo "Starting Liquibase migration..."

# 애플리케이션이 완전히 시작될 때까지 대기
echo "Waiting for application to be ready..."
until curl -f http://app:8081/actuator/health > /dev/null 2>&1; do
    echo "Application not ready yet, waiting..."
    sleep 10
done

echo "Application is ready! Starting Liquibase migration..."

# Liquibase 마이그레이션 실행
# 애플리케이션 내장 Liquibase가 자동으로 실행됨
echo "Liquibase migration completed!"

# 시드 데이터 삽입 확인
echo "Checking seed data..."
mysql -h mysql -u puppy_user -ppuppy_pass puppy_talk_db -e "
SELECT 
    'Users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 
    'Pets' as table_name, COUNT(*) as count FROM pets
UNION ALL
SELECT 
    'Personas' as table_name, COUNT(*) as count FROM personas;
"

echo "Database initialization completed successfully!"

