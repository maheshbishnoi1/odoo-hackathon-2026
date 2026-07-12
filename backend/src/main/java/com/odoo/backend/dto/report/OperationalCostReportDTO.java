package com.odoo.backend.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Combined operational cost report DTO aggregating Fuel, Maintenance, and General Expenses.
 * Returned by {@code GET /api/v1/reports/operational-costs}.
 */
@Schema(description = "Comprehensive operational cost report combining Fuel, Maintenance, and Expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationalCostReportDTO {

    /** Optional start date filter applied to the report. */
    @Schema(description = "Report start date filter (inclusive)", example = "2026-07-01")
    private LocalDate fromDate;

    /** Optional end date filter applied to the report. */
    @Schema(description = "Report end date filter (inclusive)", example = "2026-07-31")
    private LocalDate toDate;

    /** Total fuel cost across the fleet. */
    @Schema(description = "Total fleet fuel expenditure (INR)", example = "125000.00")
    private BigDecimal totalFuelCost;

    /** Total maintenance cost across the fleet. */
    @Schema(description = "Total fleet maintenance expenditure (INR)", example = "85000.00")
    private BigDecimal totalMaintenanceCost;

    /** Total general expense cost across the fleet. */
    @Schema(description = "Total fleet general expenses (INR)", example = "64000.00")
    private BigDecimal totalExpenseCost;

    /** Grand total operational expenditure across the fleet. */
    @Schema(description = "Grand total operational expenditure (INR)", example = "274000.00")
    private BigDecimal grandTotalOperationalCost;

    /** List of per-vehicle operational cost breakdowns. */
    @Schema(description = "Per-vehicle operational cost breakdown")
    private List<VehicleOperationalCostItem> vehicleOperationalCosts;

    /**
     * Item DTO representing total operational cost breakdown for a single vehicle.
     */
    @Schema(description = "Operational cost breakdown for a single vehicle")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleOperationalCostItem {

        @Schema(description = "Vehicle ID", example = "1")
        private Long vehicleId;

        @Schema(description = "Vehicle registration number", example = "MH12AB1234")
        private String vehicleRegistrationNumber;

        @Schema(description = "Fuel expenditure (INR)", example = "45000.00")
        private BigDecimal fuelCost;

        @Schema(description = "Maintenance expenditure (INR)", example = "25000.00")
        private BigDecimal maintenanceCost;

        @Schema(description = "General expenses expenditure (INR)", example = "12000.00")
        private BigDecimal expenseCost;

        @Schema(description = "Total operational cost for this vehicle (INR)", example = "82000.00")
        private BigDecimal totalOperationalCost;
    }
}
