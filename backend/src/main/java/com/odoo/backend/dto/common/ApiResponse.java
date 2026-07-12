package com.odoo.backend.dto.common;

import lombok.*;

/**
 * ============================================================================
 * Standard API Response
 * ============================================================================
 *
 * Used by all REST APIs to provide a consistent response format.
 *
 * Example Success:
 *
 * {
 *   "success": true,
 *   "message": "Login successful.",
 *   "data": { ... },
 *   "timestamp": "2026-07-12T12:30:15"
 * }
 *
 * Example Error:
 *
 * {
 *   "success": false,
 *   "message": "Invalid credentials.",
 *   "data": null,
 *   "timestamp": "2026-07-12T12:30:15"
 * }
 *
 * ============================================================================
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    // =========================================================================
    // RESPONSE FIELDS
    // =========================================================================

    /**
     * Indicates whether the request was successful.
     */
    private boolean success;

    /**
     * Response message.
     */
    private String message;

    /**
     * Response data.
     */
    private T data;

    /**
     * Response timestamp.
     */
    @Builder.Default
    private String timestamp = java.time.LocalDateTime.now().toString();

    // =========================================================================
    // SUCCESS RESPONSE
    // =========================================================================

    public static <T> ApiResponse<T> success(String message, T data) {

        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {

        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
    }

    public static ApiResponse<Void> success(String message) {

        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    // =========================================================================
    // ERROR RESPONSE
    // =========================================================================

    public static <T> ApiResponse<T> error(String message) {

        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, T data) {

        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }

}