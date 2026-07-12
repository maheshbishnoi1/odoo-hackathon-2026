package com.odoo.backend.service;

import com.odoo.backend.dto.FuelRequestDTO;
import com.odoo.backend.dto.FuelResponseDTO;
import com.odoo.backend.dto.FuelSummaryDTO;
import com.odoo.backend.entity.FuelLog;
import com.odoo.backend.entity.Trip;
import com.odoo.backend.entity.Vehicle;
import com.odoo.backend.enums.FuelType;
import com.odoo.backend.exception.BusinessRuleException;
import com.odoo.backend.exception.ResourceNotFoundException;
import com.odoo.backend.repository.FuelRepository;
import com.odoo.backend.repository.TripRepository;
import com.odoo.backend.repository.VehicleRepository;
import com.odoo.backend.specification.FuelSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for the Fuel module of TransitOps.
 *
 * <p>Business rules enforced:</p>
 * <ol>
 *   <li>Fuel quantity must be greater than zero (enforced by DTO validation).</li>
 *   <li>Fuel cost must be positive (enforced by DTO validation).</li>
 *   <li>Odometer reading cannot be lower than the latest recorded odometer for the vehicle.</li>
 *   <li>{@code costPerLiter} is automatically computed as {@code cost / quantity} and persisted.</li>
 *   <li>Trip association is optional; when provided the trip must exist.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FuelService {

    private final FuelRepository    fuelRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository    tripRepository;

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------

    /**
     * Logs a new fuel fill-up for a vehicle.
     *
     * @param dto the fuel creation request
     * @return the persisted fuel log as a response DTO
     * @throws ResourceNotFoundException if vehicle or trip is not found
     * @throws BusinessRuleException     if odometer reading is lower than the previous maximum
     */
    @Transactional
    public FuelResponseDTO logFuel(FuelRequestDTO dto) {
        Vehicle vehicle = findVehicleOrThrow(dto.getVehicleId());

        // BR: Validate odometer — cannot decrease
        validateOdometer(vehicle.getId(), dto.getOdometerReading());

        // Optional trip association
        Trip trip = null;
        if (dto.getTripId() != null) {
            trip = findTripOrThrow(dto.getTripId());
        }

        // Compute costPerLiter = cost / quantity
        BigDecimal costPerLiter = computeCostPerLiter(dto.getCost(), dto.getQuantity());

        FuelLog fuelLog = FuelLog.builder()
                .vehicle(vehicle)
                .trip(trip)
                .fuelDate(dto.getFuelDate())
                .fuelType(dto.getFuelType())
                .quantity(dto.getQuantity())
                .cost(dto.getCost())
                .vendor(dto.getVendor())
                .odometerReading(dto.getOdometerReading())
                .costPerLiter(costPerLiter)
                .remarks(dto.getRemarks())
                .build();

        FuelLog saved = fuelRepository.save(fuelLog);
        log.info("Logged FuelLog [id={}] for Vehicle [id={}]", saved.getId(), vehicle.getId());

        return toResponseDTO(saved);
    }

    // -----------------------------------------------------------------------
    // READ
    // -----------------------------------------------------------------------

    /**
     * Retrieves a fuel log by its ID.
     *
     * @param id the fuel log primary key
     * @return the fuel log as a response DTO
     * @throws ResourceNotFoundException if no log exists with the given ID
     */
    public FuelResponseDTO getFuelLogById(Long id) {
        return toResponseDTO(findFuelLogOrThrow(id));
    }

    /**
     * Retrieves all fuel logs for a given vehicle, ordered by fuel date descending.
     *
     * @param vehicleId the vehicle primary key
     * @return list of fuel logs for that vehicle
     * @throws ResourceNotFoundException if the vehicle is not found
     */
    public List<FuelResponseDTO> getFuelLogsByVehicle(Long vehicleId) {
        findVehicleOrThrow(vehicleId);
        return fuelRepository.findByVehicle_IdOrderByFuelDateDesc(vehicleId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Returns a paginated, filterable list of fuel logs.
     *
     * @param vehicleId optional vehicle ID filter
     * @param tripId    optional trip ID filter
     * @param fuelType  optional fuel type filter
     * @param fromDate  optional fuel date lower bound
     * @param toDate    optional fuel date upper bound
     * @param pageable  pagination and sorting
     * @return page of fuel log response DTOs
     */
    public Page<FuelResponseDTO> getAllFuelLogs(
            Long vehicleId,
            Long tripId,
            FuelType fuelType,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        Specification<FuelLog> spec = FuelSpecification.withFilters(vehicleId, tripId, fuelType, fromDate, toDate);
        return fuelRepository.findAll(spec, pageable).map(this::toResponseDTO);
    }

    /**
     * Returns an aggregate fuel summary for a vehicle.
     *
     * @param vehicleId the vehicle primary key
     * @return fuel summary DTO with totals and averages
     * @throws ResourceNotFoundException if the vehicle is not found
     */
    public FuelSummaryDTO getFuelSummaryByVehicle(Long vehicleId) {
        Vehicle vehicle = findVehicleOrThrow(vehicleId);

        long    count             = fuelRepository.countByVehicle_Id(vehicleId);
        BigDecimal totalQuantity  = fuelRepository.sumQuantityByVehicleId(vehicleId).orElse(BigDecimal.ZERO);
        BigDecimal totalCost      = fuelRepository.sumCostByVehicleId(vehicleId).orElse(BigDecimal.ZERO);
        Long    latestOdometer    = fuelRepository.findMaxOdometerByVehicleId(vehicleId).orElse(null);

        BigDecimal avgCostPerLiter = (totalQuantity.compareTo(BigDecimal.ZERO) > 0)
                ? totalCost.divide(totalQuantity, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return FuelSummaryDTO.builder()
                .vehicleId(vehicle.getId())
                .vehicleRegistrationNumber(vehicle.getRegistrationNumber())
                .totalFuelLogs(count)
                .totalQuantity(totalQuantity)
                .totalCost(totalCost)
                .averageCostPerLiter(avgCostPerLiter)
                .latestOdometerReading(latestOdometer)
                .build();
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------

    /**
     * Updates an existing fuel log.
     *
     * @param id  the fuel log primary key
     * @param dto the update payload
     * @return updated fuel log as response DTO
     * @throws ResourceNotFoundException if the log, vehicle, or trip is not found
     * @throws BusinessRuleException     if odometer rule is violated
     */
    @Transactional
    public FuelResponseDTO updateFuelLog(Long id, FuelRequestDTO dto) {
        FuelLog fuelLog = findFuelLogOrThrow(id);

        if (dto.getVehicleId() != null) {
            fuelLog.setVehicle(findVehicleOrThrow(dto.getVehicleId()));
        }

        if (dto.getTripId() != null) {
            fuelLog.setTrip(findTripOrThrow(dto.getTripId()));
        }

        // Re-validate odometer if it's being changed (skip if same value)
        if (dto.getOdometerReading() != null
                && !dto.getOdometerReading().equals(fuelLog.getOdometerReading())) {
            // Find max odometer excluding this record
            Optional<Long> maxOdometer = fuelRepository.findMaxOdometerByVehicleId(fuelLog.getVehicle().getId());
            // If max equals this record's current value, allow any new value >= current (we allow corrections)
            if (maxOdometer.isPresent() && !maxOdometer.get().equals(fuelLog.getOdometerReading())
                    && dto.getOdometerReading() < maxOdometer.get()) {
                throw new BusinessRuleException(
                        "Odometer reading cannot be less than the current maximum of " + maxOdometer.get() + " km.");
            }
        }

        if (dto.getFuelDate()       != null) fuelLog.setFuelDate(dto.getFuelDate());
        if (dto.getFuelType()       != null) fuelLog.setFuelType(dto.getFuelType());
        if (dto.getQuantity()       != null) fuelLog.setQuantity(dto.getQuantity());
        if (dto.getCost()           != null) fuelLog.setCost(dto.getCost());
        if (dto.getVendor()         != null) fuelLog.setVendor(dto.getVendor());
        if (dto.getOdometerReading()!= null) fuelLog.setOdometerReading(dto.getOdometerReading());
        if (dto.getRemarks()        != null) fuelLog.setRemarks(dto.getRemarks());

        // Recompute costPerLiter
        fuelLog.setCostPerLiter(computeCostPerLiter(fuelLog.getCost(), fuelLog.getQuantity()));

        FuelLog updated = fuelRepository.save(fuelLog);
        log.info("Updated FuelLog [id={}]", updated.getId());

        return toResponseDTO(updated);
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------

    /**
     * Deletes a fuel log by its ID.
     *
     * @param id the fuel log primary key
     * @throws ResourceNotFoundException if no log exists with the given ID
     */
    @Transactional
    public void deleteFuelLog(Long id) {
        FuelLog fuelLog = findFuelLogOrThrow(id);
        fuelRepository.delete(fuelLog);
        log.info("Deleted FuelLog [id={}]", id);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Vehicle findVehicleOrThrow(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));
    }

    private Trip findTripOrThrow(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));
    }

    private FuelLog findFuelLogOrThrow(Long id) {
        return fuelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FuelLog", id));
    }

    /**
     * Validates that the new odometer reading is not lower than the current maximum for the vehicle.
     *
     * @param vehicleId          the vehicle primary key
     * @param newOdometerReading the proposed new reading
     */
    private void validateOdometer(Long vehicleId, Long newOdometerReading) {
        fuelRepository.findMaxOdometerByVehicleId(vehicleId).ifPresent(maxOdometer -> {
            if (newOdometerReading < maxOdometer) {
                throw new BusinessRuleException(
                        "Odometer reading [" + newOdometerReading + " km] cannot be lower than the "
                        + "current maximum recorded reading [" + maxOdometer + " km].");
            }
        });
    }

    /**
     * Computes cost per litre = cost / quantity, rounded to 4 decimal places.
     *
     * @param cost     total fuel cost
     * @param quantity total fuel quantity
     * @return cost per litre
     */
    private BigDecimal computeCostPerLiter(BigDecimal cost, BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return cost.divide(quantity, 4, RoundingMode.HALF_UP);
    }

    /**
     * Maps a {@link FuelLog} entity to a {@link FuelResponseDTO}.
     *
     * @param fuelLog the entity
     * @return the flat response DTO
     */
    private FuelResponseDTO toResponseDTO(FuelLog fuelLog) {
        return FuelResponseDTO.builder()
                .id(fuelLog.getId())
                .vehicleId(fuelLog.getVehicle().getId())
                .vehicleRegistrationNumber(fuelLog.getVehicle().getRegistrationNumber())
                .tripId(fuelLog.getTrip() != null ? fuelLog.getTrip().getId() : null)
                .tripNumber(fuelLog.getTrip() != null ? fuelLog.getTrip().getTripNumber() : null)
                .fuelDate(fuelLog.getFuelDate())
                .fuelType(fuelLog.getFuelType())
                .quantity(fuelLog.getQuantity())
                .cost(fuelLog.getCost())
                .vendor(fuelLog.getVendor())
                .odometerReading(fuelLog.getOdometerReading())
                .costPerLiter(fuelLog.getCostPerLiter())
                .remarks(fuelLog.getRemarks())
                .createdAt(fuelLog.getCreatedAt())
                .updatedAt(fuelLog.getUpdatedAt())
                .build();
    }
}
