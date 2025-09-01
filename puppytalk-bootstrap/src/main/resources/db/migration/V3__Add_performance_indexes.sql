-- 성능 최적화를 위한 추가 인덱스
-- 퍼피톡 서버의 쿼리 패턴 분석 기반 인덱스 추가

-- 1. pets 테이블 복합 인덱스
-- 근거: PetRepository.findByIdAndOwnerId(PetId id, UserId ownerId) 메서드
-- 사용자가 자신의 반려동물을 조회할 때 id와 owner_id를 동시에 필터링
CREATE INDEX idx_pets_id_owner_id ON pets(id, owner_id);

-- 2. pets 테이블 owner_id + status 복합 인덱스  
-- 근거: PetRepository.findByOwnerId(UserId ownerId)에서 status='ACTIVE'인 것만 조회
-- 소유자의 활성 반려동물 목록을 조회할 때 성능 향상
CREATE INDEX idx_pets_owner_id_status ON pets(owner_id, status);

-- 3. users 테이블 username + status 복합 인덱스
-- 근거: UserRepository.findByUsername()에서 삭제되지 않은 사용자만 조회하는 경우
-- 사용자명 검색 시 활성 사용자만 필터링할 때 성능 향상
CREATE INDEX idx_users_username_status ON users(username, status);

-- 4. users 테이블 email + status 복합 인덱스
-- 근거: UserRepository.findByEmail()에서 삭제되지 않은 사용자만 조회하는 경우  
-- 이메일 검색 시 활성 사용자만 필터링할 때 성능 향상
CREATE INDEX idx_users_email_status ON users(email, status);

-- 5. notifications 테이블 복합 인덱스들
-- 근거: NotificationRepository.existsByUserIdAndTypeAndStatus() 메서드
-- 특정 사용자의 특정 타입과 상태의 알림 존재 여부 확인 시 성능 향상
CREATE INDEX idx_notifications_user_type_status ON notifications(user_id, type, status);

-- 6. notifications 테이블 사용자별 정렬 최적화
-- 근거: NotificationRepository.findByUserIdOrderByCreatedAtDesc() 메서드
-- 사용자의 알림 목록을 생성일자 역순으로 조회할 때 성능 향상
CREATE INDEX idx_notifications_user_created_desc ON notifications(user_id, created_at DESC);

-- 7. notifications 테이블 타입별 상태 조회 최적화
-- 근거: NotificationRepository.findByTypeAndStatus() 메서드
-- 특정 타입과 상태의 알림 목록 조회 시 성능 향상
CREATE INDEX idx_notifications_type_status ON notifications(type, status);

-- 8. chat_rooms 테이블 user_id + status 복합 인덱스
-- 근거: ChatRoomRepository.findByUserId()에서 활성 채팅방만 조회하는 경우
-- 사용자의 활성 채팅방 목록 조회 시 성능 향상
CREATE INDEX idx_chat_rooms_user_id_status ON chat_rooms(user_id, status);

-- 9. user_activities 테이블 비활성 사용자 조회 최적화
-- 근거: UserRepository.findInactiveUsers(LocalDateTime cutoffTime) 메서드
-- 특정 시간 이전에 활동한 사용자들을 조회할 때 성능 향상
CREATE INDEX idx_user_activities_type_activity_at_user ON user_activities(activity_type, activity_at, user_id);