package com.odoo.backend.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Report DTO providing vehicle-wise general expense breakdown and fleet summary.
 * Returned by {@code GET /api/v1/reports/vehicle-expenses}.
 */
@Schema(description = "Vehicle-wise general expense report with fleet summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleExpenseSummaryReportDTO {

    /** Optional start date filter applied to the report. */
    @Schema(description = "Report start date filter (inclusive)", example = "2026-07-01")
    private LocalDate fromDate;

    /** Optional end date filter applied to the report. */
    @Schema(description = "Report end date filter (inclusive)", example = "2026-07-31")
    private LocalDate toDate;

    /** Total general expense expenditure across all vehicles in the report period. */
    @Schema(description = "Total expense amount across the fleet (INR)", example = "64000.00")
    private BigDecimal totalFleetExpenseAmount;

    /** List of per-vehicle expense aggregations. */
    @Schema(description = "Per-vehicle general expense breakdown")
    private List<VehicleExpenseItem> vehicleExpenses;

    /**
     * Item DTO representing general expense aggregation for a single vehicle.
     */
    @Schema(description = "General expense summary for a single vehicle")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleExpenseItem {

        @Schema(description = "Vehicle ID", example = "1")
        private Long vehicleId;

        @Schema(description = "Vehicle registration number", example = "MH12AB1234")
        private String vehicleRegistrationNumber;

        @Schema(description = "Total number of expense records", example = "7")
        private long expenseCount;

        @Schema(description = "Total general expense amount incurred (INR)", example = "15000.00")
        private BigDecimal totalExpenseAmount;
    }
}
