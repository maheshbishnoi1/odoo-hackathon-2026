package com.odoo.backend.entity;

import com.odoo.backend.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a fleet vehicle in TransitOps.
 *
 * <p><strong>STUB — Replace with the full Vehicle entity from the Vehicle module owner.</strong>
 * This stub contains only the fields required by the Maintenance, Fuel, and Expense modules.
 * Merge carefully to avoid losing relationships defined here.</p>
 *
 * <p>Relationships owned by this entity:</p>
 * <ul>
 *   <li>{@link MaintenanceRecord} — OneToMany (mappedBy = "vehicle")</li>
 *   <li>{@link FuelLog}           — OneToMany (mappedBy = "vehicle")</li>
 *   <li>{@link Expense}           — OneToMany (mappedBy = "vehicle")</li>
 * </ul>
 */
@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    /** Primary key — auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique vehicle registration / licence plate number.
     * Required by multiple modules for display purposes.
     */
    @Column(name = "registration_number", nullable = false, unique = true, length = 50)
    private String registrationNumber;

    /**
     * Current operational status of the vehicle.
     * Mutated by {@link com.odoo.backend.service.MaintenanceService}:
     * <ul>
     *   <li>On maintenance create → {@link VehicleStatus#IN_SHOP}</li>
     *   <li>On maintenance complete → {@link VehicleStatus#AVAILABLE}</li>
     * </ul>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    /** Make / manufacturer of the vehicle (e.g., Toyota, Tata). */
    @Column(name = "make", length = 100)
    private String make;

    /** Model name of the vehicle. */
    @Column(name = "model", length = 100)
    private String model;

    /** Four-digit manufacture year. */
    @Column(name = "year")
    private Integer year;

    /** Record creation timestamp — managed by Hibernate. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Last update timestamp — managed by Hibernate. */
    @UpdateTimestamp
    @Column(name = "updated_at")
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
}
