package com.odoo.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.odoo.backend.enums.DriverStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * Driver Response DTO
 * ============================================================================
 *
 * DTO returned by Driver APIs.
 *
 * Purpose:
 * • Prevents exposing the entity directly.
 * • Provides a clean API response.
 * • Includes computed fields such as full name and license validity.
 *
 * ============================================================================
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponse {

    /**
     * Driver ID
     */
    private Long id;

    /**
     * Driver First Name
     */
    private String firstName;

    /**
     * Driver Last Name
     */
    private String lastName;

    /**
     * Driver Full Name
     */
    private String fullName;

    /**
     * Email Address
     */
    private String email;

    /**
     * Phone Number
     */
    private String phone;

    /**
     * License Number
     */
    private String licenseNumber;

    /**
     * License Expiry Date
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate licenseExpiryDate;

    /**
     * Whether the license is currently valid.
     */
    private Boolean licenseValid;

    /**
     * Date of Birth
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    /**
     * Joining Date
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate joiningDate;

    /**
     * Driver Status
     */
    private DriverStatus status;

    /**
     * Residential Address
     */
    private String address;

    /**
     * Emergency Contact Name
     */
    private String emergencyContactName;

    /**
     * Emergency Contact Phone
     */
    private String emergencyContactPhone;

    /**
     * Record Creation Time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Last Updated Time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}