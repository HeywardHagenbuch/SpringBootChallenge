package com.hln.challenge.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BadRequestExceptionTest {

    @Test
    void testExceptionMessage() {
        String errorMessage = "Test error message";
        BadRequestException exception = new BadRequestException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}
