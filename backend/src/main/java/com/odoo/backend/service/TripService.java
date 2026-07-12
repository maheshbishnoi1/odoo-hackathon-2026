package com.odoo.backend.service;

import com.odoo.backend.dto.TripRequest;
import com.odoo.backend.dto.TripResponse;
import com.odoo.backend.dto.TripUpdateRequest;
import com.odoo.backend.enums.TripStatus;

import java.util.List;

/**
 * ============================================================================
 * Trip Service Interface
 * ============================================================================
 *
 * Defines all business operations related to Trip Management.
 *
 * Responsibilities:
 * • Create Trip
 * • Update Trip
 * • Delete Trip
 * • Get Trip by ID
 * • Get All Trips
 * • Get Trips by Status
 * • Get Trips by Vehicle
 * • Get Trips by Driver
 * • Start Trip
 * • Complete Trip
 * • Cancel Trip
 *
 * ============================================================================
 */
public interface TripService {

    /**
     * Creates a new trip.
     *
     * @param request Trip request.
     * @return Created trip.
     */
    TripResponse createTrip(TripRequest request);

    /**
     * Updates an existing trip.
     *
     * @param id Trip ID.
     * @param request Trip update request.
     * @return Updated trip.
     */
    TripResponse updateTrip(Long id, TripUpdateRequest request);

    /**
     * Deletes a trip.
     *
     * @param id Trip ID.
     */
    void deleteTrip(Long id);

    /**
     * Retrieves a trip by ID.
     *
     * @param id Trip ID.
     * @return Trip details.
     */
    TripResponse getTripById(Long id);

    /**
     * Retrieves all trips.
     *
     * @return List of trips.
     */
    List<TripResponse> getAllTrips();

    /**
     * Retrieves trips by status.
     *
     * @param status Trip status.
     * @return List of trips.
     */
    List<TripResponse> getTripsByStatus(TripStatus status);

    /**
     * Retrieves trips assigned to a vehicle.
     *
     * @param vehicleId Vehicle ID.
     * @return List of trips.
     */
    List<TripResponse> getTripsByVehicle(Long vehicleId);

    /**
     * Retrieves trips assigned to a driver.
     *
     * @param driverId Driver ID.
     * @return List of trips.
     */
    List<TripResponse> getTripsByDriver(Long driverId);

    /**
     * Starts a scheduled trip.
     *
     * Business Rules:
     * • Trip must be in SCHEDULED state.
     * • Vehicle becomes IN_USE.
     * • Driver becomes ON_TRIP.
     *
     * @param id Trip ID.
     * @return Updated trip.
     */
    TripResponse startTrip(Long id);

    /**
     * Completes an active trip.
     *
     * Business Rules:
     * • Trip must be IN_PROGRESS.
     * • Vehicle becomes AVAILABLE.
     * • Driver becomes AVAILABLE.
     *
     * @param id Trip ID.
     * @return Updated trip.
     */
    TripResponse completeTrip(Long id);

    /**
     * Cancels a trip.
     *
     * Business Rules:
     * • Scheduled or active trips may be cancelled.
     * • Vehicle becomes AVAILABLE.
     * • Driver becomes AVAILABLE.
     *
     * @param id Trip ID.
     * @return Updated trip.
     */
    TripResponse cancelTrip(Long id);
}