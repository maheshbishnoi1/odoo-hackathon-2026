package com.odoo.backend.dto.report;

import com.odoo.backend.enums.VehicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Report DTO providing fleet utilization statistics and status breakdown.
 * Returned by {@code GET /api/v1/reports/fleet-utilization}.
 */
@Schema(description = "Fleet utilization statistics and vehicle status distribution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FleetUtilizationReportDTO {

    /** Total number of vehicles in the fleet. */
    @Schema(description = "Total number of vehicles", example = "50")
    private long totalVehicles;

    /** Number of active (non-retired) vehicles. */
    @Schema(description = "Number of active vehicles", example = "45")
    private long activeVehicles;

    /** Number of vehicles currently on trip. */
    @Schema(description = "Number of vehicles currently on trip", example = "25")
    private long vehiclesOnTrip;

    /** Percentage of active fleet currently dispatched on trips. */
    @Schema(description = "Trip utilization percentage of active fleet", example = "55.56")
    private BigDecimal tripUtilizationPercentage;

    /** Percentage of active fleet available for dispatch. */
    @Schema(description = "Availability percentage of active fleet", example = "33.33")
    private BigDecimal availabilityPercentage;

    /** Breakdown of vehicle counts grouped by {@link VehicleStatus}. */
    @Schema(description = "Count of vehicles grouped by status")
    private Map<VehicleStatus, Long> statusBreakdown;
}
