package com.puppy.talk.global.support;

import com.puppy.talk.pet.PersonaNotFoundException;
import com.puppy.talk.pet.PetNotFoundException;
import com.puppy.talk.user.UserNotFoundException;
import com.puppy.talk.user.DuplicateEmailException;
import com.puppy.talk.user.DuplicateUsernameException;
import com.puppy.talk.chat.ChatRoomNotFoundException;
import com.puppy.talk.chat.MessageNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;

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
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(RuntimeException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return ApiResponse.error("Requested resource not found", ErrorCode.USER_NOT_FOUND);
    }

    @ExceptionHandler({
        DuplicateEmailException.class,
        DuplicateUsernameException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleConflict(RuntimeException e) {
        log.warn("Resource conflict: {}", e.getMessage());
        return ApiResponse.error("Resource already exists", ErrorCode.DUPLICATE_USERNAME);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBadRequest(IllegalArgumentException e) {
        log.warn("Bad request: {}", e.getMessage());
        return ApiResponse.error("Invalid request parameters", ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationError(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        log.warn("Validation error: {}", errorMessage);
        return ApiResponse.error("Validation failed: " + errorMessage, ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ApiResponse<Void> handleNotImplemented(UnsupportedOperationException e) {
        log.warn("Feature not implemented: {}", e.getMessage());
        return ApiResponse.error(e.getMessage(), ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleInternalServerError(RuntimeException e) {
        log.error("Unexpected error occurred", e);
        return ApiResponse.error("An unexpected error occurred", ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
