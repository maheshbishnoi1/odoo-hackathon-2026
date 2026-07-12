package com.odoo.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * Summary DTO for fuel consumption and cost totals for a vehicle.
 * Returned by the {@code GET /api/v1/fuel/vehicle/{vehicleId}/summary} endpoint.
 */
@Schema(description = "Fuel consumption summary for a vehicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelSummaryDTO {

    /** ID of the vehicle. */
    @Schema(description = "Vehicle ID", example = "1")
    private Long vehicleId;

    /** Registration number of the vehicle. */
    @Schema(description = "Vehicle registration number", example = "MH12AB1234")
    private String vehicleRegistrationNumber;

    /** Total number of fuel log entries for this vehicle. */
    @Schema(description = "Total number of fuel log entries", example = "15")
    private long totalFuelLogs;

    /** Aggregate fuel quantity purchased across all logs (litres). */
    @Schema(description = "Total fuel quantity purchased (litres)", example = "682.50")
    private BigDecimal totalQuantity;

    /** Aggregate fuel cost across all logs. */
    @Schema(description = "Total fuel cost (INR)", example = "54600.00")
    private BigDecimal totalCost;

    /** Average cost per litre across all logs. */
    @Schema(description = "Average cost per litre (INR/L)", example = "79.9927")
    private BigDecimal averageCostPerLiter;

    /** Latest odometer reading recorded for this vehicle. */
    @Schema(description = "Latest odometer reading (km)", example = "52340")
    private Long latestOdometerReading;
}
