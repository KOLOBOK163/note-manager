package com.ksbk.auth.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
