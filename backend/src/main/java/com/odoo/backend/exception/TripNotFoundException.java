package com.odoo.backend.exception;

/**
 * ============================================================================
 * Trip Not Found Exception
 * ============================================================================
 *
 * Thrown when a requested Trip cannot be found.
 *
 * ============================================================================
 */
public class TripNotFoundException extends RuntimeException {

    /**
     * Constructs a new TripNotFoundException.
     *
     * @param message Exception message.
     */
    public TripNotFoundException(String message) {
        super(message);
    }

}