package com.odoo.backend.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Report DTO providing trip-level financial statistics linking Fuel logs and Expenses.
 * Returned by {@code GET /api/v1/reports/trip-statistics}.
 */
@Schema(description = "Trip financial statistics report combining fuel and expense costs per trip")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripStatisticsReportDTO {

    /** Total number of trips recorded. */
    @Schema(description = "Total number of trips analyzed", example = "30")
    private long totalTrips;

    /** Total fuel cost linked across all trips. */
    @Schema(description = "Total fuel expenditure linked to trips (INR)", example = "95000.00")
    private BigDecimal totalTripFuelCost;

    /** Total general expense cost linked across all trips. */
    @Schema(description = "Total expense expenditure linked to trips (INR)", example = "35000.00")
    private BigDecimal totalTripExpenseCost;

    /** Total combined cost across all trips. */
    @Schema(description = "Total combined trip expenditure (INR)", example = "130000.00")
    private BigDecimal totalTripCost;

    /** Average expenditure per trip. */
    @Schema(description = "Average cost per trip (INR)", example = "4333.33")
    private BigDecimal averageCostPerTrip;

    /** List of per-trip financial statistics. */
    @Schema(description = "Per-trip financial breakdown")
    private List<TripFinancialItem> tripStatistics;

    /**
     * Item DTO representing financial statistics for a single trip.
     */
    @Schema(description = "Financial summary for a single trip")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TripFinancialItem {

        @Schema(description = "Trip ID", example = "10")
        private Long tripId;

        @Schema(description = "Trip reference number", example = "TRP-2026-010")
        private String tripNumber;

        @Schema(description = "Fuel expenditure during this trip (INR)", example = "3200.00")
        private BigDecimal fuelCost;

        @Schema(description = "General expenses incurred during this trip (INR)", example = "1100.00")
        private BigDecimal expenseCost;

        @Schema(description = "Total expenditure for this trip (INR)", example = "4300.00")
        private BigDecimal totalTripCost;
    }
}
