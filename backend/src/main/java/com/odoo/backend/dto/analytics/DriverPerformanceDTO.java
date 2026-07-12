package com.odoo.backend.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Business Analytics — Driver Performance Report.
 *
 * <p>Returned by {@code GET /api/v1/analytics/driver-performance}</p>
 * <p>Ranks drivers by number of trips, distances covered, and associated costs.</p>
 */
@Schema(description = "Driver performance analytics with trip and cost breakdown")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverPerformanceDTO {

    @Schema(description = "Individual driver performance records")
    private List<DriverPerformanceItem> driverPerformances;

    @Schema(description = "Total trips across all drivers")
    private long totalTrips;

    @Schema(description = "Total distance covered by all drivers (km)")
    private double totalDistanceCovered;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Performance metrics for a single driver")
    public static class DriverPerformanceItem {

        @Schema(description = "Driver unique ID")
        private Long driverId;

        @Schema(description = "Driver full name")
        private String driverName;

        @Schema(description = "Driver license number")
        private String licenseNumber;

        @Schema(description = "Driver current status")
        private String status;

        @Schema(description = "Total trips completed (COMPLETED status)")
        private long completedTrips;

        @Schema(description = "Total trips assigned (all statuses)")
        private long totalTripsAssigned;

        @Schema(description = "Total distance covered across all trips (km)")
        private double totalDistanceCovered;

        @Schema(description = "Total fuel cost for this driver's trips")
        private BigDecimal totalFuelCost;

        @Schema(description = "Total expense cost for this driver's trips")
        private BigDecimal totalExpenseCost;

        @Schema(description = "Combined trip cost for this driver")
        private BigDecimal totalTripCost;

        @Schema(description = "Trip completion rate (%)")
        private BigDecimal completionRate;
    }
}
