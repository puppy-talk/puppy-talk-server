# 🌱 시드 데이터 자동 삽입 가이드

## 📋 개요

Docker Compose로 애플리케이션을 실행할 때 시드 데이터가 자동으로 데이터베이스에 저장되도록 설정되어 있습니다.

## 🚀 실행 방법

### 1. 환경 변수 설정
```bash
# 환경 변수 파일 복사
cp docker-compose.env .env

# 필요에 따라 .env 파일 수정
```

### 2. Docker Compose 실행
```bash
# 모든 서비스 시작 (시드 데이터 포함)
docker-compose up -d

# 로그 확인
docker-compose logs -f
```

### 3. 시드 데이터 삽입 확인
```bash
# MySQL에 직접 연결하여 확인
docker exec -it puppy-talk-mysql mysql -u puppy_user -ppuppy_pass puppy_talk_db

# 테이블별 데이터 수 확인
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Pets' as table_name, COUNT(*) as count FROM pets
UNION ALL
SELECT 'Personas' as table_name, COUNT(*) as count FROM personas;
```

## 🔧 동작 원리

### 1. 데이터베이스 초기화
- MySQL 컨테이너 시작 시 `schema/init/01-init-database.sql` 실행
- 데이터베이스 및 사용자 생성

### 2. 애플리케이션 시작
- Spring Boot 애플리케이션 시작
- Liquibase 자동 실행 (Spring Boot Actuator health check 완료 후)

### 3. 시드 데이터 삽입
- `db.changelog-master.xml`에 정의된 모든 changelog 실행
- 테이블 생성 → 시드 데이터 삽입 순서로 진행

### 4. 완료 확인
- `seed-data` 서비스가 애플리케이션 상태 확인
- 시드 데이터 삽입 완료 로그 출력

## 📁 파일 구조

```
schema/
├── init/
│   ├── 01-init-database.sql          # DB 초기화
│   └── 02-run-liquibase.sh          # Liquibase 실행
├── changelog/
│   ├── db.changelog-master.xml       # 마스터 changelog
│   └── changes/                      # 개별 changelog 파일들
│       ├── 001-create-dogs-table.xml
│       ├── 002-rename-dogs-to-pets.xml
│       ├── 003-create-users-table.xml
│       ├── 004-create-personas-table.xml
│       ├── 005-update-pets-table.xml
│       ├── 006-create-chat-rooms-table.xml
│       ├── 007-create-messages-table.xml
│       ├── 008-create-user-activities-table.xml
│       ├── 009-create-inactivity-notifications-table.xml
│       ├── 010-insert-sample-personas.xml
│       ├── 011-create-device-tokens-table.xml
│       ├── 012-create-push-notifications-table.xml
│       ├── 013-insert-sample-users.xml
│       └── 014-insert-sample-pets.xml
```

## 🎯 포함된 시드 데이터

### 1. 사용자 (Users)
- `testuser1` - 테스트사용자1
- `testuser2` - 테스트사용자2  
- `demo_user` - 데모사용자

### 2. 펫 (Pets)
- `멍멍이` - 골든리트리버 (3살, 수컷)
- `댕댕이` - 웰시코기 (2살, 암컷)
- `강아지` - 푸들 (1살, 수컷)

### 3. 페르소나 (Personas)
- `장난꾸러기 골든리트리버` - 활발하고 호기심 많은 성격

## ⚠️ 주의사항

### 1. 데이터 초기화
- `docker-compose down -v` 실행 시 모든 데이터 삭제
- 시드 데이터는 컨테이너 재시작 시마다 다시 삽입

### 2. 충돌 방지
- ID 값이 중복되지 않도록 설계
- `IF NOT EXISTS` 조건으로 안전한 삽입

### 3. 순서 의존성
- 테이블 생성 → 시드 데이터 삽입 순서 준수
- Liquibase changelog 순서 중요

## 🔍 문제 해결

### 1. 시드 데이터가 삽입되지 않는 경우
```bash
# 애플리케이션 로그 확인
docker-compose logs app

# Liquibase 상태 확인
curl http://localhost:8081/actuator/liquibase
```

### 2. 데이터베이스 연결 실패
```bash
# MySQL 컨테이너 상태 확인
docker-compose ps mysql

# MySQL 로그 확인
docker-compose logs mysql
```

### 3. 권한 문제
```bash
# MySQL에 root로 연결하여 권한 확인
docker exec -it puppy-talk-mysql mysql -u root -proot1234
```

## 📚 추가 정보

- [Liquibase 공식 문서](https://www.liquibase.org/documentation/)
- [Spring Boot Liquibase 자동 설정](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.using-basic-sql-scripts)
- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
