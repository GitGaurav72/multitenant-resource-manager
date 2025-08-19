package com.edstruments.multitenant_resource_manager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource is not found in the system.
 * Automatically returns a 404 Not Found HTTP status when thrown from a controller.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ResourceNotFoundException with default message
     */
    public ResourceNotFoundException() {
        super("Requested resource not found");
    }

    /**
     * Constructs a new ResourceNotFoundException with custom message
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with custom message and cause
     * @param message the detail message
     * @param cause the root cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ResourceNotFoundException with formatted message
     * @param resourceName the name/type of resource (e.g., "User", "Resource")
     * @param fieldName the name of the field being searched
     * @param fieldValue the value of the field being searched
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}