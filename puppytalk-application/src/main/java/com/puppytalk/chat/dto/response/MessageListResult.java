package com.puppytalk.chat.dto.response;

import com.puppytalk.chat.Message;
import java.util.List;

/**
 * 메시지 목록 조회 결과 (커서 기반 페이징)
 */
public record MessageListResult(
    List<MessageResult> messageList,
    int count,
    Long nextCursor,
    boolean hasNext
) {
    
    public static MessageListResult from(List<Message> messages) {
        List<MessageResult> results = messages.stream()
                .map(MessageResult::from)
                .toList();
        
        // 다음 커서는 마지막 메시지의 ID
        Long nextCursor = results.isEmpty() ? null : 
            results.get(results.size() - 1).messageId();
        
        // hasNext는 실제 사용 시 요청한 size와 비교하여 결정해야 하므로 
        // 여기서는 기본값으로 false 설정 (추후 개선 가능)
        boolean hasNext = false;
        
        return new MessageListResult(results, results.size(), nextCursor, hasNext);
    }
    
    /**
     * 커서 정보를 포함한 결과 생성
     */
    public static MessageListResult withCursor(List<Message> messages, int requestedSize) {
        List<MessageResult> results = messages.stream()
                .map(MessageResult::from)
                .toList();
        
        // 다음 커서는 마지막 메시지의 ID
        Long nextCursor = results.isEmpty() ? null : 
            results.get(results.size() - 1).messageId();
        
        // 요청한 크기만큼 결과가 있으면 다음 페이지가 있을 가능성이 높음
        boolean hasNext = results.size() >= requestedSize;
        
        return new MessageListResult(results, results.size(), nextCursor, hasNext);
    }
    
    /**
     * 빈 결과 생성
     */
    public static MessageListResult empty() {
        return new MessageListResult(List.of(), 0, null, false);
    }
}