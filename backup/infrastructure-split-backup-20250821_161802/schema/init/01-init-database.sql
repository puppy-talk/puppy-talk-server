-- Puppy Talk 데이터베이스 초기화 스크립트
-- 이 스크립트는 MySQL 컨테이너가 처음 시작될 때 실행됩니다.

-- 데이터베이스 생성 (이미 존재하는 경우 무시)
CREATE DATABASE IF NOT EXISTS puppy_talk_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 데이터베이스 선택
USE puppy_talk_db;

-- 사용자 권한 확인 및 부여
GRANT ALL PRIVILEGES ON puppy_talk_db.* TO 'puppy_user'@'%';
FLUSH PRIVILEGES;

-- 초기 설정 완료 로그
SELECT 'Puppy Talk Database initialized successfully!' as status;

