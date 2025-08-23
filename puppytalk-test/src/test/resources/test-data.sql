-- Test Data for Integration Tests
-- Minimal test data for setup

INSERT INTO users (username, email, status) VALUES 
('test_user', 'test@example.com', 'ACTIVE');

INSERT INTO pets (owner_id, name, persona, status) VALUES 
(1, '테스트펫', '테스트용 반려동물', 'ACTIVE');

INSERT INTO chat_rooms (user_id, pet_id, status) VALUES 
(1, 1, 'ACTIVE');

INSERT INTO messages (chat_room_id, sender_type, content) VALUES 
(1, 'USER', '테스트 메시지'),
(1, 'PET', '테스트 응답 메시지');

INSERT INTO user_activities (user_id, activity_type, last_active_at) VALUES 
(1, 'APP_USAGE', CURRENT_TIMESTAMP),
(1, 'CHAT_INTERACTION', CURRENT_TIMESTAMP);

INSERT INTO notifications (user_id, pet_id, title, content, type, status) VALUES 
(1, 1, '테스트 알림', '테스트용 알림 내용', 'PET_MESSAGE', 'PENDING');