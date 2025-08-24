package com.puppytalk.chat.exception;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.user.UserId;

/**
 * 채팅방을 찾을 수 없을 때 발생하는 예외
 * <p>
 * 사용자가 존재하지 않는 채팅방에 접근하려고 시도할 때 발생합니다.
 * 디버깅과 모니터링을 위해 첫팅방 ID와 요청한 사용자 정보를 제공합니다.
 * 
 * @author PuppyTalk Team
 * @since 1.0
 */
public class ChatRoomNotFoundException extends RuntimeException {
    
    private final ChatRoomId chatRoomId;
    private final UserId requestedBy;
    private final String errorCode;
    
    /**
     * 채팅방 ID로 예외 생성
     */
    public ChatRoomNotFoundException(ChatRoomId chatRoomId) {
        this(chatRoomId, null, "채팅방을 찾을 수 없습니다", "CHAT_ROOM_NOT_FOUND");
    }
    
    /**
     * 채팅방 ID와 사용자 ID로 예외 생성
     */
    public ChatRoomNotFoundException(ChatRoomId chatRoomId, UserId requestedBy) {
        this(chatRoomId, requestedBy, "채팅방을 찾을 수 없습니다", "CHAT_ROOM_NOT_FOUND");
    }
    
    /**
     * 상세 정보를 포함한 예외 생성
     */
    public ChatRoomNotFoundException(ChatRoomId chatRoomId, UserId requestedBy, String message, String errorCode) {
        super(buildMessage(chatRoomId, requestedBy, message));
        this.chatRoomId = chatRoomId;
        this.requestedBy = requestedBy;
        this.errorCode = errorCode;
    }
    
    /**
     * 레거시 지원을 위한 생성자
     */
    @Deprecated
    public ChatRoomNotFoundException(String message) {
        super(message);
        this.chatRoomId = null;
        this.requestedBy = null;
        this.errorCode = "CHAT_ROOM_NOT_FOUND";
    }
    
    private static String buildMessage(ChatRoomId chatRoomId, UserId requestedBy, String baseMessage) {
        StringBuilder sb = new StringBuilder(baseMessage);
        if (chatRoomId != null) {
            sb.append(" (ChatRoom ID: ").append(chatRoomId.getValue()).append(")");
        }
        if (requestedBy != null) {
            sb.append(" (Requested by User ID: ").append(requestedBy.getValue()).append(")");
        }
        return sb.toString();
    }
    
    /**
     * 채팅방 ID 반환
     */
    public ChatRoomId getChatRoomId() {
        return chatRoomId;
    }
    
    /**
     * 요청한 사용자 ID 반환
     */
    public UserId getRequestedBy() {
        return requestedBy;
    }
    
    /**
     * 에러 코드 반환
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 디버깅용 상세 정보 반환
     */
    public String getDetailedInfo() {
        return String.format("ChatRoomNotFoundException[chatRoomId=%s, requestedBy=%s, errorCode=%s, message=%s]",
                chatRoomId, requestedBy, errorCode, getMessage());
    }
}