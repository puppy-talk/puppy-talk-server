-- MySQL 개발환경 초기화 스크립트
-- root 사용자 권한 설정
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '1234';
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED WITH mysql_native_password BY '1234';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

-- 데이터베이스 생성 확인
CREATE DATABASE IF NOT EXISTS puppy_talk_db;
USE puppy_talk_db;

-- 기본 테스트 테이블 생성 (Liquibase가 없을 때 대비)
CREATE TABLE IF NOT EXISTS health_check (
    id INT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(50) DEFAULT 'OK',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT IGNORE INTO health_check (status) VALUES ('INITIALIZED');