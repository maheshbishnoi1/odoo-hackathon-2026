package com.odoo.backend.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Business Analytics — Maintenance Cost Variance Report.
 *
 * <p>Returned by {@code GET /api/v1/analytics/maintenance-variance}</p>
 * <p>Compares estimated vs. actual maintenance costs to highlight over/under-budget records.</p>
 */
@Schema(description = "Maintenance cost variance analytics (estimated vs actual)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceVarianceDTO {

    @Schema(description = "List of maintenance variance items per vehicle")
    private List<VehicleVarianceItem> vehicleVariances;

    @Schema(description = "Fleet total estimated maintenance cost")
    private BigDecimal fleetTotalEstimated;

    @Schema(description = "Fleet total actual maintenance cost")
    private BigDecimal fleetTotalActual;

    @Schema(description = "Fleet total variance (actual - estimated)")
    private BigDecimal fleetTotalVariance;

    @Schema(description = "Fleet variance percentage")
    private BigDecimal fleetVariancePercent;

    @Schema(description = "Number of over-budget maintenance records")
    private long overBudgetCount;

    @Schema(description = "Number of under-budget maintenance records")
    private long underBudgetCount;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Maintenance cost variance per vehicle")
    public static class VehicleVarianceItem {

        @Schema(description = "Vehicle ID")
        private Long vehicleId;

        @Schema(description = "Vehicle registration number")
        private String vehicleRegistration;

        @Schema(description = "Total maintenance records for this vehicle")
        private long maintenanceCount;

        @Schema(description = "Sum of estimated costs")
        private BigDecimal totalEstimated;

        @Schema(description = "Sum of actual costs")
        private BigDecimal totalActual;

        @Schema(description = "Variance amount (actual - estimated; positive = over budget)")
        private BigDecimal variance;

        @Schema(description = "Variance percentage relative to estimated")
        private BigDecimal variancePercent;

        @Schema(description = "Whether this vehicle's maintenance went over budget")
        private boolean overBudget;
    }
}
