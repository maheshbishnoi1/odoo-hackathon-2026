package com.odoo.backend.exception;

/**
 * ============================================================================
 * Driver Not Found Exception
 * ============================================================================
 *
 * Thrown when a requested Driver cannot be found in the database.
 *
 * ============================================================================
 */
public class DriverNotFoundException extends RuntimeException {

    /**
     * Constructs a new DriverNotFoundException with the specified message.
     *
     * @param message Exception message.
     */
    public DriverNotFoundException(String message) {
        super(message);
    }
}