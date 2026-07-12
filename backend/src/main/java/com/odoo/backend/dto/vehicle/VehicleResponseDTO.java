package com.odoo.backend.dto.vehicle;

import com.odoo.backend.entity.Vehicle;
import com.odoo.backend.enums.VehicleStatus;
import com.odoo.backend.enums.VehicleType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * Vehicle Response DTO
 * ============================================================================
 *
 * Returned to the client for all Vehicle APIs.
 *
 * ============================================================================
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponseDTO {

    // =========================================================================
    // BASIC INFORMATION
    // =========================================================================

    private Long id;

    private String registrationNumber;

    private String vehicleName;

    private VehicleType vehicleType;

    // =========================================================================
    // SPECIFICATIONS
    // =========================================================================

    private Double maximumLoadCapacity;

    private Double odometer;

    private Double acquisitionCost;

    private Integer manufactureYear;

    private String fuelType;

    private String color;

    // =========================================================================
    // DOCUMENTS
    // =========================================================================

    private LocalDate registrationExpiry;

    private LocalDate insuranceExpiry;

    // =========================================================================
    // STATUS
    // =========================================================================

    private VehicleStatus status;

    // =========================================================================
    // AUDIT
    // =========================================================================

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // =========================================================================
    // STATIC MAPPER
    // =========================================================================

    public static VehicleResponseDTO from(Vehicle vehicle) {

        if (vehicle == null) {
            return null;
        }

        return VehicleResponseDTO.builder()
                .id(vehicle.getId())
                .registrationNumber(vehicle.getRegistrationNumber())
                .vehicleName(vehicle.getVehicleName())
                .vehicleType(vehicle.getVehicleType())
                .maximumLoadCapacity(vehicle.getMaximumLoadCapacity())
                .odometer(vehicle.getOdometer())
                .acquisitionCost(vehicle.getAcquisitionCost())
                .manufactureYear(vehicle.getManufactureYear())
                .fuelType(vehicle.getFuelType())
                .color(vehicle.getColor())
                .registrationExpiry(vehicle.getRegistrationExpiry())
                .insuranceExpiry(vehicle.getInsuranceExpiry())
                .status(vehicle.getStatus())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}