package com.odoo.backend.controller;

import com.odoo.backend.dto.TripRequest;
import com.odoo.backend.dto.TripResponse;
import com.odoo.backend.dto.TripUpdateRequest;
import com.odoo.backend.enums.TripStatus;
import com.odoo.backend.service.TripService;
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
 * Trip Controller
 * ============================================================================
 *
 * REST APIs for Trip Management.
 *
 * Base URL:
 * /api/trips
 *
 * ============================================================================
 */

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Tag(name = "Trip Management", description = "APIs for managing fleet trips")
public class TripController {

    private final TripService tripService;

    /**
     * Create Trip
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new trip")
    public ResponseEntity<TripResponse> createTrip(
            @Valid @RequestBody TripRequest request) {

        TripResponse response = tripService.createTrip(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get All Trips
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get all trips")
    public ResponseEntity<List<TripResponse>> getAllTrips() {

        return ResponseEntity.ok(
                tripService.getAllTrips()
        );
    }

    /**
     * Get Trip By ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get trip by ID")
    public ResponseEntity<TripResponse> getTripById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                tripService.getTripById(id)
        );
    }

    /**
     * Update Trip
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update trip")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripUpdateRequest request) {

        return ResponseEntity.ok(
                tripService.updateTrip(id, request)
        );
    }

    /**
     * Delete Trip
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete trip")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable Long id) {

        tripService.deleteTrip(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Start Trip
     */
    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Start a scheduled trip")
    public ResponseEntity<TripResponse> startTrip(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                tripService.startTrip(id)
        );
    }

    /**
     * Complete Trip
     */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Complete an active trip")
    public ResponseEntity<TripResponse> completeTrip(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                tripService.completeTrip(id)
        );
    }

    /**
     * Cancel Trip
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Cancel a trip")
    public ResponseEntity<TripResponse> cancelTrip(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                tripService.cancelTrip(id)
        );
    }

    /**
     * Get Trips By Status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get trips by status")
    public ResponseEntity<List<TripResponse>> getTripsByStatus(
            @PathVariable TripStatus status) {

        return ResponseEntity.ok(
                tripService.getTripsByStatus(status)
        );
    }

    /**
     * Get Trips By Vehicle
     */
    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get trips by vehicle")
    public ResponseEntity<List<TripResponse>> getTripsByVehicle(
            @PathVariable Long vehicleId) {

        return ResponseEntity.ok(
                tripService.getTripsByVehicle(vehicleId)
        );
    }

    /**
     * Get Trips By Driver
     */
    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get trips by driver")
    public ResponseEntity<List<TripResponse>> getTripsByDriver(
            @PathVariable Long driverId) {

        return ResponseEntity.ok(
                tripService.getTripsByDriver(driverId)
        );
    }

}