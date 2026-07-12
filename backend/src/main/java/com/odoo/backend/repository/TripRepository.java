package com.odoo.backend.repository;

import com.odoo.backend.entity.Driver;
import com.odoo.backend.entity.Trip;
import com.odoo.backend.entity.Vehicle;
import com.odoo.backend.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * Trip Repository
 * ============================================================================
 *
 * Repository interface for Trip entity.
 *
 * Provides CRUD operations and custom queries.
 *
 * Business Rules:
 * • Trip Number must be unique.
 * • One driver can have only one active trip.
 * • One vehicle can have only one active trip.
 *
 * ============================================================================
 */

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    /**
     * Find trip by trip number.
     *
     * @param tripNumber Trip number.
     * @return Optional Trip.
     */
    Optional<Trip> findByTripNumber(String tripNumber);

    /**
     * Checks whether trip number already exists.
     *
     * @param tripNumber Trip number.
     * @return true if exists.
     */
    boolean existsByTripNumber(String tripNumber);

    /**
     * Returns all trips with the given status.
     *
     * @param status Trip status.
     * @return List of trips.
     */
    List<Trip> findByStatus(TripStatus status);

    /**
     * Returns all trips assigned to a vehicle.
     *
     * @param vehicle Vehicle.
     * @return List of trips.
     */
    List<Trip> findByVehicle(Vehicle vehicle);

    /**
     * Returns all trips assigned to a driver.
     *
     * @param driver Driver.
     * @return List of trips.
     */
    List<Trip> findByDriver(Driver driver);

    /**
     * Checks whether a vehicle has an active trip.
     *
     * @param vehicle Vehicle.
     * @param status Active status.
     * @return true if active trip exists.
     */
    boolean existsByVehicleAndStatus(
            Vehicle vehicle,
            TripStatus status
    );

    /**
     * Checks whether a driver has an active trip.
     *
     * @param driver Driver.
     * @param status Active status.
     * @return true if active trip exists.
     */
    boolean existsByDriverAndStatus(
            Driver driver,
            TripStatus status
    );

    /**
     * Counts trips by status.
     *
     * @param status Trip status.
     * @return Number of trips.
     */
    long countByStatus(TripStatus status);

    /**
     * Returns all trips ordered by latest created first.
     *
     * @return List of trips.
     */
    List<Trip> findAllByOrderByCreatedAtDesc();

}