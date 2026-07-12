package com.odoo.backend.repository;

import com.odoo.backend.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Trip} entities.
 *
 * <p><strong>STUB — Extend with additional query methods as needed by the Trip module owner.</strong></p>
 */
@Repository
public interface TripRepository
        extends JpaRepository<Trip, Long>,
                JpaSpecificationExecutor<Trip> {

    /**
     * Finds a trip by its unique trip number.
     *
     * @param tripNumber the trip reference number
     * @return an {@link Optional} containing the trip, or empty if not found
     */
    Optional<Trip> findByTripNumber(String tripNumber);

    /**
     * Returns all trips associated with a given vehicle.
     *
     * @param vehicleId the vehicle primary key
     * @return list of trips for that vehicle
     */
    java.util.List<Trip> findByVehicle_IdOrderByTripDateDesc(Long vehicleId);

    /**
     * Returns all trips ordered by trip date descending.
     *
     * @return ordered list of trips
     */
    java.util.List<Trip> findAllByOrderByTripDateDesc();
}
