package com.odoo.backend.repository;

import com.odoo.backend.entity.Vehicle;
import com.odoo.backend.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Vehicle} entities.
 *
 * <p><strong>STUB — Extend with additional query methods as needed by the Vehicle module owner.</strong></p>
 */
@Repository
public interface VehicleRepository
        extends JpaRepository<Vehicle, Long>,
                JpaSpecificationExecutor<Vehicle> {

    /**
     * Finds a vehicle by its unique registration number.
     *
     * @param registrationNumber the plate number
     * @return an {@link Optional} containing the vehicle, or empty if not found
     */
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    /**
     * Checks whether a vehicle with the given registration number exists.
     *
     * @param registrationNumber the plate number
     * @return {@code true} if exists
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Finds vehicles by operational status.
     *
     * @param status the vehicle status to filter by
     * @return list of vehicles with that status
     */
    java.util.List<Vehicle> findByStatus(VehicleStatus status);

    /**
     * Counts vehicles grouped by operational status.
     * Each element: [VehicleStatus, Long count].
     *
     * @return list of status counts
     */
    @org.springframework.data.jpa.repository.Query("SELECT v.status, COUNT(v) FROM Vehicle v GROUP BY v.status")
    java.util.List<Object[]> countVehiclesGroupedByStatus();
}
