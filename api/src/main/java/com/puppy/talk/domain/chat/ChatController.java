package com.puppy.talk.domain.chat;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.Message;
import com.puppy.talk.chat.dto.MessageSendCommand;
import com.puppy.talk.domain.chat.dto.request.MessageSendRequest;
import com.puppy.talk.domain.chat.dto.response.ChatStartResponse;
import com.puppy.talk.domain.chat.dto.response.MessageResponse;
import com.puppy.talk.domain.chat.dto.response.MessageSendResponse;
import com.puppy.talk.chat.dto.MessageSendResult;
import com.puppy.talk.chat.ChatFacade;
import com.puppy.talk.global.support.ApiResponse;
import com.puppy.talk.pet.PetIdentity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 관리 API")
public class ChatController {

    private final ChatFacade chatFacade;

    @PostMapping("/start/{petId}")
    @Operation(summary = "채팅 시작", description = "지정된 반려동물과 채팅을 시작합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "채팅 시작 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "반려동물을 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ApiResponse<ChatStartResponse> startChat(
        @Parameter(description = "반려동물 ID", required = true) @PathVariable @Positive Long petId) {
        log.info("Starting chat with pet: {}", petId);
        var result = chatFacade.startChatWithPet(PetIdentity.of(petId));
        log.debug("Chat started successfully for pet: {}, chatRoomId: {}", 
            petId, result.chatRoom().identity().id());
        return ApiResponse.ok(
            ChatStartResponse.from(result),
            "Chat started successfully"
        );
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
        log.info("Sending message to chatRoom: {}, content length: {}", 
            chatRoomId, request.content().length());
        
        MessageSendResult result = chatFacade.sendMessageToPet(
            ChatRoomIdentity.of(chatRoomId),
            MessageSendCommand.of(request.content())
        );
        
        log.debug("Message sent successfully, messageId: {}", 
            result.message().identity().id());

        return ApiResponse.ok(
            MessageSendResponse.from(result),
            "Message sent successfully"
        );
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
        log.info("Retrieving chat history for chatRoom: {}", chatRoomId);
        
        List<Message> messages = chatFacade.getChatHistory(ChatRoomIdentity.of(chatRoomId));
        
        List<MessageResponse> responses = messages.stream()
            .map(MessageResponse::from)
            .toList();
        
        log.debug("Retrieved {} messages for chatRoom: {}", messages.size(), chatRoomId);
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
        log.info("Marking messages as read for chatRoom: {}", chatRoomId);
        chatFacade.markMessagesAsRead(ChatRoomIdentity.of(chatRoomId));
        return ApiResponse.ok("Messages marked as read");
    }
}
