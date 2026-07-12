package com.odoo.backend.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Report DTO providing vehicle-wise fuel expenditure breakdown and fleet summary.
 * Returned by {@code GET /api/v1/reports/fuel-costs}.
 */
@Schema(description = "Vehicle-wise fuel cost report with fleet summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleFuelCostReportDTO {

    /** Optional start date filter applied to the report. */
    @Schema(description = "Report start date filter (inclusive)", example = "2026-07-01")
    private LocalDate fromDate;

    /** Optional end date filter applied to the report. */
    @Schema(description = "Report end date filter (inclusive)", example = "2026-07-31")
    private LocalDate toDate;

    /** Total fuel cost across all vehicles in the report period. */
    @Schema(description = "Total fuel cost across the fleet (INR)", example = "125000.00")
    private BigDecimal totalFleetFuelCost;

    /** Total quantity of fuel consumed across all vehicles. */
    @Schema(description = "Total fuel quantity consumed across the fleet (litres)", example = "1300.00")
    private BigDecimal totalFleetFuelQuantity;

    /** List of per-vehicle fuel cost aggregations. */
    @Schema(description = "Per-vehicle fuel cost breakdown")
    private List<VehicleFuelCostItem> vehicleFuelCosts;

    /**
     * Item DTO representing fuel cost aggregation for a single vehicle.
     */
    @Schema(description = "Fuel expenditure summary for a single vehicle")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleFuelCostItem {

        @Schema(description = "Vehicle ID", example = "1")
        private Long vehicleId;

        @Schema(description = "Vehicle registration number", example = "MH12AB1234")
        private String vehicleRegistrationNumber;

        @Schema(description = "Total number of fuel fill-up records", example = "10")
        private long fillUpCount;

        @Schema(description = "Total fuel quantity consumed (litres)", example = "450.00")
        private BigDecimal totalQuantity;

        @Schema(description = "Total fuel cost incurred (INR)", example = "43000.00")
        private BigDecimal totalCost;

        @Schema(description = "Average cost per litre for this vehicle (INR/litre)", example = "95.56")
        private BigDecimal averageCostPerLiter;
    }
}
