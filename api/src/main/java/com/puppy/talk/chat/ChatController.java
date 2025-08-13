package com.puppy.talk.chat;

import com.puppy.talk.support.ApiResponse;
import com.puppy.talk.chat.dto.response.ChatStartResponse;
import com.puppy.talk.chat.dto.response.MessageResponse;
import com.puppy.talk.chat.dto.request.MessageSendRequest;
import com.puppy.talk.chat.dto.response.MessageSendResponse;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.dto.ChatStartResult;
import com.puppy.talk.dto.MessageSendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 펫과의 대화를 시작합니다.
     */
    @PostMapping("/start/{petId}")
    public ApiResponse<ChatStartResponse> startChat(
        @PathVariable @Positive Long petId) {
        PetIdentity petIdentity = PetIdentity.of(petId);
        
        ChatStartResult result = chatService.startChatWithPet(petIdentity);
        
        ChatStartResponse response = ChatStartResponse.from(result);
        
        return ApiResponse.ok(response, "Chat started successfully");
    }

    /**
     * 펫에게 메시지를 보냅니다.
     */
    @PostMapping("/rooms/{chatRoomId}/messages")
    public ApiResponse<MessageSendResponse> sendMessage(
        @PathVariable @Positive Long chatRoomId,
        @Valid @RequestBody MessageSendRequest request
    ) {
        ChatRoomIdentity chatRoomIdentity = ChatRoomIdentity.of(chatRoomId);
        
        MessageSendResult result = chatService.sendMessageToPet(chatRoomIdentity, request.content());
        
        MessageSendResponse response = MessageSendResponse.from(result);
        
        return ApiResponse.ok(response, "Message sent successfully");
    }

    /**
     * 채팅방의 메시지 히스토리를 조회합니다.
     */
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ApiResponse<List<MessageResponse>> getChatHistory(
        @PathVariable @Positive Long chatRoomId) {
        ChatRoomIdentity chatRoomIdentity = ChatRoomIdentity.of(chatRoomId);
        
        List<Message> messages = chatService.getChatHistory(chatRoomIdentity);
        
        List<MessageResponse> responses = messages.stream()
            .map(MessageResponse::from)
            .toList();
        
        return ApiResponse.ok(responses);
    }

    /**
     * 채팅방의 읽지 않은 메시지를 모두 읽음 처리합니다.
     */
    @PutMapping("/rooms/{chatRoomId}/messages/read")
    public ApiResponse<Void> markMessagesAsRead(
        @PathVariable @Positive Long chatRoomId) {
        ChatRoomIdentity chatRoomIdentity = ChatRoomIdentity.of(chatRoomId);
        
        chatService.markMessagesAsRead(chatRoomIdentity);
        
        return ApiResponse.ok("Messages marked as read");
    }
}
