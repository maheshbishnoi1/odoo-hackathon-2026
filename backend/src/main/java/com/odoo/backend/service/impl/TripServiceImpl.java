package com.odoo.backend.service.impl;

import com.odoo.backend.dto.TripRequest;
import com.odoo.backend.dto.TripResponse;
import com.odoo.backend.dto.TripUpdateRequest;
import com.odoo.backend.entity.Driver;
import com.odoo.backend.entity.Trip;
import com.odoo.backend.entity.Vehicle;
import com.odoo.backend.enums.DriverStatus;
import com.odoo.backend.enums.TripStatus;
import com.odoo.backend.enums.VehicleStatus;
import com.odoo.backend.exception.DriverNotFoundException;
import com.odoo.backend.exception.TripNotFoundException;
import com.odoo.backend.exception.VehicleNotFoundException;
import com.odoo.backend.mapper.TripMapper;
import com.odoo.backend.repository.DriverRepository;
import com.odoo.backend.repository.TripRepository;
import com.odoo.backend.repository.VehicleRepository;
import com.odoo.backend.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * Trip Service Implementation
 * ============================================================================
 */

@Service
@RequiredArgsConstructor
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final TripMapper tripMapper;

    /**
     * Create Trip
     */
    @Override
    public TripResponse createTrip(TripRequest request) {

        Vehicle vehicle = getVehicle(request.getVehicleId());
        Driver driver = getDriver(request.getDriverId());

        validateTripRequest(request, vehicle, driver);

        Trip trip = tripMapper.toEntity(request, vehicle, driver);

        trip.setTripNumber(generateTripNumber());

        Trip savedTrip = tripRepository.save(trip);

        return tripMapper.toResponse(savedTrip);
    }

    /**
     * Generates a unique trip number.
     *
     * Example:
     * TRIP-20260712-0001
     */
    private String generateTripNumber() {

        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        long count = tripRepository.count() + 1;

        return String.format(
                "TRIP-%s-%04d",
                date,
                count
        );
    }

    /**
     * Returns Vehicle entity.
     */
    private Vehicle getVehicle(Long id) {

        return vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new VehicleNotFoundException(
                                "Vehicle not found with ID : " + id
                        ));
    }

    /**
     * Returns Driver entity.
     */
    private Driver getDriver(Long id) {

        return driverRepository.findById(id)
                .orElseThrow(() ->
                        new DriverNotFoundException(
                                "Driver not found with ID : " + id
                        ));
    }

    /**
     * Returns Trip entity.
     */
    private Trip getTrip(Long id) {

        return tripRepository.findById(id)
                .orElseThrow(() ->
                        new TripNotFoundException(
                                "Trip not found with ID : " + id
                        ));
    }

    /**
     * Business Validations
     */
    private void validateTripRequest(
            TripRequest request,
            Vehicle vehicle,
            Driver driver
    ) {

        if (request.getSource()
                .equalsIgnoreCase(request.getDestination())) {

            throw new IllegalArgumentException(
                    "Source and Destination cannot be the same."
            );
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Start time cannot be in the past."
            );
        }

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {

            throw new IllegalArgumentException(
                    "Vehicle is not available."
            );
        }

        if (driver.getStatus() != DriverStatus.AVAILABLE) {

            throw new IllegalArgumentException(
                    "Driver is not available."
            );
        }

        if (!driver.isLicenseValid()) {

            throw new IllegalArgumentException(
                    "Driver license has expired."
            );
        }

        if (tripRepository.existsByVehicleAndStatus(
                vehicle,
                TripStatus.IN_PROGRESS
        )) {

            throw new IllegalArgumentException(
                    "Vehicle already has an active trip."
            );
        }

        if (tripRepository.existsByDriverAndStatus(
                driver,
                TripStatus.IN_PROGRESS
        )) {

            throw new IllegalArgumentException(
                    "Driver already has an active trip."
            );
        }
    }

    // ==========================================================
    // Remaining methods implemented in Part 9.2 / 9.3 / 9.4
    // ==========================================================

    /**
     * Updates an existing trip.
     */
    @Override
    public TripResponse updateTrip(Long id, TripUpdateRequest request) {

        Trip trip = getTrip(id);

        // Only scheduled trips can be updated
        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new IllegalArgumentException(
                    "Only scheduled trips can be updated."
            );
        }

        Vehicle vehicle = getVehicle(request.getVehicleId());
        Driver driver = getDriver(request.getDriverId());

        // Source & Destination validation
        if (request.getSource().equalsIgnoreCase(request.getDestination())) {
            throw new IllegalArgumentException(
                    "Source and Destination cannot be the same."
            );
        }

        // Start time validation
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Start time cannot be in the past."
            );
        }

        // Vehicle validation
        if (!vehicle.getId().equals(trip.getVehicle().getId())) {

            if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
                throw new IllegalArgumentException(
                        "Selected vehicle is not available."
                );
            }

            if (tripRepository.existsByVehicleAndStatus(
                    vehicle,
                    TripStatus.IN_PROGRESS
            )) {
                throw new IllegalArgumentException(
                        "Selected vehicle already has an active trip."
                );
            }
        }

        // Driver validation
        if (!driver.getId().equals(trip.getDriver().getId())) {

            if (driver.getStatus() != DriverStatus.AVAILABLE) {
                throw new IllegalArgumentException(
                        "Selected driver is not available."
                );
            }

            if (!driver.isLicenseValid()) {
                throw new IllegalArgumentException(
                        "Driver license has expired."
                );
            }

            if (tripRepository.existsByDriverAndStatus(
                    driver,
                    TripStatus.IN_PROGRESS
            )) {
                throw new IllegalArgumentException(
                        "Selected driver already has an active trip."
                );
            }
        }

        tripMapper.updateEntity(
                trip,
                request,
                vehicle,
                driver
        );

        Trip updatedTrip = tripRepository.save(trip);

        return tripMapper.toResponse(updatedTrip);
    }

    /**
     * Deletes a trip.
     */
    @Override
    public void deleteTrip(Long id) {

        Trip trip = getTrip(id);

        // Active trips cannot be deleted
        if (trip.getStatus() == TripStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot delete a trip that is currently in progress."
            );
        }

        tripRepository.delete(trip);
    }

    /**
     * Retrieves a trip by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripById(Long id) {

        Trip trip = getTrip(id);

        return tripMapper.toResponse(trip);
    }

    /**
     * Retrieves all trips.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getAllTrips() {

        return tripRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(tripMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves trips by status.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getTripsByStatus(TripStatus status) {

        return tripRepository.findByStatus(status)
                .stream()
                .map(tripMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves trips assigned to a vehicle.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getTripsByVehicle(Long vehicleId) {

        Vehicle vehicle = getVehicle(vehicleId);

        return tripRepository.findByVehicle(vehicle)
                .stream()
                .map(tripMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves trips assigned to a driver.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getTripsByDriver(Long driverId) {

        Driver driver = getDriver(driverId);

        return tripRepository.findByDriver(driver)
                .stream()
                .map(tripMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Starts a scheduled trip.
     */
    @Override
    public TripResponse startTrip(Long id) {

        Trip trip = getTrip(id);

        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new IllegalStateException(
                    "Only scheduled trips can be started."
            );
        }

        Vehicle vehicle = trip.getVehicle();
        Driver driver = trip.getDriver();

        // Final validation before starting
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Vehicle is not available."
            );
        }

        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Driver is not available."
            );
        }

        if (!driver.isLicenseValid()) {
            throw new IllegalStateException(
                    "Driver license has expired."
            );
        }

        // Update statuses
        trip.setStatus(TripStatus.IN_PROGRESS);

        vehicle.setStatus(VehicleStatus.ON_TRIP);

        driver.setStatus(DriverStatus.ON_TRIP);

        vehicleRepository.save(vehicle);
        driverRepository.save(driver);

        Trip updatedTrip = tripRepository.save(trip);

        return tripMapper.toResponse(updatedTrip);
    }

    /**
     * Completes an active trip.
     */
    @Override
    public TripResponse completeTrip(Long id) {

        Trip trip = getTrip(id);

        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Only active trips can be completed."
            );
        }

        Vehicle vehicle = trip.getVehicle();
        Driver driver = trip.getDriver();

        trip.setStatus(TripStatus.COMPLETED);

        trip.setEndTime(LocalDateTime.now());

        if (trip.getStartTime() != null) {

            long minutes = java.time.Duration.between(
                    trip.getStartTime(),
                    trip.getEndTime()
            ).toMinutes();

            trip.setActualDuration((int) minutes);
        }

        vehicle.setStatus(VehicleStatus.AVAILABLE);

        driver.setStatus(DriverStatus.AVAILABLE);

        vehicleRepository.save(vehicle);
        driverRepository.save(driver);

        Trip updatedTrip = tripRepository.save(trip);

        return tripMapper.toResponse(updatedTrip);
    }

    /**
     * Cancels a trip.
     */
    @Override
    public TripResponse cancelTrip(Long id) {

        Trip trip = getTrip(id);

        if (trip.getStatus() == TripStatus.COMPLETED) {

            throw new IllegalStateException(
                    "Completed trip cannot be cancelled."
            );
        }

        if (trip.getStatus() == TripStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Trip is already cancelled."
            );
        }

        trip.setStatus(TripStatus.CANCELLED);

        // If trip was already started,
        // release resources.

        if (trip.getVehicle() != null) {

            trip.getVehicle().setStatus(
                    VehicleStatus.AVAILABLE
            );

            vehicleRepository.save(trip.getVehicle());
        }

        if (trip.getDriver() != null) {

            trip.getDriver().setStatus(
                    DriverStatus.AVAILABLE
            );

            driverRepository.save(trip.getDriver());
        }

        Trip updatedTrip = tripRepository.save(trip);

        return tripMapper.toResponse(updatedTrip);
    }
}