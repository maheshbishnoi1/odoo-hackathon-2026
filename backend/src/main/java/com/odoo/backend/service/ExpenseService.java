package com.odoo.backend.service;

import com.odoo.backend.dto.ExpenseRequestDTO;
import com.odoo.backend.dto.ExpenseResponseDTO;
import com.odoo.backend.dto.ExpenseSummaryDTO;
import com.odoo.backend.entity.Expense;
import com.odoo.backend.entity.Trip;
import com.odoo.backend.entity.Vehicle;
import com.odoo.backend.enums.ExpenseType;
import com.odoo.backend.enums.PaymentMode;
import com.odoo.backend.exception.BusinessRuleException;
import com.odoo.backend.exception.ResourceNotFoundException;
import com.odoo.backend.repository.ExpenseRepository;
import com.odoo.backend.repository.TripRepository;
import com.odoo.backend.repository.VehicleRepository;
import com.odoo.backend.specification.ExpenseSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for the Expense module of TransitOps.
 *
 * <p>Business rules enforced:</p>
 * <ol>
 *   <li>Amount must be greater than zero (enforced by DTO validation + service check).</li>
 *   <li>Expense date cannot be in the future (enforced by {@code @PastOrPresent} on DTO).</li>
 *   <li>Trip association is optional; when provided the trip must exist.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository    tripRepository;

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------

    /**
     * Creates a new expense record for a vehicle.
     *
     * @param dto the expense creation request
     * @return the persisted expense as a response DTO
     * @throws ResourceNotFoundException if the vehicle or trip is not found
     * @throws BusinessRuleException     if amount is zero or expense date is in the future
     */
    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto) {
        Vehicle vehicle = findVehicleOrThrow(dto.getVehicleId());

        // BR: Additional service-level guard (backup to @PastOrPresent)
        if (dto.getExpenseDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Expense date cannot be in the future.");
        }

        // BR: Amount greater than zero (backup to @DecimalMin)
        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Expense amount must be greater than zero.");
        }

        // Optional trip
        Trip trip = null;
        if (dto.getTripId() != null) {
            trip = findTripOrThrow(dto.getTripId());
        }

        Expense expense = Expense.builder()
                .vehicle(vehicle)
                .trip(trip)
                .expenseType(dto.getExpenseType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .expenseDate(dto.getExpenseDate())
                .paymentMode(dto.getPaymentMode())
                .remarks(dto.getRemarks())
                .build();

        Expense saved = expenseRepository.save(expense);
        log.info("Created Expense [id={}] for Vehicle [id={}]", saved.getId(), vehicle.getId());

        return toResponseDTO(saved);
    }

    // -----------------------------------------------------------------------
    // READ
    // -----------------------------------------------------------------------

    /**
     * Retrieves an expense record by its ID.
     *
     * @param id the expense primary key
     * @return the expense as a response DTO
     * @throws ResourceNotFoundException if no record exists with the given ID
     */
    public ExpenseResponseDTO getExpenseById(Long id) {
        return toResponseDTO(findExpenseOrThrow(id));
    }

    /**
     * Retrieves all expenses for a given vehicle, ordered by expense date descending.
     *
     * @param vehicleId the vehicle primary key
     * @return list of expense DTOs for that vehicle
     * @throws ResourceNotFoundException if the vehicle is not found
     */
    public List<ExpenseResponseDTO> getExpensesByVehicle(Long vehicleId) {
        findVehicleOrThrow(vehicleId);
        return expenseRepository.findByVehicle_IdOrderByExpenseDateDesc(vehicleId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Returns a paginated, filterable list of expenses.
     *
     * @param vehicleId   optional vehicle ID filter
     * @param tripId      optional trip ID filter
     * @param expenseType optional expense type filter
     * @param paymentMode optional payment mode filter
     * @param fromDate    optional expense date lower bound
     * @param toDate      optional expense date upper bound
     * @param pageable    pagination and sorting
     * @return page of expense response DTOs
     */
    public Page<ExpenseResponseDTO> getAllExpenses(
            Long vehicleId,
            Long tripId,
            ExpenseType expenseType,
            PaymentMode paymentMode,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        Specification<Expense> spec = ExpenseSpecification.withFilters(
                vehicleId, tripId, expenseType, paymentMode, fromDate, toDate);

        return expenseRepository.findAll(spec, pageable).map(this::toResponseDTO);
    }

    /**
     * Returns an aggregate expense summary for a vehicle, grouped by expense type.
     *
     * @param vehicleId the vehicle primary key
     * @return expense summary DTO
     * @throws ResourceNotFoundException if the vehicle is not found
     */
    public ExpenseSummaryDTO getExpenseSummaryByVehicle(Long vehicleId) {
        Vehicle vehicle = findVehicleOrThrow(vehicleId);

        long       totalCount  = expenseRepository.countByVehicle_Id(vehicleId);
        BigDecimal totalAmount = expenseRepository.sumAmountByVehicleId(vehicleId).orElse(BigDecimal.ZERO);

        // Build breakdown map from JPQL GROUP BY result
        List<Object[]> rows = expenseRepository.sumAmountGroupedByTypeForVehicle(vehicleId);
        Map<ExpenseType, BigDecimal> breakdown = new EnumMap<>(ExpenseType.class);
        for (Object[] row : rows) {
            ExpenseType type  = (ExpenseType) row[0];
            BigDecimal  total = (BigDecimal)  row[1];
            breakdown.put(type, total);
        }

        return ExpenseSummaryDTO.builder()
                .vehicleId(vehicle.getId())
                .vehicleRegistrationNumber(vehicle.getRegistrationNumber())
                .totalExpenses(totalCount)
                .totalAmount(totalAmount)
                .amountByType(breakdown)
                .build();
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------

    /**
     * Updates an existing expense record.
     *
     * @param id  the expense primary key
     * @param dto the update payload
     * @return the updated expense as a response DTO
     * @throws ResourceNotFoundException if the expense, vehicle, or trip is not found
     * @throws BusinessRuleException     if business rules are violated
     */
    @Transactional
    public ExpenseResponseDTO updateExpense(Long id, ExpenseRequestDTO dto) {
        Expense expense = findExpenseOrThrow(id);

        if (dto.getVehicleId() != null) {
            expense.setVehicle(findVehicleOrThrow(dto.getVehicleId()));
        }

        if (dto.getTripId() != null) {
            expense.setTrip(findTripOrThrow(dto.getTripId()));
        }

        if (dto.getExpenseDate() != null && dto.getExpenseDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Expense date cannot be in the future.");
        }

        if (dto.getAmount() != null && dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Expense amount must be greater than zero.");
        }

        if (dto.getExpenseType()  != null) expense.setExpenseType(dto.getExpenseType());
        if (dto.getTitle()        != null) expense.setTitle(dto.getTitle());
        if (dto.getDescription()  != null) expense.setDescription(dto.getDescription());
        if (dto.getAmount()       != null) expense.setAmount(dto.getAmount());
        if (dto.getExpenseDate()  != null) expense.setExpenseDate(dto.getExpenseDate());
        if (dto.getPaymentMode()  != null) expense.setPaymentMode(dto.getPaymentMode());
        if (dto.getRemarks()      != null) expense.setRemarks(dto.getRemarks());

        Expense updated = expenseRepository.save(expense);
        log.info("Updated Expense [id={}]", updated.getId());

        return toResponseDTO(updated);
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------

    /**
     * Deletes an expense record.
     *
     * @param id the expense primary key
     * @throws ResourceNotFoundException if no record exists with the given ID
     */
    @Transactional
    public void deleteExpense(Long id) {
        Expense expense = findExpenseOrThrow(id);
        expenseRepository.delete(expense);
        log.info("Deleted Expense [id={}]", id);
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

    private Expense findExpenseOrThrow(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
    }

    /**
     * Maps an {@link Expense} entity to an {@link ExpenseResponseDTO}.
     *
     * @param expense the entity
     * @return the flat response DTO
     */
    private ExpenseResponseDTO toResponseDTO(Expense expense) {
        return ExpenseResponseDTO.builder()
                .id(expense.getId())
                .vehicleId(expense.getVehicle().getId())
                .vehicleRegistrationNumber(expense.getVehicle().getRegistrationNumber())
                .tripId(expense.getTrip() != null ? expense.getTrip().getId() : null)
                .tripNumber(expense.getTrip() != null ? expense.getTrip().getTripNumber() : null)
                .expenseType(expense.getExpenseType())
                .title(expense.getTitle())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .paymentMode(expense.getPaymentMode())
                .remarks(expense.getRemarks())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
