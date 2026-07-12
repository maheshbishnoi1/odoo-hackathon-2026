package com.odoo.backend.dto;

import com.odoo.backend.enums.DriverStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * ============================================================================
 * Driver Request DTO
 * ============================================================================
 *
 * DTO used for creating a new Driver.
 *
 * Business Rules:
 * • First name and last name are mandatory.
 * • Email must be valid.
 * • Phone number must be a valid Indian mobile number.
 * • License number must be unique.
 * • License expiry date must be in the future.
 * • Driver must be at least 18 years old (validated in service layer).
 * • Joining date cannot be in the future.
 * • Status defaults to AVAILABLE if not provided.
 *
 * ============================================================================
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverRequest {

    /**
     * Driver First Name
     */
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    /**
     * Driver Last Name
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    /**
     * Driver Email
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    /**
     * Driver Phone Number
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone number must be a valid 10-digit Indian mobile number"
    )
    private String phone;

    /**
     * Driving License Number
     */
    @NotBlank(message = "License number is required")
    @Size(max = 30, message = "License number cannot exceed 30 characters")
    private String licenseNumber;

    /**
     * Driving License Expiry Date
     */
    @NotNull(message = "License expiry date is required")
    @Future(message = "License expiry date must be in the future")
    private LocalDate licenseExpiryDate;

    /**
     * Driver Date of Birth
     */
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    /**
     * Joining Date
     */
    @NotNull(message = "Joining date is required")
    @PastOrPresent(message = "Joining date cannot be in the future")
    private LocalDate joiningDate;

    /**
     * Driver Status
     * Defaults to AVAILABLE if not supplied.
     */
    @Builder.Default
    private DriverStatus status = DriverStatus.AVAILABLE;

    /**
     * Residential Address
     */
    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    /**
     * Emergency Contact Name
     */
    @NotBlank(message = "Emergency contact name is required")
    @Size(max = 100, message = "Emergency contact name cannot exceed 100 characters")
    private String emergencyContactName;

    /**
     * Emergency Contact Phone
     */
    @NotBlank(message = "Emergency contact phone is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Emergency contact phone must be a valid 10-digit Indian mobile number"
    )
    private String emergencyContactPhone;
}