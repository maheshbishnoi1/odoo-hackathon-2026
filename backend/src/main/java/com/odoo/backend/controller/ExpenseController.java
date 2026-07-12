package com.odoo.backend.controller;

import com.odoo.backend.dto.ApiResponse;
import com.odoo.backend.dto.ExpenseRequestDTO;
import com.odoo.backend.dto.ExpenseResponseDTO;
import com.odoo.backend.dto.ExpenseSummaryDTO;
import com.odoo.backend.enums.ExpenseType;
import com.odoo.backend.enums.PaymentMode;
import com.odoo.backend.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for the Expense module of TransitOps.
 *
 * <p>Base path: {@code /api/v1/expenses}</p>
 *
 * <p>All endpoints return a standardised {@link ApiResponse} envelope.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Vehicle expense management APIs")
public class ExpenseController {

    private final ExpenseService expenseService;

    // -----------------------------------------------------------------------
    // POST /api/v1/expenses
    // -----------------------------------------------------------------------

    /**
     * Creates a new expense record for a vehicle.
     */
    @Operation(
            summary     = "Create an expense record",
            description = "Records a vehicle expense. Amount must be > 0. Expense date cannot be in the future. Trip is optional."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description  = "Expense created successfully",
                    content      = @Content(schema = @Schema(implementation = ExpenseResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle or Trip not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponseDTO>> createExpense(
            @Valid @RequestBody ExpenseRequestDTO dto) {

        log.debug("POST /api/v1/expenses — vehicleId={}, type={}", dto.getVehicleId(), dto.getExpenseType());
        ExpenseResponseDTO response = expenseService.createExpense(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense record created successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/expenses/{id}
    // -----------------------------------------------------------------------

    /**
     * Retrieves an expense record by its ID.
     */
    @Operation(summary = "Get expense by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expense found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponseDTO>> getExpenseById(
            @Parameter(description = "Expense record ID", example = "20")
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success("Expense retrieved.", expenseService.getExpenseById(id)));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/expenses/vehicle/{vehicleId}
    // -----------------------------------------------------------------------

    /**
     * Retrieves all expense records for a specific vehicle.
     */
    @Operation(summary = "Get all expenses for a vehicle")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expenses retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<ApiResponse<List<ExpenseResponseDTO>>> getExpensesByVehicle(
            @Parameter(description = "Vehicle ID", example = "1")
            @PathVariable Long vehicleId) {

        List<ExpenseResponseDTO> expenses = expenseService.getExpensesByVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Expenses for vehicle retrieved.", expenses));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/expenses/vehicle/{vehicleId}/summary
    // -----------------------------------------------------------------------

    /**
     * Returns an aggregate expense summary for a vehicle, grouped by expense type.
     */
    @Operation(
            summary     = "Get expense summary for a vehicle",
            description = "Returns total count, total amount, and a breakdown of amounts by expense type for a vehicle."
    )
    @GetMapping("/vehicle/{vehicleId}/summary")
    public ResponseEntity<ApiResponse<ExpenseSummaryDTO>> getExpenseSummary(
            @Parameter(description = "Vehicle ID", example = "1")
            @PathVariable Long vehicleId) {

        ExpenseSummaryDTO summary = expenseService.getExpenseSummaryByVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Expense summary retrieved.", summary));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/expenses  (paginated + filtered)
    // -----------------------------------------------------------------------

    /**
     * Returns a paginated, filterable list of expenses.
     */
    @Operation(
            summary     = "List all expenses (paginated)",
            description = "Supports filtering by vehicleId, tripId, expenseType, paymentMode, and date range."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ExpenseResponseDTO>>> getAllExpenses(
            @Parameter(description = "Filter by vehicle ID")
            @RequestParam(required = false) Long vehicleId,

            @Parameter(description = "Filter by trip ID")
            @RequestParam(required = false) Long tripId,

            @Parameter(description = "Filter by expense type")
            @RequestParam(required = false) ExpenseType expenseType,

            @Parameter(description = "Filter by payment mode")
            @RequestParam(required = false) PaymentMode paymentMode,

            @Parameter(description = "Filter: expense date from (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "Filter: expense date to (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @PageableDefault(size = 20, sort = "expenseDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<ExpenseResponseDTO> page = expenseService.getAllExpenses(
                vehicleId, tripId, expenseType, paymentMode, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Expenses retrieved.", page));
    }

    // -----------------------------------------------------------------------
    // PUT /api/v1/expenses/{id}
    // -----------------------------------------------------------------------

    /**
     * Updates an existing expense record.
     */
    @Operation(summary = "Update an expense record")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expense updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponseDTO>> updateExpense(
            @Parameter(description = "Expense record ID", example = "20")
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequestDTO dto) {

        log.debug("PUT /api/v1/expenses/{}", id);
        ExpenseResponseDTO response = expenseService.updateExpense(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully.", response));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/v1/expenses/{id}
    // -----------------------------------------------------------------------

    /**
     * Deletes an expense record.
     */
    @Operation(summary = "Delete an expense record")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expense deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @Parameter(description = "Expense record ID", example = "20")
            @PathVariable Long id) {

        expenseService.deleteExpense(id);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully."));
    }
}
