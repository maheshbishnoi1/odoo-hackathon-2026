package com.odoo.backend.repository;

import com.odoo.backend.entity.MaintenanceRecord;
import com.odoo.backend.enums.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link MaintenanceRecord} entities.
 *
 * <p>Extends {@link JpaSpecificationExecutor} to support dynamic filtering
 * via {@link com.odoo.backend.specification.MaintenanceSpecification}.</p>
 */
@Repository
public interface MaintenanceRepository
        extends JpaRepository<MaintenanceRecord, Long>,
                JpaSpecificationExecutor<MaintenanceRecord> {

    /** Count maintenance records by status — used by DashboardService. */
    long countByStatus(com.odoo.backend.enums.MaintenanceStatus status);

    /**
     * Finds all maintenance records for a given vehicle, ordered by start date descending.
     *
     * @param vehicleId the vehicle primary key
     * @return list of maintenance records for that vehicle
     */
    List<MaintenanceRecord> findByVehicle_IdOrderByStartDateDesc(Long vehicleId);

    /**
     * Checks whether an active (OPEN or IN_PROGRESS) maintenance record exists for a vehicle.
     *
     * <p>Used by the service layer to enforce the business rule:
     * a vehicle may not have two concurrent active maintenance records.</p>
     *
     * @param vehicleId the vehicle primary key
     * @return {@code true} if at least one active record exists
     */
    @Query("""
            SELECT COUNT(m) > 0
            FROM MaintenanceRecord m
            WHERE m.vehicle.id = :vehicleId
              AND m.status IN ('OPEN', 'IN_PROGRESS')
            """)
    boolean existsActiveMaintenanceForVehicle(@Param("vehicleId") Long vehicleId);

    /**
     * Finds all maintenance records for a vehicle filtered by a specific status.
     *
     * @param vehicleId the vehicle primary key
     * @param status    the maintenance status to filter by
     * @return filtered list ordered by start date descending
     */
    List<MaintenanceRecord> findByVehicle_IdAndStatusOrderByStartDateDesc(
            Long vehicleId, MaintenanceStatus status);

    // ---- Reports Module JPQL Aggregations ----

    /**
     * Aggregates maintenance stats per vehicle within an optional date range.
     * Each row: [vehicleId, registrationNumber, COUNT(m), SUM(m.estimatedCost), SUM(m.actualCost)].
     */
    @Query("""
            SELECT m.vehicle.id, m.vehicle.registrationNumber, COUNT(m), SUM(m.estimatedCost), SUM(m.actualCost)
            FROM MaintenanceRecord m
            WHERE (:fromDate IS NULL OR m.startDate >= :fromDate)
              AND (:toDate IS NULL OR m.startDate <= :toDate)
            GROUP BY m.vehicle.id, m.vehicle.registrationNumber
            """)
    List<Object[]> aggregateMaintenanceCostPerVehicle(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate);

    /**
     * Calculates total actual maintenance cost across the entire fleet within an optional date range.
     */
    @Query("""
            SELECT SUM(m.actualCost)
            FROM MaintenanceRecord m
            WHERE (:fromDate IS NULL OR m.startDate >= :fromDate)
              AND (:toDate IS NULL OR m.startDate <= :toDate)
            """)
    java.util.Optional<java.math.BigDecimal> sumTotalActualCost(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate);

    /**
     * Counts active (OPEN or IN_PROGRESS) maintenance records across the fleet.
     */
    @Query("SELECT COUNT(m) FROM MaintenanceRecord m WHERE m.status IN ('OPEN', 'IN_PROGRESS')")
    long countActiveMaintenanceRecords();

    /**
     * Finds all active (OPEN or IN_PROGRESS) maintenance records for dashboard alerts.
     * Returns [vehicleId, registrationNumber, maintenanceType, status, startDate, estimatedCost].
     */
    @Query("""
            SELECT m.vehicle.id, m.vehicle.registrationNumber,
                   m.maintenanceType, m.status, m.startDate, m.estimatedCost
            FROM MaintenanceRecord m
            WHERE m.status IN ('OPEN', 'IN_PROGRESS')
            ORDER BY m.startDate ASC
            """)
    List<Object[]> findActiveMaintenanceForDashboard();

    /**
     * Aggregates monthly maintenance actual cost for analytics cost trend report.
     * Each row: [YEAR(m.startDate), MONTH(m.startDate), SUM(m.actualCost)].
     */
    @Query("""
            SELECT YEAR(m.startDate), MONTH(m.startDate), SUM(m.actualCost)
            FROM MaintenanceRecord m
            WHERE m.actualCost IS NOT NULL
            GROUP BY YEAR(m.startDate), MONTH(m.startDate)
            ORDER BY YEAR(m.startDate) ASC, MONTH(m.startDate) ASC
            """)
    List<Object[]> aggregateMonthlyMaintenanceCost();

    /**
     * Finds active maintenance records by vehicle ID (for fleet health alerts).
     */
    @Query("""
            SELECT m
            FROM MaintenanceRecord m
            WHERE m.vehicle.id = :vehicleId
              AND m.status IN ('OPEN', 'IN_PROGRESS')
            ORDER BY m.startDate DESC
            """)
    List<MaintenanceRecord> findActiveByVehicleId(@Param("vehicleId") Long vehicleId);
}
