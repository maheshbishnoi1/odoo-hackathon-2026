package com.odoo.backend.dto;

import com.odoo.backend.enums.FuelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating a {@link com.odoo.backend.entity.FuelLog}.
 *
 * <p>All validation is enforced via Jakarta Bean Validation annotations.
 * The service layer additionally enforces the odometer-must-not-decrease rule.</p>
 */
@Schema(description = "Payload for creating or updating a fuel log entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelRequestDTO {

    /**
     * ID of the vehicle that was fuelled.
     */
    @Schema(description = "ID of the vehicle", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Vehicle ID is required")
    @Positive(message = "Vehicle ID must be a positive number")
    private Long vehicleId;

    /**
     * Optional: ID of the trip during which fuel was purchased.
     */
    @Schema(description = "Optional: ID of the associated trip", example = "5")
    @Positive(message = "Trip ID must be a positive number")
    private Long tripId;

    /**
     * Date of the fuel purchase.
     */
    @Schema(description = "Date of fuel purchase (YYYY-MM-DD)", example = "2026-07-10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Fuel date is required")
    @PastOrPresent(message = "Fuel date cannot be in the future")
    private LocalDate fuelDate;

    /**
     * Type of fuel purchased.
     */
    @Schema(description = "Type of fuel", example = "DIESEL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;

    /**
     * Volume of fuel purchased (litres or kWh for electric).
     * Must be greater than zero.
     */
    @Schema(description = "Quantity of fuel purchased (litres)", example = "45.50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Fuel quantity is required")
    @DecimalMin(value = "0.01", message = "Fuel quantity must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Invalid quantity format")
    private BigDecimal quantity;

    /**
     * Total cost of the fuel purchased.
     * Must be positive.
     */
    @Schema(description = "Total fuel cost (INR)", example = "3640.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Fuel cost is required")
    @DecimalMin(value = "0.01", message = "Fuel cost must be positive")
    @Digits(integer = 10, fraction = 2, message = "Invalid cost format")
    private BigDecimal cost;

    /**
     * Name of the fuel vendor / petrol station.
     */
    @Schema(description = "Fuel vendor / petrol station name", example = "HP Petrol Station, MG Road")
    @Size(max = 200, message = "Vendor name must not exceed 200 characters")
    private String vendor;

    /**
     * Odometer reading at the time of fuelling (in km).
     * Must not be less than the vehicle's previous odometer reading.
     */
    @Schema(description = "Odometer reading at time of fuelling (km)", example = "48520", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Odometer reading is required")
    @Min(value = 0, message = "Odometer reading cannot be negative")
    private Long odometerReading;

    /**
     * Additional remarks about the fuel purchase.
     */
    @Schema(description = "Additional remarks", example = "Filled before long highway trip")
    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;
}
