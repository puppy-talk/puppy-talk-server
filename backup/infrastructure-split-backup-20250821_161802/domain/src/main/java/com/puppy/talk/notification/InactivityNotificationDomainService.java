package com.puppy.talk.notification;

import com.puppy.talk.activity.InactivityNotification;
import com.puppy.talk.activity.InactivityNotificationIdentity;
import com.puppy.talk.activity.InactivityNotificationRepository;
import com.puppy.talk.activity.NotificationStatus;
import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.ChatRoomRepository;
import com.puppy.talk.chat.Message;
import com.puppy.talk.chat.MessageRepository;
import com.puppy.talk.chat.SenderType;
import com.puppy.talk.notification.dto.InactivityNotificationStatistics;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.pet.PetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 비활성 알림 도메인 서비스
 * 
 * 순수한 비즈니스 로직과 데이터 관리를 담당합니다.
 * AI 통합이나 외부 서비스는 Application Layer에서 처리됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InactivityNotificationDomainService {

    private final InactivityNotificationRepository inactivityNotificationRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PetRepository petRepository;
    private final MessageRepository messageRepository;

    /**
     * 발송 대상 비활성 알림 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<InactivityNotification> findEligibleNotifications() {
        return inactivityNotificationRepository.findEligibleNotifications();
    }

    /**
     * 채팅방 ID로 비활성 알림을 비활성화합니다.
     */
    @Transactional
    public void disableNotificationForChatRoom(ChatRoom chatRoom) {
        Optional<InactivityNotification> notificationOpt = 
            inactivityNotificationRepository.findByChatRoomId(chatRoom.identity());
        
        if (notificationOpt.isPresent()) {
            InactivityNotification disabledNotification = notificationOpt.get().disable();
            inactivityNotificationRepository.save(disabledNotification);
            log.debug("Disabled inactivity notification for chatRoom: {}", chatRoom.identity().id());
        }
    }

    /**
     * 비활성 알림 통계를 조회합니다.
     */
    @Transactional(readOnly = true)
    public InactivityNotificationStatistics getNotificationStatistics() {
        long totalCount = inactivityNotificationRepository.count();
        long pendingCount = inactivityNotificationRepository.countByStatus(NotificationStatus.PENDING);
        long sentCount = inactivityNotificationRepository.countByStatus(NotificationStatus.SENT);
        long disabledCount = inactivityNotificationRepository.countByStatus(NotificationStatus.DISABLED);
        
        return new InactivityNotificationStatistics(totalCount, pendingCount, sentCount, disabledCount);
    }

    /**
     * 채팅방 정보를 조회합니다.
     */
    @Transactional(readOnly = true)
    public Optional<ChatRoom> findChatRoom(ChatRoomIdentity chatRoomId) {
        return chatRoomRepository.findByIdentity(chatRoomId);
    }

    /**
     * 펫 정보를 조회합니다.
     */
    @Transactional(readOnly = true)
    public Optional<Pet> findPet(PetIdentity petId) {
        return petRepository.findByIdentity(petId);
    }

    /**
     * 채팅방의 최근 메시지 히스토리를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<Message> findRecentMessages(ChatRoomIdentity chatRoomId, int limit) {
        return messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
            .stream()
            .limit(limit)
            .toList();
    }

    /**
     * 메시지를 저장합니다.
     */
    @Transactional
    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    /**
     * 비활성 알림을 발송 완료 상태로 업데이트합니다.
     */
    @Transactional
    public void markNotificationAsSent(InactivityNotification notification) {
        InactivityNotification sentNotification = notification.markAsSent();
        inactivityNotificationRepository.save(sentNotification);
        log.info("Marked inactivity notification as sent: {}", notification.identity().id());
    }

    /**
     * 비활성 알림을 업데이트합니다.
     */
    @Transactional
    public void updateNotification(InactivityNotification notification) {
        inactivityNotificationRepository.save(notification);
    }
}