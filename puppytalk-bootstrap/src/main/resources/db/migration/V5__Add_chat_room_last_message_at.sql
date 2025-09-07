-- 채팅방 테이블에 last_message_at 컬럼 추가
-- 채팅방의 마지막 메시지 시간 추적

-- 먼저 nullable로 추가
ALTER TABLE chat_rooms 
ADD COLUMN last_message_at TIMESTAMP NULL;

-- 기존 데이터에 대해 last_message_at을 created_at으로 초기화 (잘못된 datetime 값 처리)
UPDATE chat_rooms 
SET last_message_at = CASE 
    WHEN created_at IS NULL OR created_at = '0000-00-00 00:00:00' THEN CURRENT_TIMESTAMP
    ELSE created_at 
END;

-- 이제 NOT NULL 제약조건 추가
ALTER TABLE chat_rooms MODIFY COLUMN last_message_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- last_message_at 인덱스 추가 (최근 활동 채팅방 조회 성능 향상)
CREATE INDEX idx_chat_rooms_last_message_at ON chat_rooms(last_message_at);

-- status 컬럼이 더 이상 필요 없으므로 제거 (활성 채팅방만 존재)
ALTER TABLE chat_rooms DROP COLUMN status;