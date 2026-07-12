package com.odoo.backend.controller;

import com.odoo.backend.dto.ApiResponse;
import com.odoo.backend.dto.dashboard.DashboardSummaryDTO;
import com.odoo.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the Dashboard Module of TransitOps.
 *
 * <p>Base path: {@code /api/v1/dashboard}</p>
 *
 * <p>Provides a single aggregated executive summary endpoint that combines
 * fleet status, trip metrics, financial KPIs, and recent activity in one call.</p>
 *
 * <p>All endpoints are secured and return a standardized {@link ApiResponse} envelope.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(
        name = "Dashboard",
        description = "Executive dashboard summary combining fleet, driver, trip and financial KPIs"
)
public class DashboardController {

    private final DashboardService dashboardService;

    // -----------------------------------------------------------------------
    // GET /api/v1/dashboard/summary
    // -----------------------------------------------------------------------

    /**
     * Returns a complete executive dashboard snapshot.
     *
     * <p>This is the primary frontend-facing endpoint used to populate all KPI cards,
     * status charts, recent activity feed, and maintenance alerts on the dashboard page.</p>
     *
     * <p>Data included:
     * <ul>
     *   <li>Fleet status breakdown (AVAILABLE, ON_TRIP, IN_SHOP, RETIRED)</li>
     *   <li>Fleet utilization percentage</li>
     *   <li>Driver availability stats</li>
     *   <li>Trip status breakdown (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)</li>
     *   <li>Open and in-progress maintenance counts</li>
     *   <li>All-time financial KPIs (Fuel, Maintenance, Expenses, Total)</li>
     *   <li>5 most recent trips</li>
     *   <li>Active maintenance alerts</li>
     * </ul></p>
     *
     * @return {@link ApiResponse} wrapping {@link DashboardSummaryDTO}
     */
    @Operation(
            summary     = "Get Executive Dashboard Summary",
            description = "Returns all KPI cards, fleet status, trip metrics, financial totals, recent trips and maintenance alerts."
    )
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary() {
        log.debug("GET /api/v1/dashboard/summary");
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard summary retrieved successfully.", summary)
        );
    }
}
