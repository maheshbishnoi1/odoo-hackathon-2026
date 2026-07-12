package com.odoo.backend.exception;

import com.odoo.backend.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all REST controllers in TransitOps.
 *
 * <p>Maps application exceptions to structured {@link ApiResponse} objects with
 * appropriate HTTP status codes. All error responses follow the same envelope format,
 * making client-side error handling predictable.</p>
 *
 * <ul>
 *   <li>{@link ResourceNotFoundException}         → 404 Not Found</li>
 *   <li>{@link BusinessRuleException}             → 422 Unprocessable Entity</li>
 *   <li>{@link MethodArgumentNotValidException}   → 400 Bad Request (with field errors)</li>
 *   <li>{@link ConstraintViolationException}      → 400 Bad Request</li>
 *   <li>{@link HttpMessageNotReadableException}   → 400 Bad Request</li>
 *   <li>{@link Exception}                         → 500 Internal Server Error</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ------------------------------------------------------------------
    // 404 — Resource Not Found
    // ------------------------------------------------------------------

    /**
     * Handles {@link ResourceNotFoundException}.
     *
     * @param ex the exception
     * @return 404 response with error message
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ------------------------------------------------------------------
    // 422 — Business Rule Violation
    // ------------------------------------------------------------------

    /**
     * Handles {@link BusinessRuleException}.
     *
     * @param ex the exception
     * @return 422 response with violation detail
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRule(BusinessRuleException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ------------------------------------------------------------------
    // 400 — Validation Errors (Bean Validation on @RequestBody)
    // ------------------------------------------------------------------

    /**
     * Handles Jakarta Bean Validation failures on {@code @RequestBody} DTOs.
     * Returns a map of {@code field → error message} inside the {@link ApiResponse} data field.
     *
     * @param ex the validation exception
     * @return 400 response with field-level error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field   = (error instanceof FieldError fe) ? fe.getField() : error.getObjectName();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });

        log.warn("Validation failed: {}", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed. Please correct the highlighted fields.")
                        .data(fieldErrors)
                        .build());
    }

    // ------------------------------------------------------------------
    // 400 — Constraint Violations (path/query params)
    // ------------------------------------------------------------------

    /**
     * Handles {@link ConstraintViolationException} raised on path/query parameters.
     *
     * @param ex the exception
     * @return 400 response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ------------------------------------------------------------------
    // 400 — Unreadable / Malformed Request Body
    // ------------------------------------------------------------------

    /**
     * Handles malformed JSON or unreadable request body.
     *
     * @param ex the exception
     * @return 400 response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Malformed or unreadable request body."));
    }

    // ------------------------------------------------------------------
    // 400 — Missing or Wrong-Type Request Parameters
    // ------------------------------------------------------------------

    /**
     * Handles missing required request parameters.
     *
     * @param ex the exception
     * @return 400 response
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Required parameter '" + ex.getParameterName() + "' is missing."));
    }

    /**
     * Handles type mismatch on path/query parameters (e.g., passing a string where Long is expected).
     *
     * @param ex the exception
     * @return 400 response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'.", ex.getValue(), ex.getName());
        log.warn("Type mismatch: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    // ------------------------------------------------------------------
    // 500 — Catch-All
    // ------------------------------------------------------------------

    /**
     * Catch-all handler for unexpected errors.
     *
     * @param ex the exception
     * @return 500 response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please contact support."));
    }
}
