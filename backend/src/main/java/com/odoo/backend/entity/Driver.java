package com.odoo.backend.entity;

import com.odoo.backend.enums.DriverStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * Driver Entity
 * ============================================================================
 *
 * Represents a driver in the TransitOps Fleet Management System.
 *
 * Business Rules:
 * • Email must be unique.
 * • Phone number must be unique.
 * • License number must be unique.
 * • License expiry date must be in the future.
 * • Driver must be at least 18 years old.
 * • Joining date cannot be in the future.
 * • Default status is AVAILABLE.
 *
 * ============================================================================
 */

@Entity
@Table(
        name = "drivers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_driver_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_driver_phone", columnNames = "phone"),
                @UniqueConstraint(name = "uk_driver_license", columnNames = "license_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Driver First Name
     */
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    /**
     * Driver Last Name
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * Email Address
     */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Mobile Number
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone number must be a valid 10-digit Indian mobile number"
    )
    @Column(nullable = false, unique = true, length = 10)
    private String phone;

    /**
     * Driving License Number
     */
    @NotBlank(message = "License number is required")
    @Size(max = 30, message = "License number cannot exceed 30 characters")
    @Column(name = "license_number", nullable = false, unique = true, length = 30)
    private String licenseNumber;

    /**
     * Driving License Expiry Date
     */
    @Future(message = "License expiry date must be in the future")
    @Column(name = "license_expiry_date", nullable = false)
    private LocalDate licenseExpiryDate;

    /**
     * Driver Date of Birth
     */
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /**
     * Joining Date
     */
    @PastOrPresent(message = "Joining date cannot be in the future")
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    /**
     * Driver Availability Status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DriverStatus status = DriverStatus.AVAILABLE;

    /**
     * Residential Address
     */
    @NotBlank(message = "Address is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    /**
     * Emergency Contact Name
     */
    @NotBlank(message = "Emergency contact name is required")
    @Column(name = "emergency_contact_name", nullable = false)
    private String emergencyContactName;

    /**
     * Emergency Contact Phone
     */
    @NotBlank(message = "Emergency contact phone is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Emergency contact phone must be a valid 10-digit Indian mobile number"
    )
    @Column(name = "emergency_contact_phone", nullable = false, length = 10)
    private String emergencyContactPhone;

    /**
     * Record Creation Timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Record Last Update Timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Automatically sets timestamps before insert.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = DriverStatus.AVAILABLE;
        }
    }

    /**
     * Automatically updates timestamp before update.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Returns driver's full name.
     *
     * @return Full name.
     */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Checks whether the driver's license is currently valid.
     *
     * @return true if license has not expired.
     */
    @Transient
    public boolean isLicenseValid() {
        return licenseExpiryDate != null &&
                !licenseExpiryDate.isBefore(LocalDate.now());
    }
}