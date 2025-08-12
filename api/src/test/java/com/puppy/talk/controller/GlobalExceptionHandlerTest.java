package com.puppy.talk.controller;

import com.puppy.talk.exception.pet.PersonaNotFoundException;
import com.puppy.talk.exception.user.UserNotFoundException;
import com.puppy.talk.exception.user.DuplicateEmailException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_UserNotFound() {
        // Given
        UserNotFoundException exception = new UserNotFoundException("User not found");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleNotFound(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("Requested resource not found");
    }

    @Test
    void handleNotFound_PersonaNotFound() {
        // Given
        PersonaNotFoundException exception = new PersonaNotFoundException("Persona not found");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleNotFound(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void handleConflict_DuplicateEmail() {
        // Given
        DuplicateEmailException exception = new DuplicateEmailException("test@example.com");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleConflict(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("RESOURCE_CONFLICT");
        assertThat(response.getBody().message()).isEqualTo("Resource already exists");
    }

    @Test
    void handleBadRequest_IllegalArgument() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleBadRequest(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("Invalid request parameters");
    }

    @Test
    void handleInternalServerError_RuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleInternalServerError(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
    }
}