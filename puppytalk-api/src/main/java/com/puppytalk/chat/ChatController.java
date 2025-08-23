package com.puppytalk.chat;

import com.puppytalk.chat.dto.request.ChatRoomCreateCommand;
import com.puppytalk.chat.dto.request.ChatRoomCreateRequest;
import com.puppytalk.chat.dto.request.ChatRoomListQuery;
import com.puppytalk.chat.dto.request.MessageListQuery;
import com.puppytalk.chat.dto.request.MessageSendCommand;
import com.puppytalk.chat.dto.request.MessageSendRequest;
import com.puppytalk.chat.dto.response.ChatRoomCreateResponse;
import com.puppytalk.chat.dto.response.ChatRoomListResult;
import com.puppytalk.chat.dto.response.ChatRoomResponse;
import com.puppytalk.chat.dto.response.ChatRoomsResponse;
import com.puppytalk.chat.dto.response.MessageListResult;
import com.puppytalk.chat.dto.response.MessagesResponse;
import com.puppytalk.support.ApiResponse;
import com.puppytalk.support.ApiSuccessMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat", description = "채팅 관리 API")
@RestController
@RequestMapping("/api/chat")
@Validated
public class ChatController {

    private final ChatFacade chatFacade;

    public ChatController(ChatFacade chatFacade) {
        this.chatFacade = chatFacade;
    }

    @Operation(summary = "채팅방 생성/조회", description = "채팅방을 생성하거나 기존 채팅방을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "새 채팅방 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기존 채팅방 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrFindChatRoom(
        @Parameter(description = "채팅방 생성 요청 정보", required = true)
        @Valid @RequestBody ChatRoomCreateRequest request
    ) {
        ChatRoomCreateCommand command = ChatRoomCreateCommand.of(
            request.userId(),
            request.petId()
        );

        ChatRoomCreateResponse response = chatFacade.createOrFindChatRoom(command);

        HttpStatus status = response.isNewlyCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        String message = response.isNewlyCreated() ?
            ApiSuccessMessage.CHAT_ROOM_CREATE_SUCCESS.getMessage() :
            ApiSuccessMessage.CHAT_ROOM_FIND_SUCCESS.getMessage();

        ChatRoomResponse chatRoomResponse = ChatRoomResponse.from(response.chatRoom());

        return ResponseEntity.status(status)
            .body(ApiResponse.success(chatRoomResponse, message));
    }

    @Operation(summary = "채팅방 목록 조회", description = "사용자의 모든 채팅방 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomsResponse>> getChatRoomList(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @RequestParam @Positive(message = "사용자 ID는 양수여야 합니다") Long userId
    ) {
        ChatRoomListQuery query = ChatRoomListQuery.of(userId);
        ChatRoomListResult result = chatFacade.getChatRoomList(query);

        return ResponseEntity.ok(
            ApiResponse.success(
                ChatRoomsResponse.from(result),
                ApiSuccessMessage.CHAT_ROOM_LIST_SUCCESS.getMessage()
            )
        );
    }

    @Operation(summary = "메시지 전송", description = "채팅방에 사용자 메시지를 전송합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "메시지 전송 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    @PostMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<Void>> sendMessage(
        @Parameter(description = "채팅방 ID", required = true, example = "1")
        @PathVariable @Positive(message = "채팅방 ID는 양수여야 합니다") Long chatRoomId,
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @RequestParam @Positive(message = "user ID는 양수여야 합니다") Long userId,
        @Parameter(description = "메시지 전송 요청 정보", required = true)
        @Valid @RequestBody MessageSendRequest request
    ) {
        MessageSendCommand command = MessageSendCommand.of(
            chatRoomId,
            userId,
            request.content()
        );

        chatFacade.sendMessage(command);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(ApiSuccessMessage.CHAT_MESSAGE_SEND_SUCCESS.getMessage()));
    }

    @Operation(summary = "메시지 목록 조회", description = "메시지 목록을 커서 기반 페이징으로 조회합니다. ")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "메시지 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<MessagesResponse>> getMessageList(
        @Parameter(description = "채팅방 ID", required = true, example = "1")
        @PathVariable @Positive(message = "채팅방 ID는 양수여야 합니다") Long chatRoomId,
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @RequestParam @Positive(message = "사용자 ID는 양수여야 합니다") Long userId,
        @Parameter(description = "커서 (이전 조회의 nextCursor 값, 첫 조회시 생략)", example = "123")
        @RequestParam(required = false) @Positive(message = "커서는 양수여야 합니다") Long cursor,
        @Parameter(description = "조회할 메시지 개수 (기본: 20, 최대: 100)", example = "20")
        @RequestParam(required = false) @Positive(message = "사이즈는 양수여야 합니다") Integer size
    ) {
        MessageListQuery query = MessageListQuery.of(chatRoomId, userId, cursor, size);
        MessageListResult result = chatFacade.getMessageList(query);

        return ResponseEntity.ok(
            ApiResponse.success(
                MessagesResponse.from(result),
                ApiSuccessMessage.CHAT_MESSAGE_LIST_SUCCESS.getMessage())
        );
    }
}