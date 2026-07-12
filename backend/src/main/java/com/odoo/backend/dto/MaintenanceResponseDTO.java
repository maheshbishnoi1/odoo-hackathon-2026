package com.odoo.backend.dto;

import com.odoo.backend.enums.MaintenanceStatus;
import com.odoo.backend.enums.MaintenanceType;
import com.odoo.backend.enums.VehicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for {@link com.odoo.backend.entity.MaintenanceRecord}.
 *
 * <p>This DTO is a flat projection — no entity references are exposed.
 * Vehicle information is embedded as flat fields to prevent circular references
 * and avoid lazy-loading issues in JSON serialization.</p>
 */
@Schema(description = "Maintenance record details returned by the API")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceResponseDTO {

    /** Unique identifier of the maintenance record. */
    @Schema(description = "Maintenance record ID", example = "42")
    private Long id;

    // ---- Vehicle summary (flat — no entity reference) ----

    /** ID of the associated vehicle. */
    @Schema(description = "Vehicle ID", example = "1")
    private Long vehicleId;

    /** Registration number of the associated vehicle. */
    @Schema(description = "Vehicle registration number", example = "MH12AB1234")
    private String vehicleRegistrationNumber;

    /** Current operational status of the associated vehicle. */
    @Schema(description = "Current vehicle status", example = "IN_SHOP")
    private VehicleStatus vehicleStatus;

    // ---- Maintenance fields ----

    /** Category of maintenance. */
    @Schema(description = "Type of maintenance performed", example = "ROUTINE")
    private MaintenanceType maintenanceType;

    /** Description of maintenance work. */
    @Schema(description = "Description of the maintenance work")
    private String description;

    /** Estimated cost provided at creation. */
    @Schema(description = "Estimated cost (INR)", example = "2500.00")
    private BigDecimal estimatedCost;

    /** Actual final cost after completion. */
    @Schema(description = "Actual cost incurred (INR)", example = "2750.00")
    private BigDecimal actualCost;

    /** Date when maintenance started. */
    @Schema(description = "Maintenance start date", example = "2026-07-10")
    private LocalDate startDate;

    /** Date when maintenance was completed. */
    @Schema(description = "Maintenance completion date", example = "2026-07-12")
    private LocalDate completionDate;

    /** Current lifecycle status. */
    @Schema(description = "Maintenance status", example = "COMPLETED")
    private MaintenanceStatus status;

    /** Remarks / notes. */
    @Schema(description = "Additional remarks")
    private String remarks;

    /** Record creation timestamp. */
    @Schema(description = "Record created at", example = "2026-07-10T09:30:00")
    private LocalDateTime createdAt;

    /** Last update timestamp. */
    @Schema(description = "Record last updated at", example = "2026-07-12T14:00:00")
    private LocalDateTime updatedAt;
}
