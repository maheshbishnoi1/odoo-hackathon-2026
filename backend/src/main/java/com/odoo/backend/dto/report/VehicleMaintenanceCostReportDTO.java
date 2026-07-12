package com.odoo.backend.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Report DTO providing vehicle-wise maintenance expenditure breakdown and fleet summary.
 * Returned by {@code GET /api/v1/reports/maintenance-costs}.
 */
@Schema(description = "Vehicle-wise maintenance cost report with fleet summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleMaintenanceCostReportDTO {

    /** Optional start date filter applied to the report. */
    @Schema(description = "Report start date filter (inclusive)", example = "2026-07-01")
    private LocalDate fromDate;

    /** Optional end date filter applied to the report. */
    @Schema(description = "Report end date filter (inclusive)", example = "2026-07-31")
    private LocalDate toDate;

    /** Total actual maintenance cost across all vehicles in the report period. */
    @Schema(description = "Total actual maintenance cost across the fleet (INR)", example = "85000.00")
    private BigDecimal totalFleetMaintenanceCost;

    /** Total estimated maintenance cost across all vehicles in the report period. */
    @Schema(description = "Total estimated maintenance cost across the fleet (INR)", example = "80000.00")
    private BigDecimal totalFleetEstimatedCost;

    /** List of per-vehicle maintenance cost aggregations. */
    @Schema(description = "Per-vehicle maintenance cost breakdown")
    private List<VehicleMaintenanceCostItem> vehicleMaintenanceCosts;

    /**
     * Item DTO representing maintenance cost aggregation for a single vehicle.
     */
    @Schema(description = "Maintenance expenditure summary for a single vehicle")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleMaintenanceCostItem {

        @Schema(description = "Vehicle ID", example = "1")
        private Long vehicleId;

        @Schema(description = "Vehicle registration number", example = "MH12AB1234")
        private String vehicleRegistrationNumber;

        @Schema(description = "Total number of maintenance records", example = "4")
        private long maintenanceRecordCount;

        @Schema(description = "Total estimated cost (INR)", example = "18000.00")
        private BigDecimal totalEstimatedCost;

        @Schema(description = "Total actual cost incurred (INR)", example = "19500.00")
        private BigDecimal totalActualCost;
    }
}
