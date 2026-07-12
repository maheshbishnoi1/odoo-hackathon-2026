package com.odoo.backend.dto.dashboard;

import com.odoo.backend.enums.VehicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive Dashboard Summary DTO for the TransitOps executive dashboard.
 *
 * <p>Returned by {@code GET /api/v1/dashboard/summary}</p>
 * <p>Aggregates all high-level operational KPIs into a single API call.</p>
 */
@Schema(description = "Executive dashboard summary with all key performance indicators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {

    // ---- Fleet Status ----

    @Schema(description = "Total number of registered vehicles")
    private long totalVehicles;

    @Schema(description = "Number of vehicles currently available for dispatch")
    private long availableVehicles;

    @Schema(description = "Number of vehicles currently on a trip")
    private long vehiclesOnTrip;

    @Schema(description = "Number of vehicles in shop or under repair")
    private long vehiclesInMaintenance;

    @Schema(description = "Number of retired vehicles")
    private long retiredVehicles;

    @Schema(description = "Fleet utilization percentage (on-trip vehicles / active vehicles * 100)")
    private BigDecimal fleetUtilizationPercent;

    @Schema(description = "Vehicle count grouped by status")
    private Map<VehicleStatus, Long> vehicleStatusBreakdown;

    // ---- Driver Stats ----

    @Schema(description = "Total registered drivers")
    private long totalDrivers;

    @Schema(description = "Drivers currently available")
    private long availableDrivers;

    @Schema(description = "Drivers currently on trip")
    private long driversOnTrip;

    // ---- Trip Stats ----

    @Schema(description = "Total trips scheduled")
    private long totalTrips;

    @Schema(description = "Trips currently in progress")
    private long activeTrips;

    @Schema(description = "Trips completed successfully")
    private long completedTrips;

    @Schema(description = "Trips cancelled")
    private long cancelledTrips;

    @Schema(description = "Trips scheduled but not started yet")
    private long scheduledTrips;

    // ---- Maintenance Stats ----

    @Schema(description = "Open maintenance requests")
    private long openMaintenanceRequests;

    @Schema(description = "Maintenance records in progress")
    private long inProgressMaintenance;

    // ---- Financial KPIs (All Time) ----

    @Schema(description = "Total fuel cost incurred (INR)")
    private BigDecimal totalFuelCost;

    @Schema(description = "Total maintenance cost incurred (INR)")
    private BigDecimal totalMaintenanceCost;

    @Schema(description = "Total general expenses (INR)")
    private BigDecimal totalExpenses;

    @Schema(description = "Grand total operational cost (INR)")
    private BigDecimal totalOperationalCost;

    // ---- Recent Activity ----

    @Schema(description = "Most recent 5 trips for quick view")
    private List<RecentTripDTO> recentTrips;

    @Schema(description = "Active maintenance alerts")
    private List<MaintenanceAlertDTO> maintenanceAlerts;

    // ---- Nested DTOs ----

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Recent trip summary for dashboard feed")
    public static class RecentTripDTO {
        private Long tripId;
        private String tripNumber;
        private String vehicleRegistration;
        private String driverName;
        private String source;
        private String destination;
        private String status;
        private String startTime;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Maintenance alert for dashboard")
    public static class MaintenanceAlertDTO {
        private Long maintenanceId;
        private String vehicleRegistration;
        private String serviceType;
        private String status;
        private String scheduledDate;
        private BigDecimal estimatedCost;
    }
}
