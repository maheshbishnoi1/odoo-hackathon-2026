package com.odoo.backend.dto;

import com.odoo.backend.enums.MaintenanceStatus;
import com.odoo.backend.enums.MaintenanceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating a {@link com.odoo.backend.entity.MaintenanceRecord}.
 *
 * <p>All validation is enforced via Jakarta Bean Validation annotations.
 * The service layer additionally enforces business rules (RETIRED vehicle check,
 * duplicate active maintenance check).</p>
 */
@Schema(description = "Payload for creating or updating a maintenance record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRequestDTO {

    /**
     * ID of the vehicle receiving maintenance.
     * Must reference an existing {@link com.odoo.backend.entity.Vehicle}.
     */
    @Schema(description = "ID of the vehicle to maintain", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Vehicle ID is required")
    @Positive(message = "Vehicle ID must be a positive number")
    private Long vehicleId;

    /**
     * Category of maintenance work.
     *
     * @see com.odoo.backend.enums.MaintenanceType
     */
    @Schema(description = "Type of maintenance", example = "ROUTINE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Maintenance type is required")
    private MaintenanceType maintenanceType;

    /**
     * Detailed description of work to be performed.
     */
    @Schema(description = "Detailed description of maintenance work", example = "Full engine oil change and filter replacement")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    /**
     * Estimated cost before work starts.
     */
    @Schema(description = "Estimated cost of maintenance", example = "2500.00")
    @DecimalMin(value = "0.0", inclusive = false, message = "Estimated cost must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Estimated cost must have at most 10 integer digits and 2 decimal places")
    private BigDecimal estimatedCost;

    /**
     * Actual cost after completion — may be null until status is COMPLETED.
     */
    @Schema(description = "Actual cost incurred after maintenance completion", example = "2750.00")
    @DecimalMin(value = "0.0", inclusive = false, message = "Actual cost must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Actual cost must have at most 10 integer digits and 2 decimal places")
    private BigDecimal actualCost;

    /**
     * Date when maintenance work starts.
     * Cannot be in the future for historical records; required.
     */
    @Schema(description = "Start date of maintenance (YYYY-MM-DD)", example = "2026-07-10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    /**
     * Completion date — required only when status is COMPLETED.
     * Must be on or after startDate (validated in service layer).
     */
    @Schema(description = "Completion date of maintenance (YYYY-MM-DD)", example = "2026-07-12")
    private LocalDate completionDate;

    /**
     * Lifecycle status of the maintenance record.
     * Defaults to OPEN if not supplied.
     */
    @Schema(description = "Status of the maintenance record", example = "OPEN")
    private MaintenanceStatus status;

    /**
     * Additional notes or remarks.
     */
    @Schema(description = "Remarks or notes about the maintenance", example = "Parts sourced from OEM supplier")
    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;
}
