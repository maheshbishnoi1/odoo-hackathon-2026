package com.odoo.backend.repository;

import com.odoo.backend.entity.Vehicle;
import com.odoo.backend.enums.VehicleStatus;
import com.odoo.backend.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * Vehicle Repository
 * ============================================================================
 *
 * Handles all database operations related to Vehicle.
 *
 * ============================================================================
 */

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // =========================================================================
    // Registration Number
    // =========================================================================

    /**
     * Checks whether a registration number already exists.
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Finds vehicle by registration number.
     */
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    // =========================================================================
    // Status
    // =========================================================================

    /**
     * Finds all vehicles by status.
     */
    List<Vehicle> findByStatus(VehicleStatus status);

    Page<Vehicle> findByStatus(
            VehicleStatus status,
            Pageable pageable
    );

    // =========================================================================
    // Vehicle Type
    // =========================================================================

    /**
     * Finds all vehicles by type.
     */
    List<Vehicle> findByVehicleType(VehicleType vehicleType);

    Page<Vehicle> findByVehicleType(
            VehicleType vehicleType,
            Pageable pageable
    );

    // =========================================================================
    // Available Vehicles
    // =========================================================================

    /**
     * Returns all vehicles available for dispatch.
     */
    @Query("""
            SELECT v
            FROM Vehicle v
            WHERE v.status = 'AVAILABLE'
            ORDER BY v.vehicleName
            """)
    List<Vehicle> findAvailableVehicles();

    // =========================================================================
    // Search
    // =========================================================================

    /**
     * Search by registration number or vehicle name.
     */
    @Query("""
            SELECT v
            FROM Vehicle v
            WHERE LOWER(v.registrationNumber)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(v.vehicleName)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Vehicle> search(
            String keyword,
            Pageable pageable
    );

    // =========================================================================
    // Dashboard
    // =========================================================================

    long countByStatus(VehicleStatus status);

    long countByVehicleType(VehicleType vehicleType);

}