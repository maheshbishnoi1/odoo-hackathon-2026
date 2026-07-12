package com.odoo.backend.dto;

import com.odoo.backend.enums.ExpenseType;
import com.odoo.backend.enums.PaymentMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating an {@link com.odoo.backend.entity.Expense}.
 *
 * <p>Jakarta Bean Validation ensures data integrity at the API boundary.
 * The service layer additionally validates that expense date is not in the future.</p>
 */
@Schema(description = "Payload for creating or updating an expense record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRequestDTO {

    /**
     * ID of the vehicle this expense is associated with.
     */
    @Schema(description = "Vehicle ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Vehicle ID is required")
    @Positive(message = "Vehicle ID must be a positive number")
    private Long vehicleId;

    /**
     * Optional: ID of the trip during which the expense was incurred.
     */
    @Schema(description = "Optional: Trip ID associated with this expense", example = "5")
    @Positive(message = "Trip ID must be a positive number")
    private Long tripId;

    /**
     * Category of the expense.
     */
    @Schema(description = "Expense type / category", example = "TOLL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Expense type is required")
    private ExpenseType expenseType;

    /**
     * Short descriptive title of the expense.
     */
    @Schema(description = "Short title of the expense", example = "Mumbai-Pune Expressway Toll", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Expense title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    /**
     * Detailed description of the expense.
     */
    @Schema(description = "Detailed description of the expense")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    /**
     * Amount of the expense. Must be greater than zero.
     */
    @Schema(description = "Expense amount (INR)", example = "250.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    /**
     * Date on which the expense was incurred. Cannot be a future date.
     */
    @Schema(description = "Date of expense (YYYY-MM-DD)", example = "2026-07-10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Expense date is required")
    @PastOrPresent(message = "Expense date cannot be in the future")
    private LocalDate expenseDate;

    /**
     * Payment method used to settle the expense.
     */
    @Schema(description = "Payment mode", example = "CASH", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    /**
     * Additional notes about the expense.
     */
    @Schema(description = "Additional remarks", example = "Paid at Khopoli toll plaza")
    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;
}
