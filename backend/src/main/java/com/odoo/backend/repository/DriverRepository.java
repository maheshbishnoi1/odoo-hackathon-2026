package com.odoo.backend.repository;

import com.odoo.backend.entity.Driver;
import com.odoo.backend.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * Driver Repository
 * ============================================================================
 *
 * Repository interface for Driver entity.
 *
 * Provides CRUD operations and custom database queries.
 *
 * Business Rules:
 * • Email must be unique.
 * • Phone number must be unique.
 * • License number must be unique.
 * • Drivers can be searched by name.
 * • Drivers can be filtered by status.
 * • Available drivers can be fetched for trip assignment.
 * • Drivers with expired licenses can be identified.
 *
 * ============================================================================
 */

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    /**
     * Find driver by email.
     *
     * @param email Driver email.
     * @return Optional Driver.
     */
    Optional<Driver> findByEmail(String email);

    /**
     * Find driver by phone number.
     *
     * @param phone Driver phone.
     * @return Optional Driver.
     */
    Optional<Driver> findByPhone(String phone);

    /**
     * Find driver by license number.
     *
     * @param licenseNumber License number.
     * @return Optional Driver.
     */
    Optional<Driver> findByLicenseNumber(String licenseNumber);

    /**
     * Check if email already exists.
     *
     * @param email Driver email.
     * @return true if exists.
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone already exists.
     *
     * @param phone Driver phone.
     * @return true if exists.
     */
    boolean existsByPhone(String phone);

    /**
     * Check if license number already exists.
     *
     * @param licenseNumber License number.
     * @return true if exists.
     */
    boolean existsByLicenseNumber(String licenseNumber);

    /**
     * Find drivers by status.
     *
     * @param status Driver status.
     * @return List of drivers.
     */
    List<Driver> findByStatus(DriverStatus status);

    /**
     * Search drivers by first name.
     *
     * @param firstName Driver first name.
     * @return Matching drivers.
     */
    List<Driver> findByFirstNameContainingIgnoreCase(String firstName);

    /**
     * Search drivers by last name.
     *
     * @param lastName Driver last name.
     * @return Matching drivers.
     */
    List<Driver> findByLastNameContainingIgnoreCase(String lastName);

    /**
     * Search drivers by first or last name.
     *
     * @param firstName First name.
     * @param lastName Last name.
     * @return Matching drivers.
     */
    List<Driver> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName
    );

    /**
     * Find all available drivers.
     *
     * @return Available drivers.
     */
    List<Driver> findByStatusOrderByFirstNameAsc(DriverStatus status);

    /**
     * Find drivers whose licenses expire before a given date.
     *
     * @param expiryDate Expiry date.
     * @return Drivers with expired/expiring licenses.
     */
    List<Driver> findByLicenseExpiryDateBefore(LocalDate expiryDate);

    /**
     * Count drivers by status.
     *
     * @param status Driver status.
     * @return Number of drivers.
     */
    long countByStatus(DriverStatus status);

}