package com.odoo.backend.controller;

import com.odoo.backend.dto.ApiResponse;
import com.odoo.backend.dto.MaintenanceRequestDTO;
import com.odoo.backend.dto.MaintenanceResponseDTO;
import com.odoo.backend.enums.MaintenanceStatus;
import com.odoo.backend.enums.MaintenanceType;
import com.odoo.backend.service.MaintenanceService;
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
 * REST controller for the Maintenance module of TransitOps.
 *
 * <p>Base path: {@code /api/v1/maintenance}</p>
 *
 * <p>All endpoints return a standardised {@link ApiResponse} envelope.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "Vehicle maintenance record management APIs")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    // -----------------------------------------------------------------------
    // POST /api/v1/maintenance
    // -----------------------------------------------------------------------

    /**
     * Creates a new maintenance record for a vehicle.
     * Sets the vehicle status to IN_SHOP automatically.
     */
    @Operation(
            summary     = "Create a maintenance record",
            description = "Creates a new maintenance record for a vehicle and marks the vehicle as IN_SHOP."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description  = "Maintenance record created successfully",
                    content      = @Content(schema = @Schema(implementation = MaintenanceResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceResponseDTO>> createMaintenance(
            @Valid @RequestBody MaintenanceRequestDTO dto) {

        log.debug("POST /api/v1/maintenance — vehicleId={}", dto.getVehicleId());
        MaintenanceResponseDTO response = maintenanceService.createMaintenance(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Maintenance record created successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/maintenance/{id}
    // -----------------------------------------------------------------------

    /**
     * Retrieves a maintenance record by its ID.
     */
    @Operation(summary = "Get maintenance record by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Record found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Record not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceResponseDTO>> getMaintenanceById(
            @Parameter(description = "Maintenance record ID", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success("Maintenance record retrieved.", maintenanceService.getMaintenanceById(id)));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/maintenance/vehicle/{vehicleId}
    // -----------------------------------------------------------------------

    /**
     * Retrieves all maintenance records for a specific vehicle.
     */
    @Operation(summary = "Get all maintenance records for a vehicle")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Records retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<ApiResponse<List<MaintenanceResponseDTO>>> getMaintenanceByVehicle(
            @Parameter(description = "Vehicle ID", example = "1")
            @PathVariable Long vehicleId) {

        List<MaintenanceResponseDTO> records = maintenanceService.getMaintenanceByVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Maintenance records for vehicle retrieved.", records));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/maintenance  (paginated + filtered)
    // -----------------------------------------------------------------------

    /**
     * Returns a paginated, filterable list of maintenance records.
     */
    @Operation(
            summary     = "List all maintenance records (paginated)",
            description = "Supports filtering by vehicleId, status, maintenanceType, and date range."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MaintenanceResponseDTO>>> getAllMaintenance(
            @Parameter(description = "Filter by vehicle ID")
            @RequestParam(required = false) Long vehicleId,

            @Parameter(description = "Filter by maintenance status")
            @RequestParam(required = false) MaintenanceStatus status,

            @Parameter(description = "Filter by maintenance type")
            @RequestParam(required = false) MaintenanceType maintenanceType,

            @Parameter(description = "Filter: start date from (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "Filter: start date to (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<MaintenanceResponseDTO> page = maintenanceService.getAllMaintenance(
                vehicleId, status, maintenanceType, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Maintenance records retrieved.", page));
    }

    // -----------------------------------------------------------------------
    // PUT /api/v1/maintenance/{id}
    // -----------------------------------------------------------------------

    /**
     * Updates a maintenance record.
     */
    @Operation(
            summary     = "Update a maintenance record",
            description = "Updates fields of an existing maintenance record. Completing maintenance (status=COMPLETED) restores vehicle to AVAILABLE."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Record updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Record not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceResponseDTO>> updateMaintenance(
            @Parameter(description = "Maintenance record ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceRequestDTO dto) {

        log.debug("PUT /api/v1/maintenance/{} — payload={}", id, dto);
        MaintenanceResponseDTO response = maintenanceService.updateMaintenance(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Maintenance record updated successfully.", response));
    }

    // -----------------------------------------------------------------------
    // PATCH /api/v1/maintenance/{id}/status
    // -----------------------------------------------------------------------

    /**
     * Convenience endpoint to update only the status of a maintenance record.
     */
    @Operation(
            summary     = "Update maintenance status",
            description = "Quick status transition endpoint. Setting status=COMPLETED automatically restores vehicle to AVAILABLE."
    )
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<MaintenanceResponseDTO>> updateStatus(
            @Parameter(description = "Maintenance record ID", example = "1")
            @PathVariable Long id,

            @Parameter(description = "New status", required = true)
            @RequestParam MaintenanceStatus status) {

        log.debug("PATCH /api/v1/maintenance/{}/status — status={}", id, status);
        MaintenanceResponseDTO response = maintenanceService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Maintenance status updated to " + status, response));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/v1/maintenance/{id}
    // -----------------------------------------------------------------------

    /**
     * Deletes a maintenance record.
     */
    @Operation(
            summary     = "Delete a maintenance record",
            description = "Permanently deletes the record. If the record is still active, the vehicle status is restored to AVAILABLE."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Record deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Record not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMaintenance(
            @Parameter(description = "Maintenance record ID", example = "1")
            @PathVariable Long id) {

        maintenanceService.deleteMaintenance(id);
        return ResponseEntity.ok(ApiResponse.success("Maintenance record deleted successfully."));
    }
}
