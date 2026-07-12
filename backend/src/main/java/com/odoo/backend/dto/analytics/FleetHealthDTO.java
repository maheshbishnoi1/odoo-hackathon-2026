package com.odoo.backend.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Business Rules — Fleet Health & Smart Alerts.
 *
 * <p>Returned by {@code GET /api/v1/analytics/fleet-health}</p>
 * <p>Highlights vehicles requiring attention: license expiry, overdue maintenance,
 * high-cost vehicles, and idle assets.</p>
 */
@Schema(description = "Fleet health status with smart business rule alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FleetHealthDTO {

    @Schema(description = "Overall fleet health score (0-100)")
    private int healthScore;

    @Schema(description = "Fleet health grade: EXCELLENT, GOOD, FAIR, POOR")
    private String healthGrade;

    @Schema(description = "Drivers with licenses expiring within 30 days")
    private List<LicenseExpiryAlert> licenseExpiryAlerts;

    @Schema(description = "Vehicles with active open maintenance (vehicles in shop)")
    private List<MaintenanceAlert> activeMaintenanceAlerts;

    @Schema(description = "Vehicles with no trips in the last 30 days (idle assets)")
    private List<IdleVehicleAlert> idleVehicleAlerts;

    @Schema(description = "Top 5 highest cost vehicles this period")
    private List<HighCostVehicleAlert> highCostVehicleAlerts;

    @Schema(description = "Summary counts of all alert types")
    private AlertSummary alertSummary;

    // ---- Nested Alert DTOs ----

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Driver license expiry alert")
    public static class LicenseExpiryAlert {
        private Long driverId;
        private String driverName;
        private String licenseNumber;
        private String licenseExpiryDate;
        private long daysUntilExpiry;
        private String severity; // CRITICAL (<7 days), WARNING (<30 days)
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Vehicle active maintenance alert")
    public static class MaintenanceAlert {
        private Long vehicleId;
        private String vehicleRegistration;
        private String maintenanceType;
        private String status;
        private String startDate;
        private BigDecimal estimatedCost;
        private long daysInShop;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Idle vehicle alert — no trips in 30 days")
    public static class IdleVehicleAlert {
        private Long vehicleId;
        private String vehicleRegistration;
        private String vehicleName;
        private String status;
        private String lastTripDate;
        private long idleDays;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "High cost vehicle alert")
    public static class HighCostVehicleAlert {
        private Long vehicleId;
        private String vehicleRegistration;
        private BigDecimal totalCost;
        private BigDecimal fuelCost;
        private BigDecimal maintenanceCost;
        private BigDecimal expenseCost;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Summary of all alert counts")
    public static class AlertSummary {
        private int licenseExpiryCount;
        private int activeMaintenanceCount;
        private int idleVehicleCount;
        private int highCostVehicleCount;
        private int totalAlerts;
    }
}
