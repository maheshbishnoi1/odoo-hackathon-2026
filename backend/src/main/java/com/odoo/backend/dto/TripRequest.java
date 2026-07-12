package com.odoo.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * Trip Request DTO
 * ============================================================================
 *
 * DTO used for creating a new Trip.
 *
 * Business Rules:
 * • Vehicle ID is required.
 * • Driver ID is required.
 * • Source and Destination are required.
 * • Source and Destination cannot be the same (validated in service layer).
 * • Start time cannot be in the past (validated in service layer).
 * • Distance must be greater than zero.
 * • Estimated duration must be greater than zero.
 * • Trip Number is generated automatically by the system.
 *
 * ============================================================================
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRequest {

    /**
     * Vehicle ID
     */
    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    /**
     * Driver ID
     */
    @NotNull(message = "Driver ID is required")
    private Long driverId;

    /**
     * Trip Source
     */
    @NotBlank(message = "Source is required")
    @Size(max = 100, message = "Source cannot exceed 100 characters")
    private String source;

    /**
     * Trip Destination
     */
    @NotBlank(message = "Destination is required")
    @Size(max = 100, message = "Destination cannot exceed 100 characters")
    private String destination;

    /**
     * Planned Start Time
     */
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    /**
     * Estimated Distance (KM)
     */
    @NotNull(message = "Distance is required")
    @Positive(message = "Distance must be greater than zero")
    private Double distance;

    /**
     * Estimated Duration (Minutes)
     */
    @NotNull(message = "Estimated duration is required")
    @Positive(message = "Estimated duration must be greater than zero")
    private Integer estimatedDuration;

    /**
     * Additional Notes
     */
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}