package com.odoo.backend.entity;

import com.odoo.backend.enums.VehicleStatus;
import com.odoo.backend.enums.VehicleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * Vehicle Entity
 * ============================================================================
 *
 * Represents a fleet vehicle within TransitOps.
 *
 * Business Rules:
 * • Registration Number must be unique.
 * • Vehicle Status defaults to AVAILABLE.
 * • Capacity must be greater than zero.
 * • Odometer cannot be negative.
 *
 * ============================================================================
 */

@Entity
@Table(
        name = "vehicles",
        indexes = {
                @Index(name = "idx_vehicle_registration", columnList = "registrationNumber"),
                @Index(name = "idx_vehicle_status", columnList = "status"),
                @Index(name = "idx_vehicle_type", columnList = "vehicleType")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Vehicle {

    // =========================================================================
    // PRIMARY KEY
    // =========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================================
    // BASIC INFORMATION
    // =========================================================================

    @NotBlank(message = "Registration number is required")
    @Size(max = 30)
    @EqualsAndHashCode.Include
    @Column(name = "registration_number", nullable = false, unique = true, length = 30)
    private String registrationNumber;

    @NotBlank(message = "Vehicle name is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String vehicleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleType vehicleType;

    // =========================================================================
    // SPECIFICATIONS
    // =========================================================================

    @NotNull(message = "Maximum load capacity is required")
    @Positive(message = "Maximum load capacity must be greater than zero")
    @Column(nullable = false)
    private Double maximumLoadCapacity;

    @Builder.Default
    @PositiveOrZero(message = "Odometer cannot be negative")
    @Column(nullable = false)
    private Double odometer = 0.0;

    @PositiveOrZero(message = "Acquisition cost cannot be negative")
    private Double acquisitionCost;

    @Min(value = 1990)
    @Max(value = 2100)
    private Integer manufactureYear;

    @Size(max = 30)
    private String fuelType;

    @Size(max = 30)
    private String color;

    // Additional fields from Maintenance/Fuel/Expense stub
    @Column(name = "make", length = 100)
    private String make;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "year")
    private Integer year;

    // =========================================================================
    // DOCUMENTS
    // =========================================================================

    private LocalDate registrationExpiry;

    private LocalDate insuranceExpiry;

    // =========================================================================
    // STATUS
    // =========================================================================

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    // =========================================================================
    // AUDIT
    // =========================================================================

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ---- Bidirectional JPA Relationships (Lazy Loaded) ----

    /** Maintenance records associated with this vehicle. */
    @OneToMany(mappedBy = "vehicle", fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<MaintenanceRecord> maintenanceRecords = new java.util.ArrayList<>();

    /** Fuel logs associated with this vehicle. */
    @OneToMany(mappedBy = "vehicle", fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<FuelLog> fuelLogs = new java.util.ArrayList<>();

    /** Expenses associated with this vehicle. */
    @OneToMany(mappedBy = "vehicle", fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<Expense> expenses = new java.util.ArrayList<>();

    // =========================================================================
    // JPA LIFECYCLE CALLBACKS
    // =========================================================================

    @PrePersist
    protected void prePersist() {

        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = VehicleStatus.AVAILABLE;
        }

        if (this.odometer == null) {
            this.odometer = 0.0;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================================================================
    // BUSINESS METHODS
    // =========================================================================

    /**
     * Vehicle can be dispatched only when available.
     */
    public boolean canDispatch() {
        return status == VehicleStatus.AVAILABLE;
    }

    /**
     * Put vehicle on trip.
     */
    public void dispatch() {
        this.status = VehicleStatus.ON_TRIP;
    }

    /**
     * Mark vehicle available.
     */
    public void markAvailable() {

        if (this.status != VehicleStatus.RETIRED) {
            this.status = VehicleStatus.AVAILABLE;
        }
    }

    /**
     * Move vehicle to maintenance.
     */
    public void sendToMaintenance() {
        this.status = VehicleStatus.IN_SHOP;
    }

    /**
     * Retire vehicle permanently.
     */
    public void retire() {
        this.status = VehicleStatus.RETIRED;
    }

    /**
     * Update odometer after trip completion.
     */
    public void updateOdometer(Double newReading) {

        if (newReading != null && newReading >= this.odometer) {
            this.odometer = newReading;
        }
    }

    /**
     * Check if insurance is expired.
     */
    public boolean isInsuranceExpired() {

        return insuranceExpiry != null
                && insuranceExpiry.isBefore(LocalDate.now());
    }

    /**
     * Check if registration is expired.
     */
    public boolean isRegistrationExpired() {

        return registrationExpiry != null
                && registrationExpiry.isBefore(LocalDate.now());
    }

    /**
     * Vehicle is active in fleet.
     */
    public boolean isActive() {
        return status != VehicleStatus.RETIRED;
    }

    /**
     * Vehicle can be assigned to a trip.
     */
    public boolean isAvailable() {
        return status == VehicleStatus.AVAILABLE;
    }

    /**
     * Vehicle is currently under maintenance.
     */
    public boolean isInMaintenance() {
        return status == VehicleStatus.IN_SHOP;
    }

    /**
     * Vehicle is currently on a trip.
     */
    public boolean isOnTrip() {
        return status == VehicleStatus.ON_TRIP;
    }

    /**
     * Increment odometer after a completed trip.
     */
    public void addDistance(Double distance) {

        if (distance != null && distance > 0) {
            this.odometer += distance;
        }
    }

    /**
     * Checks whether the given cargo can be carried.
     */
    public boolean canCarry(Double cargoWeight) {

        if (cargoWeight == null) {
            return false;
        }

        return cargoWeight <= maximumLoadCapacity;
    }

    /**
     * Change vehicle status.
     */
    public void changeStatus(VehicleStatus status) {
        this.status = status;
    }
}
