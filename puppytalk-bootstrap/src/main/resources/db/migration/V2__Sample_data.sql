-- V2__Sample_data.sql
-- PuppyTalk Sample Data for Development and Testing
-- 개발 및 테스트용 초기 데이터

-- 테스트 사용자 생성
INSERT INTO users (username, email, status) VALUES 
('puppy_lover', 'puppy@example.com', 'ACTIVE'),
('dog_owner', 'dog@example.com', 'ACTIVE'),
('cat_friend', 'cat@example.com', 'ACTIVE'),
('test_user', 'test@example.com', 'INACTIVE');

-- 테스트 반려동물 생성
INSERT INTO pets (owner_id, name, persona, status) VALUES 
(1, '맥스', '활발하고 장난기 많은 골든 리트리버. 공 놀이를 좋아하고 주인과 함께 산책하는 것을 즐긴다. 항상 꼬리를 흔들며 긍정적인 에너지를 가지고 있다.', 'ACTIVE'),
(1, '루나', '조용하고 우아한 페르시안 고양이. 따뜻한 햇살 아래에서 낮잠 자는 것을 좋아하며, 주인의 무릎에 올라가 골골거리는 것을 즐긴다.', 'ACTIVE'),
(2, '코코', '똑똑하고 호기심 많은 보더콜리. 새로운 트릭 배우기를 좋아하고 퍼즐 게임을 즐긴다. 주인의 말을 잘 듣는 착한 개.', 'ACTIVE'),
(3, '모카', '장난기 많고 사교적인 래브라도 믹스. 다른 개들과 놀기를 좋아하고 사람들과 친해지는 것을 즐긴다. 간식을 매우 좋아한다.', 'ACTIVE');

-- 테스트 채팅방 생성
INSERT INTO chat_rooms (user_id, pet_id, status) VALUES 
(1, 1, 'ACTIVE'), -- puppy_lover와 맥스
(1, 2, 'ACTIVE'), -- puppy_lover와 루나
(2, 3, 'ACTIVE'), -- dog_owner와 코코
(3, 4, 'ACTIVE'); -- cat_friend와 모카

-- 테스트 메시지 생성
INSERT INTO messages (chat_room_id, sender_type, content) VALUES 
-- 맥스와의 대화
(1, 'USER', '맥스야, 오늘 날씨가 좋네! 산책 갈까?'),
(1, 'PET', '와우! 정말요? 저는 산책을 정말 좋아해요! 공도 가져가요! 🐕'),
(1, 'USER', '좋아, 공도 가져가자!'),
(1, 'PET', '야호! 빨리 가요! 꼬리가 멈추지 않아요! 🎾'),

-- 루나와의 대화
(2, 'USER', '루나야, 잘 지내고 있어?'),
(2, 'PET', '음... 따뜻한 햇살 아래에서 낮잠을 자고 있었어요. 골골골... 😸'),
(2, 'USER', '편안해 보이네. 맛있는 간식 줄까?'),
(2, 'PET', '간식이라면... 관심이 있어요. 하지만 너무 시끄럽게 하지는 말아요.'),

-- 코코와의 대화
(3, 'USER', '코코야, 새로운 트릭 배워볼까?'),
(3, 'PET', '오! 새로운 트릭이요? 저는 배우는 걸 정말 좋아해요! 무엇을 배울까요? 🧠'),
(3, 'USER', '하이파이브는 어때?'),
(3, 'PET', '하이파이브! 좋은 아이디어네요! 손을 올리면 되나요? 🐾');

-- 테스트 사용자 활동 생성
INSERT INTO user_activities (user_id, activity_type, last_active_at) VALUES 
(1, 'APP_USAGE', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(1, 'CHAT_INTERACTION', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(2, 'APP_USAGE', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 'CHAT_INTERACTION', DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
(3, 'APP_USAGE', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(3, 'CHAT_INTERACTION', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(4, 'APP_USAGE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'CHAT_INTERACTION', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 테스트 알림 생성
INSERT INTO notifications (user_id, pet_id, title, content, type, status) VALUES 
(1, 1, '맥스가 보고싶어해요!', '맥스: 주인님, 오늘 산책 어떠셨나요? 저도 함께하고 싶었어요!', 'PET_MESSAGE', 'DELIVERED'),
(2, 3, '코코의 새 트릭!', '코코: 주인님! 제가 새로운 트릭을 배웠어요! 보러 와주세요!', 'PET_MESSAGE', 'READ'),
(3, 4, '모카의 간식 시간', '모카: 간식 시간이에요! 맛있는 거 주세요!', 'PET_MESSAGE', 'PENDING');