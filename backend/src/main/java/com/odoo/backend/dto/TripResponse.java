package com.odoo.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.odoo.backend.enums.TripStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * Trip Response DTO
 * ============================================================================
 *
 * DTO returned by Trip APIs.
 *
 * Purpose:
 * • Prevents exposing the Trip entity directly.
 * • Provides a clean and structured API response.
 * • Includes summary information for Vehicle and Driver.
 *
 * ============================================================================
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripResponse {

    /**
     * Trip ID
     */
    private Long id;

    /**
     * Unique Trip Number
     */
    private String tripNumber;

    /**
     * Vehicle ID
     */
    private Long vehicleId;

    /**
     * Vehicle Registration Number
     */
    private String vehicleRegistrationNumber;

    /**
     * Vehicle Model
     */
    private String vehicleModel;

    /**
     * Driver ID
     */
    private Long driverId;

    /**
     * Driver Full Name
     */
    private String driverName;

    /**
     * Driver License Number
     */
    private String driverLicenseNumber;

    /**
     * Trip Source
     */
    private String source;

    /**
     * Trip Destination
     */
    private String destination;

    /**
     * Planned Start Time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * Actual End Time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * Distance (KM)
     */
    private Double distance;

    /**
     * Estimated Duration (Minutes)
     */
    private Integer estimatedDuration;

    /**
     * Actual Duration (Minutes)
     */
    private Integer actualDuration;

    /**
     * Trip Status
     */
    private TripStatus status;

    /**
     * Additional Notes
     */
    private String notes;

    /**
     * Record Creation Time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Record Last Updated Time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}