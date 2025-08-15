package com.puppy.talk.chat;

import com.puppy.talk.support.ApiResponse;
import com.puppy.talk.chat.dto.response.ChatStartResponse;
import com.puppy.talk.chat.dto.response.MessageResponse;
import com.puppy.talk.chat.dto.request.MessageSendRequest;
import com.puppy.talk.chat.dto.response.MessageSendResponse;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.dto.ChatStartResult;
import com.puppy.talk.dto.MessageSendResult;
import com.puppy.talk.chat.command.MessageSendCommand;
import com.puppy.talk.facade.ChatFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 관리 API")
public class ChatController {

    private final ChatService chatService;
    private final ChatFacade chatFacade; // Facade 패턴 적용

    @PostMapping("/start/{petId}")
    @Operation(summary = "채팅 시작", description = "지정된 반려동물과 채팅을 시작합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "채팅 시작 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "반려동물을 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ApiResponse<ChatStartResponse> startChat(
        @Parameter(description = "반려동물 ID", required = true) @PathVariable @Positive Long petId) {
        PetIdentity petIdentity = PetIdentity.of(petId);
        
        ChatStartResult result = chatService.startChatWithPet(petIdentity);
        
        ChatStartResponse response = ChatStartResponse.from(result);
        
        return ApiResponse.ok(response, "Chat started successfully");
    }

    @PostMapping("/rooms/{chatRoomId}/messages")
    @Operation(summary = "메시지 전송", description = "채팅방에 메시지를 전송하고 AI 반려동물의 응답을 받습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "메시지 전송 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ApiResponse<MessageSendResponse> sendMessage(
        @Parameter(description = "채팅방 ID", required = true) @PathVariable @Positive Long chatRoomId,
        @Valid @RequestBody MessageSendRequest request
    ) {
        ChatRoomIdentity chatRoomIdentity = ChatRoomIdentity.of(chatRoomId);
        
        MessageSendCommand command = MessageSendCommand.of(request.content());
        MessageSendResult result = chatService.sendMessageToPet(chatRoomIdentity, command);
        
        MessageSendResponse response = MessageSendResponse.from(result);
        
        return ApiResponse.ok(response, "Message sent successfully");
    }

    @GetMapping("/rooms/{chatRoomId}/messages")
    @Operation(summary = "채팅 히스토리 조회", description = "지정된 채팅방의 메시지 히스토리를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "채팅 히스토리 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ApiResponse<List<MessageResponse>> getChatHistory(
        @Parameter(description = "채팅방 ID", required = true) @PathVariable @Positive Long chatRoomId) {
        ChatRoomIdentity chatRoomIdentity = ChatRoomIdentity.of(chatRoomId);
        
        List<Message> messages = chatService.getChatHistory(chatRoomIdentity);
        
        List<MessageResponse> responses = messages.stream()
            .map(MessageResponse::from)
            .toList();
        
        return ApiResponse.ok(responses);
    }

    @PutMapping("/rooms/{chatRoomId}/messages/read")
    @Operation(summary = "메시지 읽음 처리", description = "채팅방의 모든 메시지를 읽음 상태로 변경합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "메시지 읽음 처리 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ApiResponse<Void> markMessagesAsRead(
        @Parameter(description = "채팅방 ID", required = true) @PathVariable @Positive Long chatRoomId) {
        ChatRoomIdentity chatRoomIdentity = ChatRoomIdentity.of(chatRoomId);
        
        chatService.markMessagesAsRead(chatRoomIdentity);
        
        return ApiResponse.ok("Messages marked as read");
    }
}
