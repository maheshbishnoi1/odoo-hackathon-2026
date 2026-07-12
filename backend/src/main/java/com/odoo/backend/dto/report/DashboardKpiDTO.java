package com.odoo.backend.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * Executive KPI summary DTO for high-level fleet operational insights.
 * Returned by {@code GET /api/v1/reports/dashboard-kpis}.
 */
@Schema(description = "Executive Dashboard KPI metrics for fleet operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardKpiDTO {

    /** Total number of vehicles registered in the fleet. */
    @Schema(description = "Total registered fleet size", example = "50")
    private long totalFleetSize;

    /** Number of active (non-retired) vehicles in the fleet. */
    @Schema(description = "Number of active vehicles", example = "45")
    private long activeVehicles;

    /** Number of vehicles currently undergoing maintenance (IN_SHOP or UNDER_REPAIR). */
    @Schema(description = "Vehicles currently in workshop or under repair", example = "3")
    private long vehiclesInShop;

    /** Number of vehicles currently on active trips. */
    @Schema(description = "Vehicles currently on trip", example = "20")
    private long vehiclesOnTrip;

    /** Number of open or in-progress maintenance records. */
    @Schema(description = "Count of active maintenance requests (OPEN or IN_PROGRESS)", example = "4")
    private long openMaintenanceCount;

    /** Total fuel expenditure across the fleet. */
    @Schema(description = "Total fuel cost (INR)", example = "450000.00")
    private BigDecimal totalFuelCost;

    /** Total actual maintenance expenditure across the fleet. */
    @Schema(description = "Total actual maintenance cost (INR)", example = "125000.00")
    private BigDecimal totalMaintenanceCost;

    /** Total general expenses expenditure across the fleet. */
    @Schema(description = "Total general expenses (INR)", example = "85000.00")
    private BigDecimal totalExpenseCost;

    /** Total combined operational cost (Fuel + Maintenance + Expenses). */
    @Schema(description = "Total combined operational expenditure (INR)", example = "660000.00")
    private BigDecimal totalOperationalCost;
}
