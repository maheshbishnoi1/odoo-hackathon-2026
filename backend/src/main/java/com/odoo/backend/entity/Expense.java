package com.odoo.backend.entity;

import com.odoo.backend.enums.ExpenseType;
import com.odoo.backend.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a vehicle-related expense in TransitOps.
 *
 * <p>Business rules enforced at the service layer:</p>
 * <ul>
 *   <li>Amount must be greater than zero.</li>
 *   <li>Expense date cannot be in the future.</li>
 *   <li>Trip association is optional; when provided the trip must exist.</li>
 * </ul>
 */
@Entity
@Table(
        name = "expenses",
        indexes = {
                @Index(name = "idx_expense_vehicle_id",   columnList = "vehicle_id"),
                @Index(name = "idx_expense_trip_id",      columnList = "trip_id"),
                @Index(name = "idx_expense_type",         columnList = "expense_type"),
                @Index(name = "idx_expense_date",         columnList = "expense_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    /** Primary key — auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The vehicle this expense belongs to.
     * Fetched lazily.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    /**
     * The trip associated with this expense (optional).
     * Fetched lazily.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    /**
     * Category of the expense.
     *
     * @see ExpenseType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type", nullable = false, length = 20)
    private ExpenseType expenseType;

    /**
     * Short title / label for the expense (e.g., "Mumbai-Pune Toll").
     */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * Detailed description of the expense.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Expense amount. Must be greater than zero.
     */
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * Date on which the expense was incurred.
     * Cannot be in the future.
     */
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    /**
     * Mode of payment used to settle the expense.
     *
     * @see PaymentMode
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 20)
    private PaymentMode paymentMode;

    /** Additional notes about the expense. */
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
