package com.odoo.backend.entity;

import com.odoo.backend.enums.FuelType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a fuel fill-up event for a fleet vehicle in TransitOps.
 *
 * <p>Business rules enforced at the service layer:</p>
 * <ul>
 *   <li>Fuel quantity must be greater than zero.</li>
 *   <li>Fuel cost must be positive.</li>
 *   <li>Odometer reading cannot be less than the previous reading for the same vehicle.</li>
 *   <li>{@link #costPerLiter} is automatically computed as {@code cost / quantity}.</li>
 *   <li>Trip association is optional.</li>
 * </ul>
 */
@Entity
@Table(
        name = "fuel_logs",
        indexes = {
                @Index(name = "idx_fuel_log_vehicle_id", columnList = "vehicle_id"),
                @Index(name = "idx_fuel_log_trip_id",    columnList = "trip_id"),
                @Index(name = "idx_fuel_log_fuel_date",  columnList = "fuel_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelLog {

    /** Primary key — auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The vehicle this fuel log belongs to.
     * Fetched lazily to avoid unnecessary joins.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    /**
     * The trip during which fuel was purchased (optional).
     * Fetched lazily.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    /** Date when fuel was purchased. */
    @Column(name = "fuel_date", nullable = false)
    private LocalDate fuelDate;

    /**
     * Type of fuel purchased.
     *
     * @see FuelType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 20)
    private FuelType fuelType;

    /**
     * Volume of fuel purchased in litres (or kWh for electric).
     * Must be greater than zero.
     */
    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    /**
     * Total cost of fuel purchased.
     * Must be positive.
     */
    @Column(name = "cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal cost;

    /**
     * Name of the fuel vendor / petrol station.
     */
    @Column(name = "vendor", length = 200)
    private String vendor;

    /**
     * Vehicle odometer reading at the time of fuelling (in kilometres).
     * Must be greater than the previous odometer reading for this vehicle.
     */
    @Column(name = "odometer_reading", nullable = false)
    private Long odometerReading;

    /**
     * Cost per litre — automatically calculated by the service layer as {@code cost / quantity}.
     * Stored for reporting convenience.
     */
    @Column(name = "cost_per_liter", precision = 10, scale = 4)
    private BigDecimal costPerLiter;

    /** Additional notes or remarks about the fuel purchase. */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    /** Record creation timestamp — managed by Hibernate. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Last update timestamp — managed by Hibernate. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
