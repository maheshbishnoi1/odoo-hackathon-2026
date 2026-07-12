package com.odoo.backend.repository;

import com.odoo.backend.entity.Driver;
import com.odoo.backend.entity.Trip;
import com.odoo.backend.entity.Vehicle;
import com.odoo.backend.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
 * Provides CRUD operations, specifications, and custom queries.
 *
 * Business Rules:
 * • Trip Number must be unique.
 * • One driver can have only one active trip.
 * • One vehicle can have only one active trip.
 *
 * ============================================================================
 */

@Repository
public interface TripRepository extends JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip> {

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

    /**
     * Returns all trips associated with a given vehicle ordered by start time desc.
     *
     * @param vehicleId the vehicle primary key
     * @return list of trips for that vehicle
     */
    List<Trip> findByVehicle_IdOrderByStartTimeDesc(Long vehicleId);

    /**
     * Returns all trips ordered by start time descending.
     *
     * @return ordered list of trips
     */
    List<Trip> findAllByOrderByStartTimeDesc();

    /**
     * Returns most recent N trips for dashboard feed ordered by creation date descending.
     * Uses Spring Data's Pageable for limiting.
     */
    org.springframework.data.domain.Page<Trip> findAllByOrderByCreatedAtDesc(
            org.springframework.data.domain.Pageable pageable);

    /**
     * Aggregates trip counts and total distance per driver.
     * Each row: [driverId, COUNT(t), SUM(t.distance), completedCount].
     */
    @org.springframework.data.jpa.repository.Query("""
            SELECT t.driver.id, COUNT(t), SUM(t.distance),
                   SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END)
            FROM Trip t
            GROUP BY t.driver.id
            """)
    List<Object[]> aggregateTripStatsByDriver();

    /**
     * Returns trips for a given vehicle that were started within the last N days.
     * Used for idle vehicle detection in fleet health analytics.
     */
    @org.springframework.data.jpa.repository.Query("""
            SELECT t
            FROM Trip t
            WHERE t.vehicle.id = :vehicleId
              AND t.startTime >= :since
            ORDER BY t.startTime DESC
            """)
    List<Trip> findRecentTripsForVehicle(
            @org.springframework.data.repository.query.Param("vehicleId") Long vehicleId,
            @org.springframework.data.repository.query.Param("since") java.time.LocalDateTime since);

    /**
     * Counts trips for each status grouped.
     * Each row: [TripStatus, count].
     */
    @org.springframework.data.jpa.repository.Query(
            "SELECT t.status, COUNT(t) FROM Trip t GROUP BY t.status")
    List<Object[]> countTripsGroupedByStatus();
}
