package com.odoo.backend.repository;

import com.odoo.backend.entity.FuelLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link FuelLog} entities.
 *
 * <p>Extends {@link JpaSpecificationExecutor} for dynamic filtering via
 * {@link com.odoo.backend.specification.FuelSpecification}.</p>
 */
@Repository
public interface FuelRepository
        extends JpaRepository<FuelLog, Long>,
                JpaSpecificationExecutor<FuelLog> {

    /**
     * Finds all fuel logs for a vehicle, ordered by fuel date descending.
     *
     * @param vehicleId the vehicle primary key
     * @return ordered list of fuel logs
     */
    List<FuelLog> findByVehicle_IdOrderByFuelDateDesc(Long vehicleId);

    /**
     * Finds all fuel logs for a specific trip.
     *
     * @param tripId the trip primary key
     * @return list of fuel logs linked to the trip
     */
    List<FuelLog> findByTrip_Id(Long tripId);

    /**
     * Returns the maximum odometer reading recorded for a vehicle.
     * Used by the service layer to validate that new readings do not decrease.
     *
     * @param vehicleId the vehicle primary key
     * @return the maximum odometer reading, or {@link Optional#empty()} if no logs exist
     */
    @Query("SELECT MAX(f.odometerReading) FROM FuelLog f WHERE f.vehicle.id = :vehicleId")
    Optional<Long> findMaxOdometerByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Calculates the total quantity of fuel purchased for a vehicle.
     *
     * @param vehicleId the vehicle primary key
     * @return total fuel quantity (litres), or {@code null} if no logs exist
     */
    @Query("SELECT SUM(f.quantity) FROM FuelLog f WHERE f.vehicle.id = :vehicleId")
    Optional<BigDecimal> sumQuantityByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Calculates the total fuel cost for a vehicle.
     *
     * @param vehicleId the vehicle primary key
     * @return total fuel cost, or {@code null} if no logs exist
     */
    @Query("SELECT SUM(f.cost) FROM FuelLog f WHERE f.vehicle.id = :vehicleId")
    Optional<BigDecimal> sumCostByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Counts all fuel logs for a vehicle.
     *
     * @param vehicleId the vehicle primary key
     * @return count of fuel log entries
     */
    long countByVehicle_Id(Long vehicleId);

    // ---- Reports Module JPQL Aggregations ----

    /**
     * Aggregates fuel stats per vehicle within an optional date range.
     * Each row: [vehicleId, registrationNumber, COUNT(f), SUM(f.quantity), SUM(f.cost), AVG(f.costPerLiter)].
     */
    @Query("""
            SELECT f.vehicle.id, f.vehicle.registrationNumber, COUNT(f), SUM(f.quantity), SUM(f.cost), AVG(f.costPerLiter)
            FROM FuelLog f
            WHERE (:fromDate IS NULL OR f.fuelDate >= :fromDate)
              AND (:toDate IS NULL OR f.fuelDate <= :toDate)
            GROUP BY f.vehicle.id, f.vehicle.registrationNumber
            """)
    List<Object[]> aggregateFuelCostPerVehicle(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate);

    /**
     * Calculates total fuel cost across the entire fleet within an optional date range.
     */
    @Query("""
            SELECT SUM(f.cost)
            FROM FuelLog f
            WHERE (:fromDate IS NULL OR f.fuelDate >= :fromDate)
              AND (:toDate IS NULL OR f.fuelDate <= :toDate)
            """)
    Optional<BigDecimal> sumTotalFuelCost(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate);

    /**
     * Calculates total fuel quantity across the entire fleet within an optional date range.
     */
    @Query("""
            SELECT SUM(f.quantity)
            FROM FuelLog f
            WHERE (:fromDate IS NULL OR f.fuelDate >= :fromDate)
              AND (:toDate IS NULL OR f.fuelDate <= :toDate)
            """)
    Optional<BigDecimal> sumTotalFuelQuantity(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate);

    /**
     * Aggregates fuel cost and quantity grouped by trip.
     * Each row: [tripId, tripNumber, SUM(f.cost), SUM(f.quantity)].
     */
    @Query("""
            SELECT f.trip.id, f.trip.tripNumber, SUM(f.cost), SUM(f.quantity)
            FROM FuelLog f
            WHERE f.trip IS NOT NULL
            GROUP BY f.trip.id, f.trip.tripNumber
            """)
    List<Object[]> aggregateFuelByTrip();

    /**
     * Aggregates monthly fuel cost for analytics cost trend report.
     * Each row: [YEAR(f.fuelDate), MONTH(f.fuelDate), SUM(f.cost)].
     */
    @Query("""
            SELECT YEAR(f.fuelDate), MONTH(f.fuelDate), SUM(f.cost)
            FROM FuelLog f
            GROUP BY YEAR(f.fuelDate), MONTH(f.fuelDate)
            ORDER BY YEAR(f.fuelDate) ASC, MONTH(f.fuelDate) ASC
            """)
    List<Object[]> aggregateMonthlyFuelCost();

    /**
     * Aggregates fuel cost grouped by trip's driver for driver performance analytics.
     * Each row: [driverId, SUM(f.cost)].
     */
    @Query("""
            SELECT f.trip.driver.id, SUM(f.cost)
            FROM FuelLog f
            WHERE f.trip IS NOT NULL
            GROUP BY f.trip.driver.id
            """)
    List<Object[]> aggregateFuelCostByDriver();
}
