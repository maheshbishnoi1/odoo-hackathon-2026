package com.odoo.backend.service;

import com.odoo.backend.dto.MaintenanceRequestDTO;
import com.odoo.backend.dto.MaintenanceResponseDTO;
import com.odoo.backend.entity.MaintenanceRecord;
import com.odoo.backend.entity.Vehicle;
import com.odoo.backend.enums.MaintenanceStatus;
import com.odoo.backend.enums.VehicleStatus;
import com.odoo.backend.exception.BusinessRuleException;
import com.odoo.backend.exception.ResourceNotFoundException;
import com.odoo.backend.repository.MaintenanceRepository;
import com.odoo.backend.repository.VehicleRepository;
import com.odoo.backend.specification.MaintenanceSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for the Maintenance module.
 *
 * <p>Enforces all business rules before delegating persistence to
 * {@link MaintenanceRepository}:</p>
 * <ol>
 *   <li>A {@link com.odoo.backend.enums.VehicleStatus#RETIRED} vehicle cannot receive
 *       new maintenance records.</li>
 *   <li>A vehicle already in an active maintenance (OPEN or IN_PROGRESS) cannot receive
 *       another concurrent maintenance record.</li>
 *   <li>Creating a maintenance record sets the vehicle status to
 *       {@link com.odoo.backend.enums.VehicleStatus#IN_SHOP}.</li>
 *   <li>Completing a maintenance record (status → COMPLETED) sets the vehicle status back
 *       to {@link com.odoo.backend.enums.VehicleStatus#AVAILABLE}.</li>
 *   <li>completionDate, when provided, must not be before startDate.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final VehicleRepository     vehicleRepository;

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------

    /**
     * Creates a new maintenance record and marks the vehicle as IN_SHOP.
     *
     * @param dto the maintenance creation request
     * @return the persisted record as a response DTO
     * @throws ResourceNotFoundException if the vehicle is not found
     * @throws BusinessRuleException     if the vehicle is RETIRED or already has active maintenance
     */
    @Transactional
    public MaintenanceResponseDTO createMaintenance(MaintenanceRequestDTO dto) {
        Vehicle vehicle = findVehicleOrThrow(dto.getVehicleId());

        // BR-1: Retired vehicle cannot receive maintenance
        if (vehicle.getStatus() == VehicleStatus.RETIRED) {
            throw new BusinessRuleException(
                    "Vehicle [" + vehicle.getRegistrationNumber() + "] is RETIRED and cannot receive maintenance.");
        }

        // BR-2: Only one active maintenance at a time
        if (maintenanceRepository.existsActiveMaintenanceForVehicle(vehicle.getId())) {
            throw new BusinessRuleException(
                    "Vehicle [" + vehicle.getRegistrationNumber() + "] already has an active maintenance record. "
                    + "Complete or close the existing record before creating a new one.");
        }

        // BR-3: completionDate must not be before startDate
        validateDates(dto.getStartDate(), dto.getCompletionDate());

        // Build the entity
        MaintenanceStatus status = (dto.getStatus() != null) ? dto.getStatus() : MaintenanceStatus.OPEN;

        MaintenanceRecord record = MaintenanceRecord.builder()
                .vehicle(vehicle)
                .maintenanceType(dto.getMaintenanceType())
                .description(dto.getDescription())
                .estimatedCost(dto.getEstimatedCost())
                .actualCost(dto.getActualCost())
                .startDate(dto.getStartDate())
                .completionDate(dto.getCompletionDate())
                .status(status)
                .remarks(dto.getRemarks())
                .build();

        // BR-4: Mark vehicle as IN_SHOP (unless record is being created already completed)
        if (status != MaintenanceStatus.COMPLETED) {
            vehicle.setStatus(VehicleStatus.IN_SHOP);
            vehicleRepository.save(vehicle);
        }

        // BR-5: If created as COMPLETED, restore vehicle to AVAILABLE
        if (status == MaintenanceStatus.COMPLETED) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        MaintenanceRecord saved = maintenanceRepository.save(record);
        log.info("Created MaintenanceRecord [id={}] for Vehicle [id={}]", saved.getId(), vehicle.getId());

        return toResponseDTO(saved);
    }

    // -----------------------------------------------------------------------
    // READ
    // -----------------------------------------------------------------------

    /**
     * Retrieves a maintenance record by its ID.
     *
     * @param id the maintenance record primary key
     * @return the record as a response DTO
     * @throws ResourceNotFoundException if no record exists with the given ID
     */
    public MaintenanceResponseDTO getMaintenanceById(Long id) {
        MaintenanceRecord record = findRecordOrThrow(id);
        return toResponseDTO(record);
    }

    /**
     * Retrieves all maintenance records for a given vehicle, ordered by start date descending.
     *
     * @param vehicleId the vehicle primary key
     * @return list of maintenance records for that vehicle
     * @throws ResourceNotFoundException if the vehicle is not found
     */
    public List<MaintenanceResponseDTO> getMaintenanceByVehicle(Long vehicleId) {
        findVehicleOrThrow(vehicleId); // validate vehicle exists
        return maintenanceRepository.findByVehicle_IdOrderByStartDateDesc(vehicleId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Returns a paginated, filterable list of maintenance records.
     *
     * @param vehicleId       optional vehicle ID filter
     * @param status          optional status filter
     * @param maintenanceType optional type filter
     * @param fromDate        optional start-date lower bound
     * @param toDate          optional start-date upper bound
     * @param pageable        pagination and sorting
     * @return page of maintenance response DTOs
     */
    public Page<MaintenanceResponseDTO> getAllMaintenance(
            Long vehicleId,
            MaintenanceStatus status,
            com.odoo.backend.enums.MaintenanceType maintenanceType,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        Specification<MaintenanceRecord> spec =
                MaintenanceSpecification.withFilters(vehicleId, status, maintenanceType, fromDate, toDate);

        return maintenanceRepository.findAll(spec, pageable)
                .map(this::toResponseDTO);
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------

    /**
     * Updates an existing maintenance record.
     *
     * <p>If the status is changed to {@link MaintenanceStatus#COMPLETED}:</p>
     * <ul>
     *   <li>completionDate must be provided.</li>
     *   <li>completionDate must be on or after startDate.</li>
     *   <li>Vehicle status is set back to {@link VehicleStatus#AVAILABLE}.</li>
     * </ul>
     *
     * @param id  the maintenance record ID
     * @param dto the update payload
     * @return the updated record as a response DTO
     * @throws ResourceNotFoundException if the record or vehicle is not found
     * @throws BusinessRuleException     if business rules are violated
     */
    @Transactional
    public MaintenanceResponseDTO updateMaintenance(Long id, MaintenanceRequestDTO dto) {
        MaintenanceRecord record = findRecordOrThrow(id);

        // If vehicleId changed, validate the new vehicle too
        if (dto.getVehicleId() != null && !dto.getVehicleId().equals(record.getVehicle().getId())) {
            Vehicle newVehicle = findVehicleOrThrow(dto.getVehicleId());
            if (newVehicle.getStatus() == VehicleStatus.RETIRED) {
                throw new BusinessRuleException(
                        "Cannot reassign maintenance to RETIRED vehicle [" + newVehicle.getRegistrationNumber() + "].");
            }
            record.setVehicle(newVehicle);
        }

        // Track if we're transitioning to COMPLETED
        boolean completingNow = (dto.getStatus() == MaintenanceStatus.COMPLETED)
                && (record.getStatus() != MaintenanceStatus.COMPLETED);

        if (completingNow && dto.getCompletionDate() == null) {
            throw new BusinessRuleException("completionDate is required when marking maintenance as COMPLETED.");
        }

        if (dto.getMaintenanceType()  != null) record.setMaintenanceType(dto.getMaintenanceType());
        if (dto.getDescription()      != null) record.setDescription(dto.getDescription());
        if (dto.getEstimatedCost()    != null) record.setEstimatedCost(dto.getEstimatedCost());
        if (dto.getActualCost()       != null) record.setActualCost(dto.getActualCost());
        if (dto.getStartDate()        != null) record.setStartDate(dto.getStartDate());
        if (dto.getCompletionDate()   != null) record.setCompletionDate(dto.getCompletionDate());
        if (dto.getStatus()           != null) record.setStatus(dto.getStatus());
        if (dto.getRemarks()          != null) record.setRemarks(dto.getRemarks());

        // Validate date ordering after applying updates
        validateDates(record.getStartDate(), record.getCompletionDate());

        // BR: On completion, restore vehicle status
        if (completingNow) {
            Vehicle vehicle = record.getVehicle();
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
            log.info("Vehicle [id={}] status restored to AVAILABLE after maintenance completion.", vehicle.getId());
        }

        MaintenanceRecord updated = maintenanceRepository.save(record);
        log.info("Updated MaintenanceRecord [id={}]", updated.getId());

        return toResponseDTO(updated);
    }

    /**
     * Convenience endpoint to update only the status of a maintenance record.
     *
     * @param id     the maintenance record ID
     * @param status the new status
     * @return the updated record as a response DTO
     */
    @Transactional
    public MaintenanceResponseDTO updateStatus(Long id, MaintenanceStatus status) {
        MaintenanceRequestDTO dto = new MaintenanceRequestDTO();
        dto.setStatus(status);
        if (status == MaintenanceStatus.COMPLETED) {
            dto.setCompletionDate(LocalDate.now());
        }
        return updateMaintenance(id, dto);
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------

    /**
     * Deletes a maintenance record.
     *
     * <p>If the record is still OPEN or IN_PROGRESS, the vehicle status is restored
     * to AVAILABLE before deletion.</p>
     *
     * @param id the maintenance record primary key
     * @throws ResourceNotFoundException if no record exists with the given ID
     */
    @Transactional
    public void deleteMaintenance(Long id) {
        MaintenanceRecord record = findRecordOrThrow(id);

        // Restore vehicle status if deleting an active record
        if (record.getStatus() != MaintenanceStatus.COMPLETED) {
            Vehicle vehicle = record.getVehicle();
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
            log.info("Vehicle [id={}] status restored to AVAILABLE on maintenance record deletion.", vehicle.getId());
        }

        maintenanceRepository.delete(record);
        log.info("Deleted MaintenanceRecord [id={}]", id);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Vehicle findVehicleOrThrow(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));
    }

    private MaintenanceRecord findRecordOrThrow(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", id));
    }

    private void validateDates(LocalDate startDate, LocalDate completionDate) {
        if (startDate != null && completionDate != null && completionDate.isBefore(startDate)) {
            throw new BusinessRuleException("Completion date cannot be before the start date.");
        }
    }

    /**
     * Maps a {@link MaintenanceRecord} entity to a {@link MaintenanceResponseDTO}.
     *
     * @param record the entity
     * @return the flat response DTO
     */
    private MaintenanceResponseDTO toResponseDTO(MaintenanceRecord record) {
        return MaintenanceResponseDTO.builder()
                .id(record.getId())
                .vehicleId(record.getVehicle().getId())
                .vehicleRegistrationNumber(record.getVehicle().getRegistrationNumber())
                .vehicleStatus(record.getVehicle().getStatus())
                .maintenanceType(record.getMaintenanceType())
                .description(record.getDescription())
                .estimatedCost(record.getEstimatedCost())
                .actualCost(record.getActualCost())
                .startDate(record.getStartDate())
                .completionDate(record.getCompletionDate())
                .status(record.getStatus())
                .remarks(record.getRemarks())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
