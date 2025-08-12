package com.puppy.talk.controller;

import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.chat.Message;
import com.puppy.talk.model.pet.PetIdentity;
import com.puppy.talk.service.ChatService;
import com.puppy.talk.service.dto.ChatStartResult;
import com.puppy.talk.service.dto.MessageSendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
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
    public ResponseEntity<ApiResponse<ChatStartResponse>> startChat(
        @PathVariable @Positive Long petId) {
        PetIdentity petIdentity = PetIdentity.of(petId);
        
        ChatStartResult result = chatService.startChatWithPet(petIdentity);
        
        ChatStartResponse response = ChatStartResponse.from(result);
        
        URI location = URI.create(String.format("/api/chat/rooms/%d", result.chatRoom().identity().id()));
        return ResponseEntity
            .created(location)
            .body(ApiResponse.ok(response, "Chat started successfully"));
    }

    /**
     * 펫에게 메시지를 보냅니다.
     */
    @PostMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<MessageSendResponse>> sendMessage(
        @PathVariable @Positive Long chatRoomId,
        @Valid @RequestBody MessageSendRequest request
    ) {
        ChatRoomIdentity chatRoomIdentity = ChatRoomIdentity.of(chatRoomId);
        
        MessageSendResult result = chatService.sendMessageToPet(chatRoomIdentity, request.content());
        
        MessageSendResponse response = MessageSendResponse.from(result);
        
        URI location = URI.create(String.format("/api/chat/messages/%d", result.message().identity().id()));
        return ResponseEntity
            .created(location)
            .body(ApiResponse.ok(response, "Message sent successfully"));
    }

    /**
     * 채팅방의 메시지 히스토리를 조회합니다.
     */
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getChatHistory(
        @PathVariable @Positive Long chatRoomId) {
        ChatRoomIdentity chatRoomIdentity = ChatRoomIdentity.of(chatRoomId);
        
        List<Message> messages = chatService.getChatHistory(chatRoomIdentity);
        
        List<MessageResponse> responses = messages.stream()
            .map(MessageResponse::from)
            .toList();
        
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    /**
     * 채팅방의 읽지 않은 메시지를 모두 읽음 처리합니다.
     */
    @PutMapping("/rooms/{chatRoomId}/messages/read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
        @PathVariable @Positive Long chatRoomId) {
        ChatRoomIdentity chatRoomIdentity = ChatRoomIdentity.of(chatRoomId);
        
        chatService.markMessagesAsRead(chatRoomIdentity);
        
        return ResponseEntity.ok(ApiResponse.ok("Messages marked as read"));
    }
}
