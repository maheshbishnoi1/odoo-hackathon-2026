package com.odoo.backend.dto;

import com.odoo.backend.enums.FuelType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for {@link com.odoo.backend.entity.FuelLog}.
 *
 * <p>Flat projection — no entity references exposed.
 * Vehicle and Trip information is included as flat ID + display fields.</p>
 */
@Schema(description = "Fuel log entry details returned by the API")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelResponseDTO {

    /** Unique identifier of the fuel log entry. */
    @Schema(description = "Fuel log ID", example = "10")
    private Long id;

    // ---- Vehicle summary ----

    /** ID of the associated vehicle. */
    @Schema(description = "Vehicle ID", example = "1")
    private Long vehicleId;

    /** Registration number of the associated vehicle. */
    @Schema(description = "Vehicle registration number", example = "MH12AB1234")
    private String vehicleRegistrationNumber;

    // ---- Trip summary (nullable) ----

    /** ID of the associated trip, or {@code null} if not linked to a trip. */
    @Schema(description = "Trip ID (nullable)", example = "5")
    private Long tripId;

    /** Trip reference number, or {@code null} if not linked. */
    @Schema(description = "Trip number (nullable)", example = "TRP-2026-005")
    private String tripNumber;

    // ---- Fuel fields ----

    /** Date of fuel purchase. */
    @Schema(description = "Fuel purchase date", example = "2026-07-10")
    private LocalDate fuelDate;

    /** Type of fuel. */
    @Schema(description = "Fuel type", example = "DIESEL")
    private FuelType fuelType;

    /** Quantity purchased (litres or kWh). */
    @Schema(description = "Fuel quantity (litres)", example = "45.50")
    private BigDecimal quantity;

    /** Total cost of the fuel. */
    @Schema(description = "Total fuel cost (INR)", example = "3640.00")
    private BigDecimal cost;

    /** Fuel vendor / station name. */
    @Schema(description = "Fuel vendor name", example = "HP Petrol Station, MG Road")
    private String vendor;

    /** Odometer reading at fuelling time. */
    @Schema(description = "Odometer reading at fuelling (km)", example = "48520")
    private Long odometerReading;

    /**
     * Automatically calculated cost per litre ({@code cost / quantity}).
     */
    @Schema(description = "Cost per litre (INR/L) — auto-calculated", example = "80.0000")
    private BigDecimal costPerLiter;

    /** Additional remarks. */
    @Schema(description = "Remarks", example = "Filled before highway trip")
    private String remarks;

    /** Record creation timestamp. */
    @Schema(description = "Record created at", example = "2026-07-10T08:15:00")
    private LocalDateTime createdAt;

    /** Last update timestamp. */
    @Schema(description = "Record last updated at", example = "2026-07-10T08:15:00")
    private LocalDateTime updatedAt;
}
