package com.puppy.talk.controller.support;

import com.puppy.talk.exception.pet.PersonaNotFoundException;
import com.puppy.talk.exception.pet.PetNotFoundException;
import com.puppy.talk.exception.user.UserNotFoundException;
import com.puppy.talk.exception.user.DuplicateEmailException;
import com.puppy.talk.exception.user.DuplicateUsernameException;
import com.puppy.talk.exception.chat.ChatRoomNotFoundException;
import com.puppy.talk.exception.chat.MessageNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
        UserNotFoundException.class,
        PersonaNotFoundException.class,
        PetNotFoundException.class,
        ChatRoomNotFoundException.class,
        MessageNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ErrorCode.USER_NOT_FOUND, "Requested resource not found"));
    }

    @ExceptionHandler({
        DuplicateEmailException.class,
        DuplicateUsernameException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException e) {
        log.warn("Resource conflict: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(ErrorCode.DUPLICATE_USERNAME, "Resource already exists"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        log.warn("Bad request: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ErrorCode.VALIDATION_ERROR, "Invalid request parameters"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleInternalServerError(RuntimeException e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
    }

    public record ErrorResponse(ErrorCode code, String message) {}
}