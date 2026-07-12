package com.odoo.backend.service;

import com.odoo.backend.dto.DriverRequest;
import com.odoo.backend.dto.DriverResponse;
import com.odoo.backend.dto.DriverUpdateRequest;
import com.odoo.backend.enums.DriverStatus;

import java.util.List;

/**
 * ============================================================================
 * Driver Service Interface
 * ============================================================================
 *
 * Defines all business operations related to Driver management.
 *
 * Responsibilities:
 * • Create Driver
 * • Update Driver
 * • Delete Driver
 * • Get Driver by ID
 * • Get All Drivers
 * • Search Drivers
 * • Get Drivers by Status
 * • Get Available Drivers
 *
 * ============================================================================
 */
public interface DriverService {

    /**
     * Creates a new driver.
     *
     * @param request Driver request.
     * @return Created driver.
     */
    DriverResponse createDriver(DriverRequest request);

    /**
     * Updates an existing driver.
     *
     * @param id Driver ID.
     * @param request Driver update request.
     * @return Updated driver.
     */
    DriverResponse updateDriver(Long id, DriverUpdateRequest request);

    /**
     * Deletes a driver.
     *
     * @param id Driver ID.
     */
    void deleteDriver(Long id);

    /**
     * Retrieves a driver by ID.
     *
     * @param id Driver ID.
     * @return Driver details.
     */
    DriverResponse getDriverById(Long id);

    /**
     * Retrieves all drivers.
     *
     * @return List of drivers.
     */
    List<DriverResponse> getAllDrivers();

    /**
     * Searches drivers by first name or last name.
     *
     * @param keyword Search keyword.
     * @return Matching drivers.
     */
    List<DriverResponse> searchDrivers(String keyword);

    /**
     * Retrieves drivers by status.
     *
     * @param status Driver status.
     * @return List of drivers.
     */
    List<DriverResponse> getDriversByStatus(DriverStatus status);

    /**
     * Retrieves all available drivers.
     *
     * @return List of available drivers.
     */
    List<DriverResponse> getAvailableDrivers();
}