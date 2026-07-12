package com.odoo.backend.dto;

import com.odoo.backend.enums.ExpenseType;
import com.odoo.backend.enums.PaymentMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for {@link com.odoo.backend.entity.Expense}.
 *
 * <p>Flat projection — no entity references exposed.
 * Vehicle and Trip information is embedded as flat fields to prevent circular
 * references and lazy-loading issues in JSON serialization.</p>
 */
@Schema(description = "Expense record details returned by the API")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponseDTO {

    /** Unique identifier of the expense record. */
    @Schema(description = "Expense ID", example = "20")
    private Long id;

    // ---- Vehicle summary ----

    /** ID of the associated vehicle. */
    @Schema(description = "Vehicle ID", example = "1")
    private Long vehicleId;

    /** Registration number of the associated vehicle. */
    @Schema(description = "Vehicle registration number", example = "MH12AB1234")
    private String vehicleRegistrationNumber;

    // ---- Trip summary (nullable) ----

    /** ID of the associated trip, or {@code null} if not linked. */
    @Schema(description = "Trip ID (nullable)", example = "5")
    private Long tripId;

    /** Trip reference number, or {@code null} if not linked. */
    @Schema(description = "Trip number (nullable)", example = "TRP-2026-005")
    private String tripNumber;

    // ---- Expense fields ----

    /** Category of the expense. */
    @Schema(description = "Expense type", example = "TOLL")
    private ExpenseType expenseType;

    /** Short title of the expense. */
    @Schema(description = "Expense title", example = "Mumbai-Pune Expressway Toll")
    private String title;

    /** Detailed description. */
    @Schema(description = "Expense description")
    private String description;

    /** Expense amount. */
    @Schema(description = "Expense amount (INR)", example = "250.00")
    private BigDecimal amount;

    /** Date the expense was incurred. */
    @Schema(description = "Date of expense", example = "2026-07-10")
    private LocalDate expenseDate;

    /** Payment mode used. */
    @Schema(description = "Payment mode", example = "CASH")
    private PaymentMode paymentMode;

    /** Additional remarks. */
    @Schema(description = "Remarks", example = "Paid at Khopoli toll plaza")
    private String remarks;

    /** Record creation timestamp. */
    @Schema(description = "Record created at", example = "2026-07-10T12:00:00")
    private LocalDateTime createdAt;

    /** Last update timestamp. */
    @Schema(description = "Record last updated at", example = "2026-07-10T12:00:00")
    private LocalDateTime updatedAt;
}
