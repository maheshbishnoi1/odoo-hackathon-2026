package com.odoo.backend.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Business Analytics — Cost Trend Analysis.
 *
 * <p>Returned by {@code GET /api/v1/analytics/cost-trends}</p>
 * <p>Provides monthly aggregated costs broken down by Fuel, Maintenance, and Expenses.</p>
 */
@Schema(description = "Monthly cost trend analysis across fuel, maintenance and expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostTrendDTO {

    @Schema(description = "List of monthly cost data points")
    private List<MonthlyCostItem> monthlyCosts;

    @Schema(description = "Total fuel cost across all months")
    private BigDecimal totalFuelCost;

    @Schema(description = "Total maintenance cost across all months")
    private BigDecimal totalMaintenanceCost;

    @Schema(description = "Total expense cost across all months")
    private BigDecimal totalExpenseCost;

    @Schema(description = "Grand total operational cost")
    private BigDecimal grandTotal;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Cost breakdown for a single month")
    public static class MonthlyCostItem {

        @Schema(description = "Year", example = "2025")
        private int year;

        @Schema(description = "Month (1-12)", example = "7")
        private int month;

        @Schema(description = "Month label", example = "Jul 2025")
        private String monthLabel;

        @Schema(description = "Fuel cost for this month")
        private BigDecimal fuelCost;

        @Schema(description = "Maintenance cost for this month")
        private BigDecimal maintenanceCost;

        @Schema(description = "General expense cost for this month")
        private BigDecimal expenseCost;

        @Schema(description = "Total operational cost for this month")
        private BigDecimal totalCost;
    }
}
