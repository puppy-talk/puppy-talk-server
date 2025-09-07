-- 사용자 테이블에 last_active_at 컬럼 추가
-- 휴면 계정 관리를 위한 마지막 활동 시간 추적

ALTER TABLE users 
ADD COLUMN last_active_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- 기존 데이터에 대해 last_active_at을 created_at으로 초기화
UPDATE users SET last_active_at = created_at WHERE last_active_at IS NULL;

-- last_active_at 인덱스 추가 (휴면 계정 조회 성능 향상)
CREATE INDEX idx_users_last_active_at ON users(last_active_at);

-- is_deleted 인덱스 추가 (활성/삭제 사용자 필터링 성능 향상)  
CREATE INDEX idx_users_is_deleted ON users(is_deleted);

-- status 컬럼이 더 이상 필요 없으므로 제거 (is_deleted로 대체)
ALTER TABLE users DROP COLUMN status;