package com.example.aquaventure.exception;

/**
 * Generic 409 for conflicts that don't warrant their own exception type
 * (e.g. slot capacity full, overlapping lesson slot).
 */
public class ConflictException extends RuntimeException {

    private final String code;

    public ConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
