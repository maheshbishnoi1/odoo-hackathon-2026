package com.odoo.backend.controller;

import com.odoo.backend.dto.ApiResponse;
import com.odoo.backend.dto.report.*;
import com.odoo.backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for the Reports Module of TransitOps.
 *
 * <p>Base path: {@code /api/v1/reports}</p>
 * <p>All endpoints return a standardized {@link ApiResponse} envelope.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Analytics", description = "Analytical and financial reporting APIs for fleet operations")
public class ReportController {

    private final ReportService reportService;

    // -----------------------------------------------------------------------
    // GET /api/v1/reports/dashboard-kpis
    // -----------------------------------------------------------------------

    /**
     * Retrieves executive dashboard KPI metrics across the fleet.
     */
    @Operation(
            summary     = "Get Executive Dashboard KPIs",
            description = "Returns high-level KPI cards including fleet size, active vehicles, workshop count, and cost summaries."
    )
    @GetMapping("/dashboard-kpis")
    public ResponseEntity<ApiResponse<DashboardKpiDTO>> getDashboardKpi() {
        log.debug("GET /api/v1/reports/dashboard-kpis");
        DashboardKpiDTO response = reportService.getDashboardKpi();
        return ResponseEntity.ok(ApiResponse.success("Dashboard KPIs retrieved successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/reports/fleet-utilization
    // -----------------------------------------------------------------------

    /**
     * Retrieves fleet utilization and status distribution report.
     */
    @Operation(
            summary     = "Get Fleet Utilization Report",
            description = "Returns fleet status breakdown (AVAILABLE, ON_TRIP, IN_SHOP, etc.) and utilization percentages."
    )
    @GetMapping("/fleet-utilization")
    public ResponseEntity<ApiResponse<FleetUtilizationReportDTO>> getFleetUtilization() {
        log.debug("GET /api/v1/reports/fleet-utilization");
        FleetUtilizationReportDTO response = reportService.getFleetUtilization();
        return ResponseEntity.ok(ApiResponse.success("Fleet utilization report retrieved successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/reports/fuel-costs
    // -----------------------------------------------------------------------

    /**
     * Retrieves vehicle-wise fuel cost report with optional date filtering.
     */
    @Operation(
            summary     = "Get Vehicle-wise Fuel Cost Report",
            description = "Returns per-vehicle fuel quantity, total cost, and average cost/litre with fleet summary."
    )
    @GetMapping("/fuel-costs")
    public ResponseEntity<ApiResponse<VehicleFuelCostReportDTO>> getFuelCostReport(
            @Parameter(description = "Filter from date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "Filter to date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        log.debug("GET /api/v1/reports/fuel-costs fromDate={} toDate={}", fromDate, toDate);
        VehicleFuelCostReportDTO response = reportService.getVehicleFuelCostReport(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Vehicle fuel cost report retrieved successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/reports/maintenance-costs
    // -----------------------------------------------------------------------

    /**
     * Retrieves vehicle-wise maintenance cost report with optional date filtering.
     */
    @Operation(
            summary     = "Get Vehicle-wise Maintenance Cost Report",
            description = "Returns per-vehicle maintenance record counts, estimated vs actual cost, and fleet summary."
    )
    @GetMapping("/maintenance-costs")
    public ResponseEntity<ApiResponse<VehicleMaintenanceCostReportDTO>> getMaintenanceCostReport(
            @Parameter(description = "Filter from date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "Filter to date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        log.debug("GET /api/v1/reports/maintenance-costs fromDate={} toDate={}", fromDate, toDate);
        VehicleMaintenanceCostReportDTO response = reportService.getVehicleMaintenanceCostReport(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Vehicle maintenance cost report retrieved successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/reports/vehicle-expenses
    // -----------------------------------------------------------------------

    /**
     * Retrieves vehicle-wise general expense summary report with optional date filtering.
     */
    @Operation(
            summary     = "Get Vehicle Expense Summary Report",
            description = "Returns per-vehicle general expense counts and total amount."
    )
    @GetMapping("/vehicle-expenses")
    public ResponseEntity<ApiResponse<VehicleExpenseSummaryReportDTO>> getExpenseSummaryReport(
            @Parameter(description = "Filter from date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "Filter to date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        log.debug("GET /api/v1/reports/vehicle-expenses fromDate={} toDate={}", fromDate, toDate);
        VehicleExpenseSummaryReportDTO response = reportService.getVehicleExpenseSummaryReport(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Vehicle expense summary report retrieved successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/reports/operational-costs
    // -----------------------------------------------------------------------

    /**
     * Retrieves combined operational cost report (Fuel + Maintenance + Expenses).
     */
    @Operation(
            summary     = "Get Combined Operational Cost Report",
            description = "Aggregates fuel, maintenance, and general expenses per vehicle and across the entire fleet."
    )
    @GetMapping("/operational-costs")
    public ResponseEntity<ApiResponse<OperationalCostReportDTO>> getOperationalCostReport(
            @Parameter(description = "Filter from date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "Filter to date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        log.debug("GET /api/v1/reports/operational-costs fromDate={} toDate={}", fromDate, toDate);
        OperationalCostReportDTO response = reportService.getOperationalCostReport(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Operational cost report retrieved successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/reports/trip-statistics
    // -----------------------------------------------------------------------

    /**
     * Retrieves trip-level financial statistics combining fuel and expenses.
     */
    @Operation(
            summary     = "Get Trip Financial Statistics Report",
            description = "Returns per-trip fuel expenditure and expense breakdown with fleet average cost per trip."
    )
    @GetMapping("/trip-statistics")
    public ResponseEntity<ApiResponse<TripStatisticsReportDTO>> getTripStatisticsReport() {
        log.debug("GET /api/v1/reports/trip-statistics");
        TripStatisticsReportDTO response = reportService.getTripStatisticsReport();
        return ResponseEntity.ok(ApiResponse.success("Trip statistics report retrieved successfully.", response));
    }
}
