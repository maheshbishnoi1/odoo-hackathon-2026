package com.odoo.backend.entity;

import com.odoo.backend.enums.MaintenanceStatus;
import com.odoo.backend.enums.MaintenanceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a maintenance record for a fleet vehicle in TransitOps.
 *
 * <p>Business rules enforced at the service layer:</p>
 * <ul>
 *   <li>A RETIRED vehicle cannot receive new maintenance records.</li>
 *   <li>A vehicle already IN_SHOP (active maintenance) cannot receive another active record.</li>
 *   <li>Creating a record sets {@link Vehicle#status} → {@code IN_SHOP}.</li>
 *   <li>Completing a record sets {@link Vehicle#status} → {@code AVAILABLE}.</li>
 * </ul>
 */
@Entity
@Table(
        name = "maintenance_records",
        indexes = {
                @Index(name = "idx_maintenance_vehicle_id", columnList = "vehicle_id"),
                @Index(name = "idx_maintenance_status",     columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecord {

    /** Primary key — auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The vehicle this maintenance record belongs to.
     * Fetched lazily to avoid unnecessary joins.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    /**
     * Category of maintenance performed.
     *
     * @see MaintenanceType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "maintenance_type", nullable = false, length = 30)
    private MaintenanceType maintenanceType;

    /** Detailed description of the maintenance work to be or being performed. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Estimated cost before the work is performed (optional). */
    @Column(name = "estimated_cost", precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    /** Actual final cost once maintenance is completed. */
    @Column(name = "actual_cost", precision = 12, scale = 2)
    private BigDecimal actualCost;

    /** Date when maintenance work started. */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Date when maintenance was completed. Null if still in progress. */
    @Column(name = "completion_date")
    private LocalDate completionDate;

    /**
     * Current status of the maintenance lifecycle.
     * Defaults to {@link MaintenanceStatus#OPEN} on creation.
     *
     * @see MaintenanceStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MaintenanceStatus status = MaintenanceStatus.OPEN;

    /** Additional notes or remarks about the maintenance. */
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
