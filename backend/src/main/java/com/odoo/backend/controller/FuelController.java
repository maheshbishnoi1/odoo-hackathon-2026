package com.odoo.backend.controller;

import com.odoo.backend.dto.ApiResponse;
import com.odoo.backend.dto.FuelRequestDTO;
import com.odoo.backend.dto.FuelResponseDTO;
import com.odoo.backend.dto.FuelSummaryDTO;
import com.odoo.backend.enums.FuelType;
import com.odoo.backend.service.FuelService;
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
 * REST controller for the Fuel module of TransitOps.
 *
 * <p>Base path: {@code /api/v1/fuel}</p>
 *
 * <p>All endpoints return a standardised {@link ApiResponse} envelope.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/fuel")
@RequiredArgsConstructor
@Tag(name = "Fuel", description = "Vehicle fuel log management APIs")
public class FuelController {

    private final FuelService fuelService;

    // -----------------------------------------------------------------------
    // POST /api/v1/fuel
    // -----------------------------------------------------------------------

    /**
     * Logs a new fuel fill-up for a vehicle.
     */
    @Operation(
            summary     = "Log a fuel fill-up",
            description = "Records a fuel purchase for a vehicle. Odometer reading must not be lower than the previous entry. costPerLiter is computed automatically."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description  = "Fuel log created successfully",
                    content      = @Content(schema = @Schema(implementation = FuelResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle or Trip not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Odometer reading decreased")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<FuelResponseDTO>> logFuel(
            @Valid @RequestBody FuelRequestDTO dto) {

        log.debug("POST /api/v1/fuel — vehicleId={}", dto.getVehicleId());
        FuelResponseDTO response = fuelService.logFuel(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fuel log created successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/fuel/{id}
    // -----------------------------------------------------------------------

    /**
     * Retrieves a fuel log by its ID.
     */
    @Operation(summary = "Get fuel log by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fuel log found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Fuel log not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FuelResponseDTO>> getFuelLogById(
            @Parameter(description = "Fuel log ID", example = "10")
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success("Fuel log retrieved.", fuelService.getFuelLogById(id)));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/fuel/vehicle/{vehicleId}
    // -----------------------------------------------------------------------

    /**
     * Retrieves all fuel logs for a specific vehicle.
     */
    @Operation(summary = "Get all fuel logs for a vehicle")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fuel logs retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<ApiResponse<List<FuelResponseDTO>>> getFuelLogsByVehicle(
            @Parameter(description = "Vehicle ID", example = "1")
            @PathVariable Long vehicleId) {

        List<FuelResponseDTO> logs = fuelService.getFuelLogsByVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Fuel logs for vehicle retrieved.", logs));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/fuel/vehicle/{vehicleId}/summary
    // -----------------------------------------------------------------------

    /**
     * Returns an aggregate fuel summary for a vehicle.
     */
    @Operation(
            summary     = "Get fuel summary for a vehicle",
            description = "Returns total fuel quantity, total cost, average cost per litre, and latest odometer reading for a vehicle."
    )
    @GetMapping("/vehicle/{vehicleId}/summary")
    public ResponseEntity<ApiResponse<FuelSummaryDTO>> getFuelSummary(
            @Parameter(description = "Vehicle ID", example = "1")
            @PathVariable Long vehicleId) {

        FuelSummaryDTO summary = fuelService.getFuelSummaryByVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Fuel summary retrieved.", summary));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/fuel  (paginated + filtered)
    // -----------------------------------------------------------------------

    /**
     * Returns a paginated, filterable list of fuel logs.
     */
    @Operation(
            summary     = "List all fuel logs (paginated)",
            description = "Supports filtering by vehicleId, tripId, fuelType, and date range."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FuelResponseDTO>>> getAllFuelLogs(
            @Parameter(description = "Filter by vehicle ID")
            @RequestParam(required = false) Long vehicleId,

            @Parameter(description = "Filter by trip ID")
            @RequestParam(required = false) Long tripId,

            @Parameter(description = "Filter by fuel type")
            @RequestParam(required = false) FuelType fuelType,

            @Parameter(description = "Filter: fuel date from (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "Filter: fuel date to (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @PageableDefault(size = 20, sort = "fuelDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<FuelResponseDTO> page = fuelService.getAllFuelLogs(vehicleId, tripId, fuelType, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Fuel logs retrieved.", page));
    }

    // -----------------------------------------------------------------------
    // PUT /api/v1/fuel/{id}
    // -----------------------------------------------------------------------

    /**
     * Updates an existing fuel log.
     */
    @Operation(summary = "Update a fuel log")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fuel log updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Fuel log not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FuelResponseDTO>> updateFuelLog(
            @Parameter(description = "Fuel log ID", example = "10")
            @PathVariable Long id,
            @Valid @RequestBody FuelRequestDTO dto) {

        log.debug("PUT /api/v1/fuel/{}", id);
        FuelResponseDTO response = fuelService.updateFuelLog(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Fuel log updated successfully.", response));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/v1/fuel/{id}
    // -----------------------------------------------------------------------

    /**
     * Deletes a fuel log.
     */
    @Operation(summary = "Delete a fuel log")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fuel log deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Fuel log not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFuelLog(
            @Parameter(description = "Fuel log ID", example = "10")
            @PathVariable Long id) {

        fuelService.deleteFuelLog(id);
        return ResponseEntity.ok(ApiResponse.success("Fuel log deleted successfully."));
    }
}
