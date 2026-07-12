package com.odoo.backend.service;

import com.odoo.backend.dto.report.*;
import com.odoo.backend.entity.Trip;
import com.odoo.backend.entity.Vehicle;
import com.odoo.backend.enums.VehicleStatus;
import com.odoo.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Service layer for the Reports Module of TransitOps.
 *
 * <p>Executes optimized JPQL aggregation queries at the repository level
 * to compute fleet analytical metrics without loading heavy entity graphs into memory.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final VehicleRepository     vehicleRepository;
    private final FuelRepository        fuelRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final ExpenseRepository     expenseRepository;
    private final TripRepository        tripRepository;

    // -----------------------------------------------------------------------
    // 1. Dashboard KPI Summary
    // -----------------------------------------------------------------------

    /**
     * Generates executive KPI summary metrics across the entire fleet.
     *
     * @return {@link DashboardKpiDTO} containing key operational metrics
     */
    public DashboardKpiDTO getDashboardKpi() {
        log.debug("Generating Dashboard KPI summary report");

        long totalFleetSize = vehicleRepository.count();
        List<Object[]> statusCounts = vehicleRepository.countVehiclesGroupedByStatus();

        long activeVehicles = 0;
        long vehiclesInShop = 0;
        long vehiclesOnTrip = 0;

        for (Object[] row : statusCounts) {
            VehicleStatus status = (VehicleStatus) row[0];
            long count = ((Number) row[1]).longValue();

            if (status != VehicleStatus.RETIRED) {
                activeVehicles += count;
            }
            if (status == VehicleStatus.IN_SHOP || status == VehicleStatus.UNDER_REPAIR) {
                vehiclesInShop += count;
            }
            if (status == VehicleStatus.ON_TRIP) {
                vehiclesOnTrip += count;
            }
        }

        long openMaintenanceCount = maintenanceRepository.countActiveMaintenanceRecords();

        BigDecimal totalFuelCost = fuelRepository.sumTotalFuelCost(null, null)
                .orElse(BigDecimal.ZERO);
        BigDecimal totalMaintenanceCost = maintenanceRepository.sumTotalActualCost(null, null)
                .orElse(BigDecimal.ZERO);
        BigDecimal totalExpenseCost = expenseRepository.sumTotalExpenseAmount(null, null)
                .orElse(BigDecimal.ZERO);

        BigDecimal totalOperationalCost = totalFuelCost
                .add(totalMaintenanceCost)
                .add(totalExpenseCost);

        return DashboardKpiDTO.builder()
                .totalFleetSize(totalFleetSize)
                .activeVehicles(activeVehicles)
                .vehiclesInShop(vehiclesInShop)
                .vehiclesOnTrip(vehiclesOnTrip)
                .openMaintenanceCount(openMaintenanceCount)
                .totalFuelCost(totalFuelCost)
                .totalMaintenanceCost(totalMaintenanceCost)
                .totalExpenseCost(totalExpenseCost)
                .totalOperationalCost(totalOperationalCost)
                .build();
    }

    // -----------------------------------------------------------------------
    // 2. Fleet Utilization Report
    // -----------------------------------------------------------------------

    /**
     * Generates a fleet utilization report with status distribution.
     *
     * @return {@link FleetUtilizationReportDTO}
     */
    public FleetUtilizationReportDTO getFleetUtilization() {
        log.debug("Generating Fleet Utilization report");

        List<Object[]> rows = vehicleRepository.countVehiclesGroupedByStatus();
        Map<VehicleStatus, Long> breakdown = new EnumMap<>(VehicleStatus.class);

        // Initialize all statuses to 0
        for (VehicleStatus status : VehicleStatus.values()) {
            breakdown.put(status, 0L);
        }

        long totalVehicles = 0;
        long activeVehicles = 0;
        long vehiclesOnTrip = 0;
        long vehiclesAvailable = 0;

        for (Object[] row : rows) {
            VehicleStatus status = (VehicleStatus) row[0];
            long count = ((Number) row[1]).longValue();
            breakdown.put(status, count);
            totalVehicles += count;

            if (status != VehicleStatus.RETIRED) {
                activeVehicles += count;
            }
            if (status == VehicleStatus.ON_TRIP) {
                vehiclesOnTrip += count;
            }
            if (status == VehicleStatus.AVAILABLE) {
                vehiclesAvailable += count;
            }
        }

        BigDecimal tripUtilizationPct = BigDecimal.ZERO;
        BigDecimal availabilityPct = BigDecimal.ZERO;

        if (activeVehicles > 0) {
            tripUtilizationPct = BigDecimal.valueOf(vehiclesOnTrip)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(activeVehicles), 2, RoundingMode.HALF_UP);

            availabilityPct = BigDecimal.valueOf(vehiclesAvailable)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(activeVehicles), 2, RoundingMode.HALF_UP);
        }

        return FleetUtilizationReportDTO.builder()
                .totalVehicles(totalVehicles)
                .activeVehicles(activeVehicles)
                .vehiclesOnTrip(vehiclesOnTrip)
                .tripUtilizationPercentage(tripUtilizationPct)
                .availabilityPercentage(availabilityPct)
                .statusBreakdown(breakdown)
                .build();
    }

    // -----------------------------------------------------------------------
    // 3. Vehicle-wise Fuel Cost Report
    // -----------------------------------------------------------------------

    /**
     * Generates vehicle-wise fuel cost aggregations within an optional date range.
     *
     * @param fromDate optional lower bound (inclusive)
     * @param toDate   optional upper bound (inclusive)
     * @return {@link VehicleFuelCostReportDTO}
     */
    public VehicleFuelCostReportDTO getVehicleFuelCostReport(LocalDate fromDate, LocalDate toDate) {
        log.debug("Generating Vehicle Fuel Cost report [fromDate={}, toDate={}]", fromDate, toDate);

        List<Object[]> rows = fuelRepository.aggregateFuelCostPerVehicle(fromDate, toDate);

        List<VehicleFuelCostReportDTO.VehicleFuelCostItem> items = new ArrayList<>();
        BigDecimal fleetTotalCost = BigDecimal.ZERO;
        BigDecimal fleetTotalQty = BigDecimal.ZERO;

        for (Object[] row : rows) {
            Long vehicleId = (Long) row[0];
            String regNum = (String) row[1];
            long count = ((Number) row[2]).longValue();
            BigDecimal totalQty = toBigDecimal(row[3]);
            BigDecimal totalCost = toBigDecimal(row[4]);
            BigDecimal avgCostPerLiter = toBigDecimal(row[5]).setScale(2, RoundingMode.HALF_UP);

            fleetTotalCost = fleetTotalCost.add(totalCost);
            fleetTotalQty = fleetTotalQty.add(totalQty);

            items.add(VehicleFuelCostReportDTO.VehicleFuelCostItem.builder()
                    .vehicleId(vehicleId)
                    .vehicleRegistrationNumber(regNum)
                    .fillUpCount(count)
                    .totalQuantity(totalQty)
                    .totalCost(totalCost)
                    .averageCostPerLiter(avgCostPerLiter)
                    .build());
        }

        return VehicleFuelCostReportDTO.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalFleetFuelCost(fleetTotalCost)
                .totalFleetFuelQuantity(fleetTotalQty)
                .vehicleFuelCosts(items)
                .build();
    }

    // -----------------------------------------------------------------------
    // 4. Vehicle-wise Maintenance Cost Report
    // -----------------------------------------------------------------------

    /**
     * Generates vehicle-wise maintenance cost aggregations within an optional date range.
     *
     * @param fromDate optional lower bound (inclusive)
     * @param toDate   optional upper bound (inclusive)
     * @return {@link VehicleMaintenanceCostReportDTO}
     */
    public VehicleMaintenanceCostReportDTO getVehicleMaintenanceCostReport(LocalDate fromDate, LocalDate toDate) {
        log.debug("Generating Vehicle Maintenance Cost report [fromDate={}, toDate={}]", fromDate, toDate);

        List<Object[]> rows = maintenanceRepository.aggregateMaintenanceCostPerVehicle(fromDate, toDate);

        List<VehicleMaintenanceCostReportDTO.VehicleMaintenanceCostItem> items = new ArrayList<>();
        BigDecimal fleetEstimatedCost = BigDecimal.ZERO;
        BigDecimal fleetActualCost = BigDecimal.ZERO;

        for (Object[] row : rows) {
            Long vehicleId = (Long) row[0];
            String regNum = (String) row[1];
            long count = ((Number) row[2]).longValue();
            BigDecimal estCost = toBigDecimal(row[3]);
            BigDecimal actCost = toBigDecimal(row[4]);

            fleetEstimatedCost = fleetEstimatedCost.add(estCost);
            fleetActualCost = fleetActualCost.add(actCost);

            items.add(VehicleMaintenanceCostReportDTO.VehicleMaintenanceCostItem.builder()
                    .vehicleId(vehicleId)
                    .vehicleRegistrationNumber(regNum)
                    .maintenanceRecordCount(count)
                    .totalEstimatedCost(estCost)
                    .totalActualCost(actCost)
                    .build());
        }

        return VehicleMaintenanceCostReportDTO.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalFleetEstimatedCost(fleetEstimatedCost)
                .totalFleetMaintenanceCost(fleetActualCost)
                .vehicleMaintenanceCosts(items)
                .build();
    }

    // -----------------------------------------------------------------------
    // 5. Vehicle Expense Summary Report
    // -----------------------------------------------------------------------

    /**
     * Generates vehicle-wise general expense aggregations within an optional date range.
     *
     * @param fromDate optional lower bound (inclusive)
     * @param toDate   optional upper bound (inclusive)
     * @return {@link VehicleExpenseSummaryReportDTO}
     */
    public VehicleExpenseSummaryReportDTO getVehicleExpenseSummaryReport(LocalDate fromDate, LocalDate toDate) {
        log.debug("Generating Vehicle Expense Summary report [fromDate={}, toDate={}]", fromDate, toDate);

        List<Object[]> rows = expenseRepository.aggregateExpensePerVehicle(fromDate, toDate);

        List<VehicleExpenseSummaryReportDTO.VehicleExpenseItem> items = new ArrayList<>();
        BigDecimal fleetTotalExpense = BigDecimal.ZERO;

        for (Object[] row : rows) {
            Long vehicleId = (Long) row[0];
            String regNum = (String) row[1];
            long count = ((Number) row[2]).longValue();
            BigDecimal totalAmount = toBigDecimal(row[3]);

            fleetTotalExpense = fleetTotalExpense.add(totalAmount);

            items.add(VehicleExpenseSummaryReportDTO.VehicleExpenseItem.builder()
                    .vehicleId(vehicleId)
                    .vehicleRegistrationNumber(regNum)
                    .expenseCount(count)
                    .totalExpenseAmount(totalAmount)
                    .build());
        }

        return VehicleExpenseSummaryReportDTO.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalFleetExpenseAmount(fleetTotalExpense)
                .vehicleExpenses(items)
                .build();
    }

    // -----------------------------------------------------------------------
    // 6. Combined Operational Cost Report
    // -----------------------------------------------------------------------

    /**
     * Generates a combined operational cost report aggregating Fuel, Maintenance, and Expenses.
     *
     * @param fromDate optional lower bound (inclusive)
     * @param toDate   optional upper bound (inclusive)
     * @return {@link OperationalCostReportDTO}
     */
    public OperationalCostReportDTO getOperationalCostReport(LocalDate fromDate, LocalDate toDate) {
        log.debug("Generating Operational Cost report [fromDate={}, toDate={}]", fromDate, toDate);

        List<Vehicle> vehicles = vehicleRepository.findAll();

        // Build lookup maps by vehicle ID
        Map<Long, BigDecimal> fuelMap = new HashMap<>();
        for (Object[] r : fuelRepository.aggregateFuelCostPerVehicle(fromDate, toDate)) {
            fuelMap.put((Long) r[0], toBigDecimal(r[4]));
        }

        Map<Long, BigDecimal> maintMap = new HashMap<>();
        for (Object[] r : maintenanceRepository.aggregateMaintenanceCostPerVehicle(fromDate, toDate)) {
            maintMap.put((Long) r[0], toBigDecimal(r[4]));
        }

        Map<Long, BigDecimal> expenseMap = new HashMap<>();
        for (Object[] r : expenseRepository.aggregateExpensePerVehicle(fromDate, toDate)) {
            expenseMap.put((Long) r[0], toBigDecimal(r[3]));
        }

        List<OperationalCostReportDTO.VehicleOperationalCostItem> items = new ArrayList<>();
        BigDecimal totalFuel = BigDecimal.ZERO;
        BigDecimal totalMaint = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Vehicle v : vehicles) {
            BigDecimal fCost = fuelMap.getOrDefault(v.getId(), BigDecimal.ZERO);
            BigDecimal mCost = maintMap.getOrDefault(v.getId(), BigDecimal.ZERO);
            BigDecimal eCost = expenseMap.getOrDefault(v.getId(), BigDecimal.ZERO);
            BigDecimal vTotal = fCost.add(mCost).add(eCost);

            totalFuel = totalFuel.add(fCost);
            totalMaint = totalMaint.add(mCost);
            totalExpense = totalExpense.add(eCost);

            items.add(OperationalCostReportDTO.VehicleOperationalCostItem.builder()
                    .vehicleId(v.getId())
                    .vehicleRegistrationNumber(v.getRegistrationNumber())
                    .fuelCost(fCost)
                    .maintenanceCost(mCost)
                    .expenseCost(eCost)
                    .totalOperationalCost(vTotal)
                    .build());
        }

        BigDecimal grandTotal = totalFuel.add(totalMaint).add(totalExpense);

        return OperationalCostReportDTO.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalFuelCost(totalFuel)
                .totalMaintenanceCost(totalMaint)
                .totalExpenseCost(totalExpense)
                .grandTotalOperationalCost(grandTotal)
                .vehicleOperationalCosts(items)
                .build();
    }

    // -----------------------------------------------------------------------
    // 7. Trip Statistics Report
    // -----------------------------------------------------------------------

    /**
     * Generates trip-level financial statistics combining fuel logs and expenses.
     *
     * @return {@link TripStatisticsReportDTO}
     */
    public TripStatisticsReportDTO getTripStatisticsReport() {
        log.debug("Generating Trip Statistics report");

        List<Trip> trips = tripRepository.findAllByOrderByStartTimeDesc();

        Map<Long, BigDecimal> fuelMap = new HashMap<>();
        for (Object[] r : fuelRepository.aggregateFuelByTrip()) {
            fuelMap.put((Long) r[0], toBigDecimal(r[2]));
        }

        Map<Long, BigDecimal> expenseMap = new HashMap<>();
        for (Object[] r : expenseRepository.aggregateExpenseByTrip()) {
            expenseMap.put((Long) r[0], toBigDecimal(r[2]));
        }

        List<TripStatisticsReportDTO.TripFinancialItem> items = new ArrayList<>();
        BigDecimal totalFuel = BigDecimal.ZERO;
        BigDecimal totalExp = BigDecimal.ZERO;

        for (Trip trip : trips) {
            BigDecimal fCost = fuelMap.getOrDefault(trip.getId(), BigDecimal.ZERO);
            BigDecimal eCost = expenseMap.getOrDefault(trip.getId(), BigDecimal.ZERO);
            BigDecimal tripTotal = fCost.add(eCost);

            totalFuel = totalFuel.add(fCost);
            totalExp = totalExp.add(eCost);

            items.add(TripStatisticsReportDTO.TripFinancialItem.builder()
                    .tripId(trip.getId())
                    .tripNumber(trip.getTripNumber())
                    .fuelCost(fCost)
                    .expenseCost(eCost)
                    .totalTripCost(tripTotal)
                    .build());
        }

        BigDecimal totalCombined = totalFuel.add(totalExp);
        BigDecimal averageTripCost = BigDecimal.ZERO;

        if (!trips.isEmpty()) {
            averageTripCost = totalCombined.divide(BigDecimal.valueOf(trips.size()), 2, RoundingMode.HALF_UP);
        }

        return TripStatisticsReportDTO.builder()
                .totalTrips(trips.size())
                .totalTripFuelCost(totalFuel)
                .totalTripExpenseCost(totalExp)
                .totalTripCost(totalCombined)
                .averageCostPerTrip(averageTripCost)
                .tripStatistics(items)
                .build();
    }

    // -----------------------------------------------------------------------
    // Utility helpers
    // -----------------------------------------------------------------------

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) {
            return BigDecimal.ZERO;
        }
        if (obj instanceof BigDecimal bd) {
            return bd;
        }
        if (obj instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
