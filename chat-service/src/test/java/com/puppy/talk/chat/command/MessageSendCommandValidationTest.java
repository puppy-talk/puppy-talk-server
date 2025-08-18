package com.puppy.talk.chat.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MessageSendCommand Bean Validation 테스트")
class MessageSendCommandValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("정상적인 메시지는 검증을 통과해야 함")
    void validMessage_ShouldPassValidation() {
        // given
        MessageSendCommand command = MessageSendCommand.of("안녕하세요!");

        // when
        Set<ConstraintViolation<MessageSendCommand>> violations = validator.validate(command);

        // then
        assertTrue(violations.isEmpty());
        assertEquals("안녕하세요!", command.content());
    }

    @Test
    @DisplayName("null 메시지는 검증에 실패해야 함")
    void nullMessage_ShouldFailValidation() {
        // given
        MessageSendCommand command = MessageSendCommand.of(null);

        // when
        Set<ConstraintViolation<MessageSendCommand>> violations = validator.validate(command);

        // then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        
        ConstraintViolation<MessageSendCommand> violation = violations.iterator().next();
        assertEquals("메시지 내용은 비어있을 수 없습니다", violation.getMessage());
        assertEquals("content", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("빈 문자열 메시지는 검증에 실패해야 함")
    void emptyMessage_ShouldFailValidation() {
        // given
        MessageSendCommand command = MessageSendCommand.of("");

        // when
        Set<ConstraintViolation<MessageSendCommand>> violations = validator.validate(command);

        // then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        
        ConstraintViolation<MessageSendCommand> violation = violations.iterator().next();
        assertEquals("메시지 내용은 비어있을 수 없습니다", violation.getMessage());
    }

    @Test
    @DisplayName("공백만 있는 메시지는 검증에 실패해야 함")
    void blankMessage_ShouldFailValidation() {
        // given
        MessageSendCommand command = MessageSendCommand.of("   ");

        // when
        Set<ConstraintViolation<MessageSendCommand>> violations = validator.validate(command);

        // then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        
        ConstraintViolation<MessageSendCommand> violation = violations.iterator().next();
        assertEquals("메시지 내용은 비어있을 수 없습니다", violation.getMessage());
    }

    @Test
    @DisplayName("2000자를 초과하는 메시지는 검증에 실패해야 함")
    void tooLongMessage_ShouldFailValidation() {
        // given
        String longMessage = "A".repeat(2001);
        MessageSendCommand command = MessageSendCommand.of(longMessage);

        // when
        Set<ConstraintViolation<MessageSendCommand>> violations = validator.validate(command);

        // then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        
        ConstraintViolation<MessageSendCommand> violation = violations.iterator().next();
        assertEquals("메시지 내용은 2000자를 초과할 수 없습니다", violation.getMessage());
    }

    @Test
    @DisplayName("정확히 2000자 메시지는 검증을 통과해야 함")
    void exactlyMaxLengthMessage_ShouldPassValidation() {
        // given
        String maxMessage = "A".repeat(2000);
        MessageSendCommand command = MessageSendCommand.of(maxMessage);

        // when
        Set<ConstraintViolation<MessageSendCommand>> violations = validator.validate(command);

        // then
        assertTrue(violations.isEmpty());
        assertEquals(2000, command.content().length());
    }

    @Test
    @DisplayName("앞뒤 공백은 자동으로 제거되어야 함")
    void messageWithWhitespace_ShouldBeTrimmed() {
        // given
        MessageSendCommand command = MessageSendCommand.of("  안녕하세요!  ");

        // when
        // then
        assertEquals("안녕하세요!", command.content());
        assertEquals(6, command.content().length());
    }
}