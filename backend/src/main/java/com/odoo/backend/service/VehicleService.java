package com.odoo.backend.service;

import com.odoo.backend.dto.vehicle.VehicleRequestDTO;
import com.odoo.backend.dto.vehicle.VehicleResponseDTO;
import com.odoo.backend.entity.Vehicle;
import com.odoo.backend.enums.VehicleStatus;
import com.odoo.backend.exception.BadRequestException;
import com.odoo.backend.exception.VehicleNotFoundException;
import com.odoo.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ============================================================================
 * Vehicle Service
 * ============================================================================
 *
 * Handles:
 *
 * • Vehicle Registration
 * • Vehicle CRUD
 * • Vehicle Status Updates
 * • Vehicle Search
 * • Business Rules
 *
 * ============================================================================
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    // =========================================================================
    // CREATE VEHICLE
    // =========================================================================

    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public VehicleResponseDTO createVehicle(VehicleRequestDTO request) {

        log.info("Registering vehicle : {}", request.getRegistrationNumber());

        if (vehicleRepository.existsByRegistrationNumber(
                request.getRegistrationNumber())) {

            throw new BadRequestException(
                    "Vehicle with registration number already exists."
            );
        }

        Vehicle vehicle = new Vehicle();

        BeanUtils.copyProperties(request, vehicle);

        if (vehicle.getStatus() == null) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
        }

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        log.info("Vehicle created successfully : {}",
                savedVehicle.getRegistrationNumber());

        return VehicleResponseDTO.from(savedVehicle);
    }

    // =========================================================================
    // GET ALL VEHICLES
    // =========================================================================

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Page<VehicleResponseDTO> getAllVehicles(Pageable pageable) {

        return vehicleRepository.findAll(pageable)
                .map(VehicleResponseDTO::from);
    }

    // =========================================================================
    // GET VEHICLE BY ID
    // =========================================================================

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public VehicleResponseDTO getVehicleById(Long vehicleId) {

        return VehicleResponseDTO.from(findVehicle(vehicleId));
    }

    // =========================================================================
    // UPDATE VEHICLE
    // =========================================================================

    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public VehicleResponseDTO updateVehicle(
            Long vehicleId,
            VehicleRequestDTO request) {

        Vehicle vehicle = findVehicle(vehicleId);

        // Registration number uniqueness check
        if (!vehicle.getRegistrationNumber().equalsIgnoreCase(request.getRegistrationNumber())
                && vehicleRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {

            throw new BadRequestException(
                    "Vehicle with registration number already exists."
            );
        }

        BeanUtils.copyProperties(request, vehicle, "id", "createdAt");

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);

        log.info("Vehicle updated successfully : {}",
                updatedVehicle.getRegistrationNumber());

        return VehicleResponseDTO.from(updatedVehicle);
    }

    // =========================================================================
    // UPDATE VEHICLE STATUS
    // =========================================================================

    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public VehicleResponseDTO updateVehicleStatus(
            Long vehicleId,
            VehicleStatus status) {

        Vehicle vehicle = findVehicle(vehicleId);

        // ---------------------------------------------------------------------
        // Business Rules
        // ---------------------------------------------------------------------

        // Retired vehicle cannot become available again
        if (vehicle.getStatus() == VehicleStatus.RETIRED
                && status != VehicleStatus.RETIRED) {

            throw new BadRequestException(
                    "Retired vehicle cannot be activated again."
            );
        }

        // Vehicle on trip cannot directly go to maintenance
        if (vehicle.getStatus() == VehicleStatus.ON_TRIP
                && status == VehicleStatus.IN_SHOP) {

            throw new BadRequestException(
                    "Complete the trip before sending vehicle to maintenance."
            );
        }

        vehicle.changeStatus(status);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);

        log.info("Vehicle status changed : {} -> {}",
                updatedVehicle.getRegistrationNumber(),
                status);

        return VehicleResponseDTO.from(updatedVehicle);
    }

    // =========================================================================
    // DELETE VEHICLE
    // =========================================================================

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteVehicle(Long vehicleId) {

        Vehicle vehicle = findVehicle(vehicleId);

        if (vehicle.getStatus() == VehicleStatus.ON_TRIP) {

            throw new BadRequestException(
                    "Cannot delete a vehicle that is currently on a trip."
            );
        }

        vehicleRepository.delete(vehicle);

        log.info("Vehicle deleted : {}",
                vehicle.getRegistrationNumber());
    }

    // =========================================================================
    // SEARCH VEHICLES
    // =========================================================================

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Page<VehicleResponseDTO> searchVehicles(
            String keyword,
            Pageable pageable) {

        return vehicleRepository.search(keyword, pageable)
                .map(VehicleResponseDTO::from);
    }

    // =========================================================================
    // GET VEHICLES BY STATUS
    // =========================================================================

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Page<VehicleResponseDTO> getVehiclesByStatus(
            VehicleStatus status,
            Pageable pageable) {

        return vehicleRepository.findByStatus(status, pageable)
                .map(VehicleResponseDTO::from);
    }

    // =========================================================================
    // DASHBOARD METHODS
    // =========================================================================

    @Transactional(readOnly = true)
    public long getAvailableVehicleCount() {
        return vehicleRepository.countByStatus(VehicleStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public long getOnTripVehicleCount() {
        return vehicleRepository.countByStatus(VehicleStatus.ON_TRIP);
    }

    @Transactional(readOnly = true)
    public long getInShopVehicleCount() {
        return vehicleRepository.countByStatus(VehicleStatus.IN_SHOP);
    }

    @Transactional(readOnly = true)
    public long getRetiredVehicleCount() {
        return vehicleRepository.countByStatus(VehicleStatus.RETIRED);
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Finds a vehicle by ID or throws an exception.
     */
    private Vehicle findVehicle(Long vehicleId) {

        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() ->
                        new VehicleNotFoundException(vehicleId));
    }

}