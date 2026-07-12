package com.odoo.backend.dto.vehicle;

import com.odoo.backend.enums.VehicleStatus;
import com.odoo.backend.enums.VehicleType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * ============================================================================
 * Vehicle Request DTO
 * ============================================================================
 *
 * Used for:
 * • Create Vehicle
 * • Update Vehicle
 *
 * ============================================================================
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRequestDTO {

    // =========================================================================
    // BASIC INFORMATION
    // =========================================================================

    @NotBlank(message = "Registration number is required")
    @Size(max = 30, message = "Registration number cannot exceed 30 characters")
    private String registrationNumber;

    @NotBlank(message = "Vehicle name is required")
    @Size(max = 100, message = "Vehicle name cannot exceed 100 characters")
    private String vehicleName;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    // =========================================================================
    // SPECIFICATIONS
    // =========================================================================

    @NotNull(message = "Maximum load capacity is required")
    @Positive(message = "Maximum load capacity must be greater than zero")
    private Double maximumLoadCapacity;

    @NotNull(message = "Odometer is required")
    @PositiveOrZero(message = "Odometer cannot be negative")
    private Double odometer;

    @PositiveOrZero(message = "Acquisition cost cannot be negative")
    private Double acquisitionCost;

    @Min(value = 1990, message = "Manufacture year is invalid")
    @Max(value = 2100, message = "Manufacture year is invalid")
    private Integer manufactureYear;

    // =========================================================================
    // EXTRA INFORMATION
    // =========================================================================

    @Size(max = 30)
    private String fuelType;

    @Size(max = 30)
    private String color;

    private LocalDate registrationExpiry;

    private LocalDate insuranceExpiry;

    // =========================================================================
    // STATUS
    // =========================================================================

    /**
     * Optional while creating.
     * Defaults to AVAILABLE if not provided.
     */
    private VehicleStatus status;

}