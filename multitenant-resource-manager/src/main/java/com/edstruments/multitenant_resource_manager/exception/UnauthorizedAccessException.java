package com.edstruments.multitenant_resource_manager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to perform an action they don't have permission for.
 * Automatically returns a 403 Forbidden HTTP status when thrown from a controller.
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new UnauthorizedAccessException with default message
     */
    public UnauthorizedAccessException() {
        super("Access denied");
    }
    /**
     * Constructs a new UnauthorizedAccessException with custom message and cause
     * @param message the detail message
     * @param cause the root cause
     */
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new UnauthorizedAccessException for role-based access failures
     * @param requiredRole the role required to access the resource
     */
    public UnauthorizedAccessException(String requiredRole) {
        super(String.format("Access denied. Required role: %s", requiredRole));
    }
}