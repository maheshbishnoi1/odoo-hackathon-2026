package com.odoo.backend.entity;

import com.odoo.backend.enums.TripStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * Trip Entity
 * ============================================================================
 *
 * Represents a vehicle trip in the TransitOps Fleet Management System.
 *
 * Business Rules:
 * • Trip Number must be unique.
 * • Vehicle must exist.
 * • Driver must exist.
 * • Source and destination cannot be empty.
 * • Start time must be present or future.
 * • Default status is SCHEDULED.
 *
 * ============================================================================
 */

@Entity
@Table(
        name = "trips",
        indexes = {
                @Index(name = "idx_trip_status", columnList = "status"),
                @Index(name = "idx_trip_vehicle", columnList = "vehicle_id"),
                @Index(name = "idx_trip_driver", columnList = "driver_id"),
                @Index(name = "idx_trip_start_time", columnList = "start_time")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trip_number",
                        columnNames = "trip_number"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique Trip Number
     * Generated automatically by the service layer.
     */
    @Column(
            name = "trip_number",
            nullable = false,
            unique = true,
            length = 30
    )
    private String tripNumber;

    /**
     * Assigned Vehicle
     */
    @NotNull(message = "Vehicle is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "vehicle_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trip_vehicle")
    )
    private Vehicle vehicle;

    /**
     * Assigned Driver
     */
    @NotNull(message = "Driver is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "driver_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trip_driver")
    )
    private Driver driver;

    /**
     * Trip Source
     */
    @NotBlank(message = "Source is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String source;

    /**
     * Trip Destination
     */
    @NotBlank(message = "Destination is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String destination;

    /**
     * Planned Start Time
     */
    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Start time must be present or future")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * Trip End Time
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * Total Distance (KM)
     */
    @NotNull(message = "Distance is required")
    @Positive(message = "Distance must be greater than zero")
    @Column(nullable = false)
    private Double distance;

    /**
     * Estimated Duration (Minutes)
     */
    @NotNull(message = "Estimated duration is required")
    @Positive(message = "Estimated duration must be greater than zero")
    @Column(name = "estimated_duration", nullable = false)
    private Integer estimatedDuration;

    /**
     * Actual Duration (Minutes)
     */
    @Column(name = "actual_duration")
    private Integer actualDuration;

    /**
     * Trip Status
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private TripStatus status = TripStatus.SCHEDULED;

    /**
     * Additional Notes
     */
    @Size(max = 500)
    @Column(length = 500)
    private String notes;

    /**
     * Record Creation Time
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Record Last Updated Time
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---- Bidirectional JPA Relationships (Lazy Loaded) ----

    /** Fuel logs associated with this trip. */
    @OneToMany(mappedBy = "trip", fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<FuelLog> fuelLogs = new java.util.ArrayList<>();

    /** Expenses associated with this trip. */
    @OneToMany(mappedBy = "trip", fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<Expense> expenses = new java.util.ArrayList<>();

    /**
     * Automatically executed before insert.
     */
    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = TripStatus.SCHEDULED;
        }
    }

    /**
     * Automatically executed before update.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Returns true if trip is scheduled.
     */
    @Transient
    public boolean isScheduled() {
        return status == TripStatus.SCHEDULED;
    }

    /**
     * Returns true if trip is active.
     */
    @Transient
    public boolean isActive() {
        return status == TripStatus.IN_PROGRESS;
    }

    /**
     * Returns true if trip has completed.
     */
    @Transient
    public boolean isCompleted() {
        return status == TripStatus.COMPLETED;
    }

    /**
     * Returns true if trip is cancelled.
     */
    @Transient
    public boolean isCancelled() {
        return status == TripStatus.CANCELLED;
    }

    /**
     * Calculates actual trip duration in minutes.
     */
    @Transient
    public long getTripDurationMinutes() {

        if (startTime == null || endTime == null) {
            return 0;
        }

        return Duration.between(startTime, endTime).toMinutes();
    }
}
