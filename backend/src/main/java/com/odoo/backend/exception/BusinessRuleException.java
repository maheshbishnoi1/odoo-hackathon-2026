package com.odoo.backend.exception;

/**
 * Thrown when a domain / business rule is violated.
 * Examples:
 * <ul>
 *   <li>Creating maintenance on a RETIRED vehicle</li>
 *   <li>Creating a second active maintenance for a vehicle already IN_SHOP</li>
 *   <li>Recording fuel with a lower odometer reading than the previous log</li>
 *   <li>Creating an expense with a future date</li>
 * </ul>
 * Maps to HTTP 422 (Unprocessable Entity) in {@link com.odoo.backend.exception.GlobalExceptionHandler}.
 */
public class BusinessRuleException extends RuntimeException {

    /**
     * Constructs the exception with a descriptive message.
     *
     * @param message the business rule violation detail
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}
