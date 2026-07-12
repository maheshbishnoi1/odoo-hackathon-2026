package com.odoo.backend.dto;

import com.odoo.backend.enums.ExpenseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Summary DTO for expense aggregation per vehicle.
 * Returned by the {@code GET /api/v1/expenses/vehicle/{vehicleId}/summary} endpoint.
 */
@Schema(description = "Expense summary for a vehicle, grouped by expense type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSummaryDTO {

    /** ID of the vehicle. */
    @Schema(description = "Vehicle ID", example = "1")
    private Long vehicleId;

    /** Registration number of the vehicle. */
    @Schema(description = "Vehicle registration number", example = "MH12AB1234")
    private String vehicleRegistrationNumber;

    /** Total number of expense records for this vehicle. */
    @Schema(description = "Total number of expense records", example = "8")
    private long totalExpenses;

    /** Total expense amount across all records. */
    @Schema(description = "Total expense amount (INR)", example = "12500.00")
    private BigDecimal totalAmount;

    /**
     * Breakdown of expense totals by expense type.
     * Keys are {@link ExpenseType} names; values are total amounts.
     */
    @Schema(description = "Amount breakdown by expense type",
            example = "{\"TOLL\": 500.00, \"MAINTENANCE\": 7000.00, \"FUEL\": 5000.00}")
    private Map<ExpenseType, BigDecimal> amountByType;
}
