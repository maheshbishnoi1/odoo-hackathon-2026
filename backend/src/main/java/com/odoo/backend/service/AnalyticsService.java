package com.odoo.backend.service;

import com.odoo.backend.dto.analytics.*;
import com.odoo.backend.entity.Driver;
import com.odoo.backend.entity.Vehicle;
import com.odoo.backend.enums.DriverStatus;
import com.odoo.backend.enums.VehicleStatus;
import com.odoo.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for the Business Analytics Module of TransitOps.
 *
 * <p>Implements business-intelligence rules and fleet health monitoring:
 * cost trends, driver performance ranking, maintenance variance,
 * and smart fleet alerts.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final VehicleRepository     vehicleRepository;
    private final DriverRepository      driverRepository;
    private final TripRepository        tripRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final FuelRepository        fuelRepository;
    private final ExpenseRepository     expenseRepository;

    // -----------------------------------------------------------------------
    // 1. Monthly Cost Trend Analysis
    // -----------------------------------------------------------------------

    /**
     * Builds a month-by-month operational cost breakdown across Fuel, Maintenance, and Expenses.
     *
     * <p>Business Rule: All three cost categories are merged by year-month key
     * to produce a unified trend timeline for charting.</p>
     *
     * @return {@link CostTrendDTO} with monthly breakdown and totals
     */
    public CostTrendDTO getMonthlyCostTrend() {
        log.debug("Computing monthly cost trend analysis");

        // Fetch monthly aggregations from each repository
        Map<String, BigDecimal> fuelByMonth     = buildMonthMap(fuelRepository.aggregateMonthlyFuelCost());
        Map<String, BigDecimal> maintByMonth    = buildMonthMap(maintenanceRepository.aggregateMonthlyMaintenanceCost());
        Map<String, BigDecimal> expenseByMonth  = buildMonthMap(expenseRepository.aggregateMonthlyExpenseCost());

        // Union of all month keys
        Set<String> allMonths = new TreeSet<>();
        allMonths.addAll(fuelByMonth.keySet());
        allMonths.addAll(maintByMonth.keySet());
        allMonths.addAll(expenseByMonth.keySet());

        List<CostTrendDTO.MonthlyCostItem> items = new ArrayList<>();
        BigDecimal totalFuel  = BigDecimal.ZERO;
        BigDecimal totalMaint = BigDecimal.ZERO;
        BigDecimal totalExp   = BigDecimal.ZERO;

        for (String monthKey : allMonths) {
            int year  = Integer.parseInt(monthKey.substring(0, 4));
            int month = Integer.parseInt(monthKey.substring(5));

            BigDecimal fuelCost  = fuelByMonth.getOrDefault(monthKey, BigDecimal.ZERO);
            BigDecimal maintCost = maintByMonth.getOrDefault(monthKey, BigDecimal.ZERO);
            BigDecimal expCost   = expenseByMonth.getOrDefault(monthKey, BigDecimal.ZERO);
            BigDecimal total     = fuelCost.add(maintCost).add(expCost);

            totalFuel  = totalFuel.add(fuelCost);
            totalMaint = totalMaint.add(maintCost);
            totalExp   = totalExp.add(expCost);

            String label = java.time.Month.of(month)
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + year;

            items.add(CostTrendDTO.MonthlyCostItem.builder()
                    .year(year)
                    .month(month)
                    .monthLabel(label)
                    .fuelCost(fuelCost)
                    .maintenanceCost(maintCost)
                    .expenseCost(expCost)
                    .totalCost(total)
                    .build());
        }

        BigDecimal grandTotal = totalFuel.add(totalMaint).add(totalExp);

        return CostTrendDTO.builder()
                .monthlyCosts(items)
                .totalFuelCost(totalFuel)
                .totalMaintenanceCost(totalMaint)
                .totalExpenseCost(totalExp)
                .grandTotal(grandTotal)
                .build();
    }

    // -----------------------------------------------------------------------
    // 2. Driver Performance Ranking
    // -----------------------------------------------------------------------

    /**
     * Ranks all drivers by trips completed, distance covered, and costs incurred.
     *
     * <p>Business Rules:
     * <ul>
     *   <li>Completion rate = completedTrips / totalTripsAssigned × 100</li>
     *   <li>Drivers with 0 trips assigned still appear in the list.</li>
     * </ul></p>
     *
     * @return {@link DriverPerformanceDTO} ranked by total trip cost descending
     */
    public DriverPerformanceDTO getDriverPerformance() {
        log.debug("Computing driver performance analytics");

        List<Driver> allDrivers = driverRepository.findAll();

        // Aggregate trip stats per driver: [driverId, count, sumDistance, completedCount]
        Map<Long, long[]> tripStatsMap = new HashMap<>();
        for (Object[] row : tripRepository.aggregateTripStatsByDriver()) {
            Long driverId     = (Long) row[0];
            long count        = ((Number) row[1]).longValue();
            double dist       = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            long completed    = ((Number) row[3]).longValue();
            tripStatsMap.put(driverId, new long[]{ count, Double.doubleToLongBits(dist), completed });
        }

        // Aggregate fuel cost per driver
        Map<Long, BigDecimal> fuelByDriver = new HashMap<>();
        for (Object[] row : fuelRepository.aggregateFuelCostByDriver()) {
            fuelByDriver.put((Long) row[0], toBigDecimal(row[1]));
        }

        // Aggregate expense cost per driver
        Map<Long, BigDecimal> expByDriver = new HashMap<>();
        for (Object[] row : expenseRepository.aggregateExpenseCostByDriver()) {
            expByDriver.put((Long) row[0], toBigDecimal(row[1]));
        }

        List<DriverPerformanceDTO.DriverPerformanceItem> items = new ArrayList<>();
        long totalTrips    = 0;
        double totalDist   = 0.0;

        for (Driver driver : allDrivers) {
            long[] stats     = tripStatsMap.getOrDefault(driver.getId(), new long[]{0L, 0L, 0L});
            long assigned    = stats[0];
            double dist      = Double.longBitsToDouble(stats[1]);
            long completed   = stats[2];

            BigDecimal fuelCost = fuelByDriver.getOrDefault(driver.getId(), BigDecimal.ZERO);
            BigDecimal expCost  = expByDriver.getOrDefault(driver.getId(), BigDecimal.ZERO);
            BigDecimal tripCost = fuelCost.add(expCost);

            BigDecimal completionRate = BigDecimal.ZERO;
            if (assigned > 0) {
                completionRate = BigDecimal.valueOf(completed * 100.0 / assigned)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            totalTrips += assigned;
            totalDist  += dist;

            items.add(DriverPerformanceDTO.DriverPerformanceItem.builder()
                    .driverId(driver.getId())
                    .driverName(driver.getFirstName() + " " + driver.getLastName())
                    .licenseNumber(driver.getLicenseNumber())
                    .status(driver.getStatus() != null ? driver.getStatus().name() : "UNKNOWN")
                    .completedTrips(completed)
                    .totalTripsAssigned(assigned)
                    .totalDistanceCovered(dist)
                    .totalFuelCost(fuelCost)
                    .totalExpenseCost(expCost)
                    .totalTripCost(tripCost)
                    .completionRate(completionRate)
                    .build());
        }

        // Sort by total trip cost descending
        items.sort(Comparator.comparing(
                DriverPerformanceDTO.DriverPerformanceItem::getTotalTripCost).reversed());

        return DriverPerformanceDTO.builder()
                .driverPerformances(items)
                .totalTrips(totalTrips)
                .totalDistanceCovered(totalDist)
                .build();
    }

    // -----------------------------------------------------------------------
    // 3. Maintenance Cost Variance Analysis
    // -----------------------------------------------------------------------

    /**
     * Computes variance between estimated and actual maintenance costs per vehicle.
     *
     * <p>Business Rules:
     * <ul>
     *   <li>Variance = actualCost − estimatedCost</li>
     *   <li>Positive variance = over budget</li>
     *   <li>Variance% = variance / estimatedCost × 100</li>
     * </ul></p>
     *
     * @return {@link MaintenanceVarianceDTO}
     */
    public MaintenanceVarianceDTO getMaintenanceVariance() {
        log.debug("Computing maintenance cost variance analytics");

        // Aggregate per vehicle: [vehicleId, regNum, count, sumEstimated, sumActual]
        List<Object[]> rows = maintenanceRepository.aggregateMaintenanceCostPerVehicle(null, null);

        List<MaintenanceVarianceDTO.VehicleVarianceItem> items = new ArrayList<>();
        BigDecimal fleetEstimated = BigDecimal.ZERO;
        BigDecimal fleetActual    = BigDecimal.ZERO;
        long overBudgetCount  = 0;
        long underBudgetCount = 0;

        for (Object[] row : rows) {
            Long vehicleId    = (Long) row[0];
            String regNum     = (String) row[1];
            long count        = ((Number) row[2]).longValue();
            BigDecimal est    = toBigDecimal(row[3]);
            BigDecimal actual = toBigDecimal(row[4]);

            BigDecimal variance = actual.subtract(est);
            boolean isOverBudget = variance.compareTo(BigDecimal.ZERO) > 0;

            BigDecimal variancePct = BigDecimal.ZERO;
            if (est.compareTo(BigDecimal.ZERO) != 0) {
                variancePct = variance.multiply(BigDecimal.valueOf(100))
                        .divide(est, 2, RoundingMode.HALF_UP);
            }

            fleetEstimated = fleetEstimated.add(est);
            fleetActual    = fleetActual.add(actual);

            if (isOverBudget) overBudgetCount++;
            else underBudgetCount++;

            items.add(MaintenanceVarianceDTO.VehicleVarianceItem.builder()
                    .vehicleId(vehicleId)
                    .vehicleRegistration(regNum)
                    .maintenanceCount(count)
                    .totalEstimated(est)
                    .totalActual(actual)
                    .variance(variance)
                    .variancePercent(variancePct)
                    .overBudget(isOverBudget)
                    .build());
        }

        // Sort by absolute variance descending
        items.sort(Comparator.comparing(
                item -> item.getVariance().abs(),
                Comparator.reverseOrder()));

        BigDecimal fleetVariance = fleetActual.subtract(fleetEstimated);
        BigDecimal fleetVariancePct = BigDecimal.ZERO;
        if (fleetEstimated.compareTo(BigDecimal.ZERO) != 0) {
            fleetVariancePct = fleetVariance.multiply(BigDecimal.valueOf(100))
                    .divide(fleetEstimated, 2, RoundingMode.HALF_UP);
        }

        return MaintenanceVarianceDTO.builder()
                .vehicleVariances(items)
                .fleetTotalEstimated(fleetEstimated)
                .fleetTotalActual(fleetActual)
                .fleetTotalVariance(fleetVariance)
                .fleetVariancePercent(fleetVariancePct)
                .overBudgetCount(overBudgetCount)
                .underBudgetCount(underBudgetCount)
                .build();
    }

    // -----------------------------------------------------------------------
    // 4. Fleet Health & Smart Business Rule Alerts
    // -----------------------------------------------------------------------

    /**
     * Evaluates fleet health using business rule engine.
     *
     * <p>Business Rules enforced:
     * <ol>
     *   <li>Driver license expiry within 30 days → WARNING; within 7 days → CRITICAL</li>
     *   <li>Vehicles with open/in-progress maintenance → Maintenance Alerts</li>
     *   <li>Vehicles with no trips in the last 30 days → Idle Asset Alert</li>
     *   <li>Top 5 vehicles by total operational cost → High Cost Alert</li>
     *   <li>Health Score = 100 − (alertPenalty × count)</li>
     * </ol></p>
     *
     * @return {@link FleetHealthDTO} with health score and all alerts
     */
    public FleetHealthDTO getFleetHealth() {
        log.debug("Evaluating fleet health with business rules");

        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // --- Rule 1: License expiry alerts ---
        List<FleetHealthDTO.LicenseExpiryAlert> licenseAlerts =
                driverRepository.findByLicenseExpiryDateBefore(in30Days).stream()
                        .filter(d -> d.getLicenseExpiryDate() != null
                                && !d.getLicenseExpiryDate().isBefore(today)) // not expired yet
                        .map(d -> {
                            long daysLeft = today.until(d.getLicenseExpiryDate(),
                                    java.time.temporal.ChronoUnit.DAYS);
                            return FleetHealthDTO.LicenseExpiryAlert.builder()
                                    .driverId(d.getId())
                                    .driverName(d.getFirstName() + " " + d.getLastName())
                                    .licenseNumber(d.getLicenseNumber())
                                    .licenseExpiryDate(d.getLicenseExpiryDate().toString())
                                    .daysUntilExpiry(daysLeft)
                                    .severity(daysLeft <= 7 ? "CRITICAL" : "WARNING")
                                    .build();
                        })
                        .sorted(Comparator.comparingLong(FleetHealthDTO.LicenseExpiryAlert::getDaysUntilExpiry))
                        .collect(Collectors.toList());

        // --- Rule 2: Active maintenance alerts ---
        List<FleetHealthDTO.MaintenanceAlert> maintenanceAlerts =
                maintenanceRepository.findActiveMaintenanceForDashboard().stream()
                        .map(row -> {
                            Long vehicleId   = (Long) row[0];
                            String regNum    = (String) row[1];
                            Object mType     = row[2];
                            Object status    = row[3];
                            Object startDate = row[4];
                            Object estCost   = row[5];

                            long daysInShop = 0;
                            if (startDate instanceof LocalDate ld) {
                                daysInShop = today.until(ld, java.time.temporal.ChronoUnit.DAYS);
                                daysInShop = Math.abs(daysInShop);
                            }

                            return FleetHealthDTO.MaintenanceAlert.builder()
                                    .vehicleId(vehicleId)
                                    .vehicleRegistration(regNum)
                                    .maintenanceType(mType != null ? mType.toString() : "N/A")
                                    .status(status != null ? status.toString() : "N/A")
                                    .startDate(startDate != null ? startDate.toString() : null)
                                    .estimatedCost(estCost instanceof Number n
                                            ? BigDecimal.valueOf(n.doubleValue()) : BigDecimal.ZERO)
                                    .daysInShop(daysInShop)
                                    .build();
                        })
                        .collect(Collectors.toList());

        // --- Rule 3: Idle vehicle alerts (no trips in 30 days) ---
        List<Vehicle> allVehicles = vehicleRepository.findAll();
        List<FleetHealthDTO.IdleVehicleAlert> idleAlerts = new ArrayList<>();

        for (Vehicle v : allVehicles) {
            if (v.getStatus() == VehicleStatus.RETIRED) continue;
            List<com.odoo.backend.entity.Trip> recentTrips =
                    tripRepository.findRecentTripsForVehicle(v.getId(), thirtyDaysAgo);
            if (recentTrips.isEmpty()) {
                idleAlerts.add(FleetHealthDTO.IdleVehicleAlert.builder()
                        .vehicleId(v.getId())
                        .vehicleRegistration(v.getRegistrationNumber())
                        .vehicleName(v.getVehicleName())
                        .status(v.getStatus() != null ? v.getStatus().name() : "UNKNOWN")
                        .lastTripDate(null)
                        .idleDays(30)
                        .build());
            }
        }

        // --- Rule 4: Top 5 high-cost vehicles ---
        Map<Long, BigDecimal> fuelMap = new HashMap<>();
        for (Object[] r : fuelRepository.aggregateFuelCostPerVehicle(null, null)) {
            fuelMap.put((Long) r[0], toBigDecimal(r[4]));
        }
        Map<Long, BigDecimal> maintMap = new HashMap<>();
        for (Object[] r : maintenanceRepository.aggregateMaintenanceCostPerVehicle(null, null)) {
            maintMap.put((Long) r[0], toBigDecimal(r[4]));
        }
        Map<Long, BigDecimal> expMap = new HashMap<>();
        for (Object[] r : expenseRepository.aggregateExpensePerVehicle(null, null)) {
            expMap.put((Long) r[0], toBigDecimal(r[3]));
        }

        List<FleetHealthDTO.HighCostVehicleAlert> highCostAlerts = allVehicles.stream()
                .map(v -> {
                    BigDecimal fc = fuelMap.getOrDefault(v.getId(), BigDecimal.ZERO);
                    BigDecimal mc = maintMap.getOrDefault(v.getId(), BigDecimal.ZERO);
                    BigDecimal ec = expMap.getOrDefault(v.getId(), BigDecimal.ZERO);
                    return FleetHealthDTO.HighCostVehicleAlert.builder()
                            .vehicleId(v.getId())
                            .vehicleRegistration(v.getRegistrationNumber())
                            .fuelCost(fc)
                            .maintenanceCost(mc)
                            .expenseCost(ec)
                            .totalCost(fc.add(mc).add(ec))
                            .build();
                })
                .sorted(Comparator.comparing(FleetHealthDTO.HighCostVehicleAlert::getTotalCost).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // --- Calculate Health Score ---
        int alertCount = licenseAlerts.size() + maintenanceAlerts.size()
                + idleAlerts.size() + highCostAlerts.size();

        // Business Rule: deduct 5 points per alert, minimum 0
        int healthScore = Math.max(0, 100 - (alertCount * 5));
        String healthGrade;
        if (healthScore >= 90) healthGrade = "EXCELLENT";
        else if (healthScore >= 75) healthGrade = "GOOD";
        else if (healthScore >= 50) healthGrade = "FAIR";
        else healthGrade = "POOR";

        FleetHealthDTO.AlertSummary alertSummary = FleetHealthDTO.AlertSummary.builder()
                .licenseExpiryCount(licenseAlerts.size())
                .activeMaintenanceCount(maintenanceAlerts.size())
                .idleVehicleCount(idleAlerts.size())
                .highCostVehicleCount(highCostAlerts.size())
                .totalAlerts(alertCount)
                .build();

        return FleetHealthDTO.builder()
                .healthScore(healthScore)
                .healthGrade(healthGrade)
                .licenseExpiryAlerts(licenseAlerts)
                .activeMaintenanceAlerts(maintenanceAlerts)
                .idleVehicleAlerts(idleAlerts)
                .highCostVehicleAlerts(highCostAlerts)
                .alertSummary(alertSummary)
                .build();
    }

    // -----------------------------------------------------------------------
    // Utility helpers
    // -----------------------------------------------------------------------

    /**
     * Converts raw Object[] monthly aggregation rows into a yearMonth→amount map.
     * Row format: [year, month, amount].
     */
    private Map<String, BigDecimal> buildMonthMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new TreeMap<>();
        for (Object[] row : rows) {
            if (row[0] == null || row[1] == null) continue;
            String key = row[0] + "-" + String.format("%02d", ((Number) row[1]).intValue());
            map.put(key, toBigDecimal(row[2]));
        }
        return map;
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal bd) return bd;
        if (obj instanceof Number num) return BigDecimal.valueOf(num.doubleValue());
        return BigDecimal.ZERO;
    }
}
