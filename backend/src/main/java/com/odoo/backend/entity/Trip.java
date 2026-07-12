package com.odoo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a vehicle trip in TransitOps.
 *
 * <p><strong>STUB — Replace with the full Trip entity from the Trip module owner.</strong>
 * This stub contains only the fields required by the Fuel and Expense modules.
 * Merge carefully to avoid losing relationships defined here.</p>
 *
 * <p>Relationships owned by this entity (relevant to my modules):</p>
 * <ul>
 *   <li>{@link FuelLog} — OneToMany (mappedBy = "trip")</li>
 *   <li>{@link Expense} — OneToMany (mappedBy = "trip")</li>
 * </ul>
 */
@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    /** Primary key — auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique trip identifier / reference number.
     * Used for display in FuelLog and Expense responses.
     */
    @Column(name = "trip_number", nullable = false, unique = true, length = 50)
    private String tripNumber;

    /**
     * The vehicle assigned to this trip.
     * Fetched lazily to avoid N+1 issues.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    /** Trip origin location. */
    @Column(name = "origin", length = 255)
    private String origin;

    /** Trip destination location. */
    @Column(name = "destination", length = 255)
    private String destination;

    /** Scheduled or actual trip date. */
    @Column(name = "trip_date")
    private LocalDate tripDate;

    /** Record creation timestamp — managed by Hibernate. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Last update timestamp — managed by Hibernate. */
    @UpdateTimestamp
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
}
