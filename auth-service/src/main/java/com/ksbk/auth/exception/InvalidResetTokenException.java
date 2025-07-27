package com.ksbk.auth.exception;

public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException(String message) {
        super(message);
    }
}
