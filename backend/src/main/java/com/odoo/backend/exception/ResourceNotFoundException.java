package com.odoo.backend.exception;

import lombok.Getter;

/**
 * Base exception thrown when a requested resource
 * does not exist in the system.
 *
 * Examples:
 * - User not found
 * - Vehicle not found
 * - Driver not found
 * - Trip not found
 * - Maintenance record not found
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * Constructor with detailed information.
     *
     * Example:
     * throw new ResourceNotFoundException(
     *      "Vehicle",
     *      "id",
     *      5L
     * );
     */
    public ResourceNotFoundException(
            String resourceName,
            String fieldName,
            Object fieldValue
    ) {

        super(String.format(
                "%s not found with %s : '%s'",
                resourceName,
                fieldName,
                fieldValue
        ));

        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Constructor with custom message.
     */
    public ResourceNotFoundException(String message) {
        super(message);

        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

}