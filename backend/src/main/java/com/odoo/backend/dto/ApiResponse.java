package com.odoo.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper used across all TransitOps endpoints.
 *
 * <p>Every REST endpoint returns this envelope:</p>
 * <pre>
 * {
 *   "success"   : true | false,
 *   "message"   : "human-readable message",
 *   "data"      : { ... } | null,
 *   "timestamp" : "2026-07-12T10:00:00"
 * }
 * </pre>
 *
 * @param <T> the type of the payload
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** Whether the request succeeded. */
    private boolean success;

    /** Human-readable status message. */
    private String message;

    /** The response payload — omitted (null) on error responses. */
    private T data;

    /** Server-side timestamp of the response. */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // -------------------------------------------------------
    // Factory helpers
    // -------------------------------------------------------

    /**
     * Creates a success response with data and message.
     *
     * @param message descriptive message
     * @param data    the payload
     * @param <T>     payload type
     * @return success {@link ApiResponse}
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a success response without a data payload (e.g., delete).
     *
     * @param message descriptive message
     * @param <T>     payload type
     * @return success {@link ApiResponse}
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response.
     *
     * @param message error description
     * @param <T>     payload type
     * @return error {@link ApiResponse}
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
