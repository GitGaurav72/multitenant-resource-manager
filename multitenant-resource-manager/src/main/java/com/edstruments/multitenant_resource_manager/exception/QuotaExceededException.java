package com.edstruments.multitenant_resource_manager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a quota or business limit is exceeded.
 * Automatically returns a 400 Bad Request HTTP status when thrown from a controller.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class QuotaExceededException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new QuotaExceededException with default message
     */
    public QuotaExceededException() {
        super("Business quota exceeded");
    }

    /**
     * Constructs a new QuotaExceededException with custom message
     * @param message the detail message
     */
    public QuotaExceededException(String message) {
        super(message);
    }

    /**
     * Constructs a new QuotaExceededException with custom message and cause
     * @param message the detail message
     * @param cause the root cause
     */
    public QuotaExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new QuotaExceededException with quota details
     * @param resourceType the type of resource that exceeded quota
     * @param current the current count
     * @param max the maximum allowed
     */
    public QuotaExceededException(String resourceType, long current, long max) {
        super(String.format("%s quota exceeded: %d/%d", resourceType, current, max));
    }
}