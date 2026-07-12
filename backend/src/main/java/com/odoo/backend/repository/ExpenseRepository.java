package com.odoo.backend.repository;

import com.odoo.backend.entity.Expense;
import com.odoo.backend.enums.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Expense} entities.
 *
 * <p>Extends {@link JpaSpecificationExecutor} for dynamic filtering via
 * {@link com.odoo.backend.specification.ExpenseSpecification}.</p>
 */
@Repository
public interface ExpenseRepository
        extends JpaRepository<Expense, Long>,
                JpaSpecificationExecutor<Expense> {

    /**
     * Finds all expenses for a vehicle, ordered by expense date descending.
     *
     * @param vehicleId the vehicle primary key
     * @return ordered list of expenses
     */
    List<Expense> findByVehicle_IdOrderByExpenseDateDesc(Long vehicleId);

    /**
     * Finds all expenses for a specific trip.
     *
     * @param tripId the trip primary key
     * @return list of expenses linked to the trip
     */
    List<Expense> findByTrip_Id(Long tripId);

    /**
     * Finds all expenses for a vehicle filtered by expense type.
     *
     * @param vehicleId   the vehicle primary key
     * @param expenseType the expense category to filter by
     * @return filtered list ordered by expense date descending
     */
    List<Expense> findByVehicle_IdAndExpenseTypeOrderByExpenseDateDesc(Long vehicleId, ExpenseType expenseType);

    /**
     * Counts all expenses for a vehicle.
     *
     * @param vehicleId the vehicle primary key
     * @return count of expense records
     */
    long countByVehicle_Id(Long vehicleId);

    /**
     * Calculates total expense amount for a vehicle.
     *
     * @param vehicleId the vehicle primary key
     * @return total amount, or empty if no records exist
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.vehicle.id = :vehicleId")
    Optional<BigDecimal> sumAmountByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Returns per-type expense totals for a vehicle as a list of Object arrays.
     * Each element: {@code [ExpenseType, BigDecimal sum]}.
     *
     * <p>Used to build the summary breakdown map in {@link com.odoo.backend.service.ExpenseService}.</p>
     *
     * @param vehicleId the vehicle primary key
     * @return list of (expenseType, total) pairs
     */
    @Query("""
            SELECT e.expenseType, SUM(e.amount)
            FROM Expense e
            WHERE e.vehicle.id = :vehicleId
            GROUP BY e.expenseType
            """)
    List<Object[]> sumAmountGroupedByTypeForVehicle(@Param("vehicleId") Long vehicleId);

    // ---- Reports Module JPQL Aggregations ----

    /**
     * Aggregates expense stats per vehicle within an optional date range.
     * Each row: [vehicleId, registrationNumber, COUNT(e), SUM(e.amount)].
     */
    @Query("""
            SELECT e.vehicle.id, e.vehicle.registrationNumber, COUNT(e), SUM(e.amount)
            FROM Expense e
            WHERE (:fromDate IS NULL OR e.expenseDate >= :fromDate)
              AND (:toDate IS NULL OR e.expenseDate <= :toDate)
            GROUP BY e.vehicle.id, e.vehicle.registrationNumber
            """)
    List<Object[]> aggregateExpensePerVehicle(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate);

    /**
     * Calculates total expense amount across the entire fleet within an optional date range.
     */
    @Query("""
            SELECT SUM(e.amount)
            FROM Expense e
            WHERE (:fromDate IS NULL OR e.expenseDate >= :fromDate)
              AND (:toDate IS NULL OR e.expenseDate <= :toDate)
            """)
    Optional<BigDecimal> sumTotalExpenseAmount(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate);

    /**
     * Aggregates expense amount and count grouped by trip.
     * Each row: [tripId, tripNumber, SUM(e.amount), COUNT(e)].
     */
    @Query("""
            SELECT e.trip.id, e.trip.tripNumber, SUM(e.amount), COUNT(e)
            FROM Expense e
            WHERE e.trip IS NOT NULL
            GROUP BY e.trip.id, e.trip.tripNumber
            """)
    List<Object[]> aggregateExpenseByTrip();
}
