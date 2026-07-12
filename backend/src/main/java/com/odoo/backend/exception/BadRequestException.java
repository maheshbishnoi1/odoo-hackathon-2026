package com.odoo.backend.exception;

import lombok.Getter;

/**
 * ============================================================================
 * Bad Request Exception
 * ============================================================================
 *
 * Thrown when the client sends an invalid request.
 *
 * Examples:
 * - Email already exists
 * - Phone number already exists
 * - Invalid credentials
 * - Invalid vehicle status
 * - Invalid trip request
 *
 * ============================================================================
 */

@Getter
public class BadRequestException extends RuntimeException {

    /**
     * Creates a new BadRequestException with a custom message.
     *
     * @param message Error message
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Creates a new BadRequestException with a custom message
     * and root cause.
     *
     * @param message Error message
     * @param cause Root cause
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}