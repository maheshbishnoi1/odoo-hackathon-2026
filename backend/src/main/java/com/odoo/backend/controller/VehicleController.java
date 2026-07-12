package com.odoo.backend.controller;

import com.odoo.backend.dto.common.ApiResponse;
import com.odoo.backend.dto.vehicle.VehicleRequestDTO;
import com.odoo.backend.dto.vehicle.VehicleResponseDTO;
import com.odoo.backend.enums.VehicleStatus;
import com.odoo.backend.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================================
 * Vehicle Controller
 * ============================================================================
 *
 * Handles:
 * • Vehicle Registration
 * • Vehicle CRUD
 * • Vehicle Search
 * • Vehicle Status Updates
 *
 * Base URL:
 * /api/vehicles
 *
 * ============================================================================
 */

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // =========================================================================
    // CREATE VEHICLE
    // =========================================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleResponseDTO>> createVehicle(
            @Valid @RequestBody VehicleRequestDTO request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Vehicle registered successfully.",
                        vehicleService.createVehicle(request)
                )
        );
    }

    // =========================================================================
    // GET ALL VEHICLES
    // =========================================================================

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VehicleResponseDTO>>> getAllVehicles(
            @PageableDefault(size = 10, sort = "createdAt")
            Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Vehicles retrieved successfully.",
                        vehicleService.getAllVehicles(pageable)
                )
        );
    }

    // =========================================================================
    // GET VEHICLE BY ID
    // =========================================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponseDTO>> getVehicleById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Vehicle retrieved successfully.",
                        vehicleService.getVehicleById(id)
                )
        );
    }

    // =========================================================================
    // UPDATE VEHICLE
    // =========================================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleResponseDTO>> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequestDTO request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Vehicle updated successfully.",
                        vehicleService.updateVehicle(id, request)
                )
        );
    }

    // =========================================================================
    // UPDATE STATUS
    // =========================================================================

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleResponseDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam VehicleStatus status) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Vehicle status updated successfully.",
                        vehicleService.updateVehicleStatus(id, status)
                )
        );
    }

    // =========================================================================
    // DELETE VEHICLE
    // =========================================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @PathVariable Long id) {

        vehicleService.deleteVehicle(id);

        return ResponseEntity.ok(
                ApiResponse.success("Vehicle deleted successfully.")
        );
    }

    // =========================================================================
    // SEARCH VEHICLES
    // =========================================================================

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<VehicleResponseDTO>>> searchVehicles(
            @RequestParam String keyword,
            @PageableDefault(size = 10)
            Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Vehicles retrieved successfully.",
                        vehicleService.searchVehicles(keyword, pageable)
                )
        );
    }

    // =========================================================================
    // FILTER BY STATUS
    // =========================================================================

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<VehicleResponseDTO>>> getVehiclesByStatus(
            @PathVariable VehicleStatus status,
            @PageableDefault(size = 10)
            Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Vehicles retrieved successfully.",
                        vehicleService.getVehiclesByStatus(status, pageable)
                )
        );
    }

}