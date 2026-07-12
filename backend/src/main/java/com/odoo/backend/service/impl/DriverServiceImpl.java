package com.odoo.backend.service.impl;

import com.odoo.backend.dto.DriverRequest;
import com.odoo.backend.dto.DriverResponse;
import com.odoo.backend.dto.DriverUpdateRequest;
import com.odoo.backend.entity.Driver;
import com.odoo.backend.enums.DriverStatus;
import com.odoo.backend.exception.DriverNotFoundException;
import com.odoo.backend.mapper.DriverMapper;
import com.odoo.backend.repository.DriverRepository;
import com.odoo.backend.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * Driver Service Implementation
 * ============================================================================
 *
 * Implements all business logic related to Driver management.
 *
 * Responsibilities:
 * • Driver CRUD Operations
 * • Duplicate validation
 * • Business rule validation
 * • Search
 * • Status filtering
 *
 * ============================================================================
 */

@Service
@RequiredArgsConstructor
@Transactional
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    /**
     * Creates a new Driver.
     */
    @Override
    public DriverResponse createDriver(DriverRequest request) {

        validateDuplicateFields(
                request.getEmail(),
                request.getPhone(),
                request.getLicenseNumber()
        );

        validateAge(request.getDateOfBirth());

        validateLicenseExpiry(request.getLicenseExpiryDate());

        Driver driver = driverMapper.toEntity(request);

        Driver savedDriver = driverRepository.save(driver);

        return driverMapper.toResponse(savedDriver);
    }

    /**
     * Validates duplicate email, phone and license number.
     */
    private void validateDuplicateFields(
            String email,
            String phone,
            String licenseNumber
    ) {

        if (driverRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Driver with email already exists."
            );
        }

        if (driverRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException(
                    "Driver with phone number already exists."
            );
        }

        if (driverRepository.existsByLicenseNumber(licenseNumber)) {
            throw new IllegalArgumentException(
                    "Driver with license number already exists."
            );
        }
    }

    /**
     * Driver must be at least 18 years old.
     */
    private void validateAge(LocalDate dateOfBirth) {

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();

        if (age < 18) {
            throw new IllegalArgumentException(
                    "Driver must be at least 18 years old."
            );
        }
    }

    /**
     * License must not be expired.
     */
    private void validateLicenseExpiry(LocalDate expiryDate) {

        if (expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Driver license has already expired."
            );
        }
    }

    /**
     * Finds driver by ID or throws exception.
     */
    private Driver getDriverEntity(Long id) {

        return driverRepository.findById(id)
                .orElseThrow(() ->
                        new DriverNotFoundException(
                                "Driver not found with ID : " + id
                        )
                );
    }

    // =========================================================================
    // Remaining methods will be implemented in Part 9.2 and Part 9.3
    // =========================================================================

    /**
     * Updates an existing driver.
     */
    @Override
    public DriverResponse updateDriver(Long id, DriverUpdateRequest request) {

        Driver driver = getDriverEntity(id);

        // Validate Email
        if (!driver.getEmail().equalsIgnoreCase(request.getEmail())
                && driverRepository.existsByEmail(request.getEmail())) {

            throw new IllegalArgumentException(
                    "Driver with email already exists."
            );
        }

        // Validate Phone
        if (!driver.getPhone().equals(request.getPhone())
                && driverRepository.existsByPhone(request.getPhone())) {

            throw new IllegalArgumentException(
                    "Driver with phone number already exists."
            );
        }

        // Validate License Number
        if (!driver.getLicenseNumber().equalsIgnoreCase(request.getLicenseNumber())
                && driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {

            throw new IllegalArgumentException(
                    "Driver with license number already exists."
            );
        }

        // Business validations
        validateAge(request.getDateOfBirth());
        validateLicenseExpiry(request.getLicenseExpiryDate());

        // Update entity
        driverMapper.updateEntity(driver, request);

        Driver updatedDriver = driverRepository.save(driver);

        return driverMapper.toResponse(updatedDriver);
    }

    /**
     * Deletes a driver.
     */
    @Override
    public void deleteDriver(Long id) {

        Driver driver = getDriverEntity(id);

        /*
         * Business Rule:
         * Drivers currently on an active trip
         * cannot be deleted.
         *
         * (Later this should check Trip table once Trip module is completed.)
         */
        if (driver.getStatus() == DriverStatus.ON_TRIP) {

            throw new IllegalStateException(
                    "Driver cannot be deleted while assigned to an active trip."
            );
        }

        driverRepository.delete(driver);
    }

    /**
     * Retrieves a driver by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public DriverResponse getDriverById(Long id) {

        Driver driver = getDriverEntity(id);

        return driverMapper.toResponse(driver);
    }

    /**
     * Retrieves all drivers.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers() {

        return driverRepository.findAll()
                .stream()
                .map(driverMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Searches drivers by first name or last name.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> searchDrivers(String keyword) {

        List<Driver> drivers =
                driverRepository
                        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                                keyword,
                                keyword
                        );

        return drivers.stream()
                .map(driverMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves drivers by status.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> getDriversByStatus(DriverStatus status) {

        return driverRepository.findByStatus(status)
                .stream()
                .map(driverMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all available drivers.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> getAvailableDrivers() {

        return driverRepository
                .findByStatusOrderByFirstNameAsc(DriverStatus.AVAILABLE)
                .stream()
                .map(driverMapper::toResponse)
                .collect(Collectors.toList());
    }
}