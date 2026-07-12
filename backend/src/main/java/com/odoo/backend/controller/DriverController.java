package com.odoo.backend.controller;

import com.odoo.backend.dto.DriverRequest;
import com.odoo.backend.dto.DriverResponse;
import com.odoo.backend.dto.DriverUpdateRequest;
import com.odoo.backend.enums.DriverStatus;
import com.odoo.backend.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================================
 * Driver Controller
 * ============================================================================
 *
 * REST APIs for Driver Management.
 *
 * Base URL:
 * /api/drivers
 *
 * ============================================================================
 */

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Tag(name = "Driver Management", description = "APIs for managing drivers")
public class DriverController {

    private final DriverService driverService;

    /**
     * Create Driver
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Driver")
    public ResponseEntity<DriverResponse> createDriver(
            @Valid @RequestBody DriverRequest request) {

        DriverResponse response = driverService.createDriver(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get All Drivers
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get All Drivers")
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {

        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    /**
     * Get Driver By ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get Driver By ID")
    public ResponseEntity<DriverResponse> getDriverById(
            @PathVariable Long id) {

        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    /**
     * Update Driver
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Driver")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable Long id,
            @Valid @RequestBody DriverUpdateRequest request) {

        return ResponseEntity.ok(driverService.updateDriver(id, request));
    }

    /**
     * Delete Driver
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Driver")
    public ResponseEntity<Void> deleteDriver(
            @PathVariable Long id) {

        driverService.deleteDriver(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Search Drivers
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Search Drivers")
    public ResponseEntity<List<DriverResponse>> searchDrivers(
            @RequestParam String keyword) {

        return ResponseEntity.ok(driverService.searchDrivers(keyword));
    }

    /**
     * Get Drivers By Status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get Drivers By Status")
    public ResponseEntity<List<DriverResponse>> getDriversByStatus(
            @PathVariable DriverStatus status) {

        return ResponseEntity.ok(driverService.getDriversByStatus(status));
    }

    /**
     * Get Available Drivers
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get Available Drivers")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers() {

        return ResponseEntity.ok(driverService.getAvailableDrivers());
    }

}