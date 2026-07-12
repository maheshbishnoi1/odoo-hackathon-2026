package com.odoo.backend.controller;

import com.odoo.backend.dto.ApiResponse;
import com.odoo.backend.dto.analytics.*;
import com.odoo.backend.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Business Analytics & Intelligence Module of TransitOps.
 *
 * <p>Base path: {@code /api/v1/analytics}</p>
 *
 * <p>All endpoints implement business rules and fleet intelligence analytics:
 * <ul>
 *   <li>Monthly cost trend analysis across Fuel, Maintenance, and Expenses</li>
 *   <li>Driver performance ranking by trips, distance, and cost</li>
 *   <li>Maintenance cost variance (estimated vs actual)</li>
 *   <li>Fleet health score with smart business rule alerts</li>
 * </ul></p>
 *
 * <p>All endpoints return a standardized {@link ApiResponse} envelope.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(
        name = "Business Analytics",
        description = "Business intelligence analytics: cost trends, driver performance, maintenance variance, and fleet health"
)
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // -----------------------------------------------------------------------
    // GET /api/v1/analytics/cost-trends
    // -----------------------------------------------------------------------

    /**
     * Returns a month-by-month breakdown of operational costs.
     *
     * <p>Business Rule: Merges Fuel, Maintenance, and Expense monthly totals
     * into a unified timeline for frontend chart rendering (line/bar charts).</p>
     *
     * @return {@link ApiResponse} wrapping {@link CostTrendDTO}
     */
    @Operation(
            summary     = "Get Monthly Operational Cost Trends",
            description = "Returns month-by-month fuel, maintenance and expense costs merged into a single timeline for charting."
    )
    @GetMapping("/cost-trends")
    public ResponseEntity<ApiResponse<CostTrendDTO>> getMonthlyCostTrend() {
        log.debug("GET /api/v1/analytics/cost-trends");
        CostTrendDTO result = analyticsService.getMonthlyCostTrend();
        return ResponseEntity.ok(
                ApiResponse.success("Monthly cost trend data retrieved successfully.", result)
        );
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/analytics/driver-performance
    // -----------------------------------------------------------------------

    /**
     * Returns performance analytics ranked for all drivers.
     *
     * <p>Business Rules applied:
     * <ul>
     *   <li>Completion Rate = completedTrips / totalTripsAssigned × 100</li>
     *   <li>Drivers are ranked by total trip cost (descending)</li>
     *   <li>All registered drivers appear even if they have zero trips</li>
     * </ul></p>
     *
     * @return {@link ApiResponse} wrapping {@link DriverPerformanceDTO}
     */
    @Operation(
            summary     = "Get Driver Performance Analytics",
            description = "Ranks drivers by trips completed, distance covered, and total cost (fuel + expenses). Includes completion rate."
    )
    @GetMapping("/driver-performance")
    public ResponseEntity<ApiResponse<DriverPerformanceDTO>> getDriverPerformance() {
        log.debug("GET /api/v1/analytics/driver-performance");
        DriverPerformanceDTO result = analyticsService.getDriverPerformance();
        return ResponseEntity.ok(
                ApiResponse.success("Driver performance analytics retrieved successfully.", result)
        );
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/analytics/maintenance-variance
    // -----------------------------------------------------------------------

    /**
     * Returns maintenance cost variance analysis per vehicle.
     *
     * <p>Business Rules applied:
     * <ul>
     *   <li>Variance = actualCost − estimatedCost</li>
     *   <li>Positive variance → over budget (flagged as alert)</li>
     *   <li>Results sorted by absolute variance descending for priority review</li>
     * </ul></p>
     *
     * @return {@link ApiResponse} wrapping {@link MaintenanceVarianceDTO}
     */
    @Operation(
            summary     = "Get Maintenance Cost Variance Report",
            description = "Compares estimated vs actual maintenance costs per vehicle. Positive variance = over budget."
    )
    @GetMapping("/maintenance-variance")
    public ResponseEntity<ApiResponse<MaintenanceVarianceDTO>> getMaintenanceVariance() {
        log.debug("GET /api/v1/analytics/maintenance-variance");
        MaintenanceVarianceDTO result = analyticsService.getMaintenanceVariance();
        return ResponseEntity.ok(
                ApiResponse.success("Maintenance cost variance report retrieved successfully.", result)
        );
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/analytics/fleet-health
    // -----------------------------------------------------------------------

    /**
     * Returns fleet health score and smart business rule alerts.
     *
     * <p>Business Rules Engine evaluates:
     * <ol>
     *   <li>Driver license expiry within 30 days → WARNING; within 7 days → CRITICAL</li>
     *   <li>Vehicles with active open/in-progress maintenance</li>
     *   <li>Vehicles with no trips in the last 30 days (idle assets)</li>
     *   <li>Top 5 highest-cost vehicles for financial review</li>
     * </ol>
     * Health Score = max(0, 100 − 5 × totalAlerts). Grade: EXCELLENT/GOOD/FAIR/POOR.</p>
     *
     * @return {@link ApiResponse} wrapping {@link FleetHealthDTO}
     */
    @Operation(
            summary     = "Get Fleet Health Score & Smart Alerts",
            description = "Evaluates fleet health using business rules: license expiry, open maintenance, idle vehicles, and high-cost vehicles. Returns a 0-100 health score."
    )
    @GetMapping("/fleet-health")
    public ResponseEntity<ApiResponse<FleetHealthDTO>> getFleetHealth() {
        log.debug("GET /api/v1/analytics/fleet-health");
        FleetHealthDTO result = analyticsService.getFleetHealth();
        return ResponseEntity.ok(
                ApiResponse.success("Fleet health report retrieved successfully.", result)
        );
    }
}
