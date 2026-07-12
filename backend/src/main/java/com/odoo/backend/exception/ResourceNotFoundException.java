package com.odoo.backend.exception;

/**
 * Thrown when a requested resource is not found in the database.
 * Maps to HTTP 404 in {@link com.odoo.backend.exception.GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs the exception with a descriptive message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Convenience constructor for entity-by-ID lookups.
     *
     * @param entityName the entity class name (e.g., "MaintenanceRecord")
     * @param id         the ID that was not found
     */
    public ResourceNotFoundException(String entityName, Long id) {
        super(entityName + " not found with id: " + id);
    }
}
