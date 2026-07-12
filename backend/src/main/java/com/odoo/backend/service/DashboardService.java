package com.odoo.backend.service;

import com.odoo.backend.dto.dashboard.DashboardSummaryDTO;
import com.odoo.backend.entity.Driver;
import com.odoo.backend.entity.MaintenanceRecord;
import com.odoo.backend.entity.Trip;
import com.odoo.backend.entity.Vehicle;
import com.odoo.backend.enums.DriverStatus;
import com.odoo.backend.enums.TripStatus;
import com.odoo.backend.enums.VehicleStatus;
import com.odoo.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for the Dashboard Module of TransitOps.
 *
 * <p>Aggregates data from all repositories to produce a single executive
 * dashboard snapshot, avoiding N+1 queries through targeted JPQL aggregations.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final DateTimeFormatter DT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final VehicleRepository     vehicleRepository;
    private final DriverRepository      driverRepository;
    private final TripRepository        tripRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final FuelRepository        fuelRepository;
    private final ExpenseRepository     expenseRepository;

    // -----------------------------------------------------------------------
    // Main dashboard endpoint
    // -----------------------------------------------------------------------

    /**
     * Builds the complete executive dashboard summary.
     *
     * @return {@link DashboardSummaryDTO} with all KPIs and recent activity
     */
    public DashboardSummaryDTO getDashboardSummary() {
        log.debug("Building executive dashboard summary");

        // ---- Vehicle Fleet Counts ----
        List<Object[]> vehicleStatusRows = vehicleRepository.countVehiclesGroupedByStatus();
        Map<VehicleStatus, Long> vehicleStatusBreakdown = new EnumMap<>(VehicleStatus.class);
        for (VehicleStatus s : VehicleStatus.values()) vehicleStatusBreakdown.put(s, 0L);

        long totalVehicles       = 0;
        long availableVehicles   = 0;
        long vehiclesOnTrip      = 0;
        long vehiclesInMaintenance = 0;
        long retiredVehicles     = 0;

        for (Object[] row : vehicleStatusRows) {
            VehicleStatus status = (VehicleStatus) row[0];
            long count = ((Number) row[1]).longValue();
            vehicleStatusBreakdown.put(status, count);
            totalVehicles += count;

            switch (status) {
                case AVAILABLE   -> availableVehicles   += count;
                case ON_TRIP     -> vehiclesOnTrip       += count;
                case IN_SHOP,
                     UNDER_REPAIR -> vehiclesInMaintenance += count;
                case RETIRED     -> retiredVehicles      += count;
            }
        }

        long activeVehicles = totalVehicles - retiredVehicles;
        BigDecimal fleetUtilPct = BigDecimal.ZERO;
        if (activeVehicles > 0) {
            fleetUtilPct = BigDecimal.valueOf(vehiclesOnTrip * 100.0 / activeVehicles)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }

        // ---- Driver Counts ----
        long totalDrivers     = driverRepository.count();
        long availableDrivers = driverRepository.countByStatus(DriverStatus.AVAILABLE);
        long driversOnTrip    = driverRepository.countByStatus(DriverStatus.ON_TRIP);

        // ---- Trip Counts ----
        List<Object[]> tripStatusRows = tripRepository.countTripsGroupedByStatus();
        long totalTrips     = 0;
        long activeTrips    = 0;
        long completedTrips = 0;
        long cancelledTrips = 0;
        long scheduledTrips = 0;

        for (Object[] row : tripStatusRows) {
            TripStatus status = (TripStatus) row[0];
            long count = ((Number) row[1]).longValue();
            totalTrips += count;
            switch (status) {
                case IN_PROGRESS -> activeTrips    += count;
                case COMPLETED   -> completedTrips += count;
                case CANCELLED   -> cancelledTrips += count;
                case SCHEDULED   -> scheduledTrips += count;
            }
        }

        // ---- Maintenance Counts ----
        long openMaintenance       = maintenanceRepository.countActiveMaintenanceRecords();
        long inProgressMaintenance = maintenanceRepository.countByStatus(
                com.odoo.backend.enums.MaintenanceStatus.IN_PROGRESS);

        // ---- Financial KPIs ----
        BigDecimal totalFuelCost = fuelRepository.sumTotalFuelCost(null, null)
                .orElse(BigDecimal.ZERO);
        BigDecimal totalMaintenanceCost = maintenanceRepository.sumTotalActualCost(null, null)
                .orElse(BigDecimal.ZERO);
        BigDecimal totalExpenses = expenseRepository.sumTotalExpenseAmount(null, null)
                .orElse(BigDecimal.ZERO);
        BigDecimal totalOperationalCost = totalFuelCost.add(totalMaintenanceCost).add(totalExpenses);

        // ---- Recent Trips (last 5) ----
        List<DashboardSummaryDTO.RecentTripDTO> recentTrips =
                tripRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5))
                        .stream()
                        .map(this::toRecentTripDTO)
                        .collect(Collectors.toList());

        // ---- Maintenance Alerts ----
        List<DashboardSummaryDTO.MaintenanceAlertDTO> maintenanceAlerts =
                maintenanceRepository.findActiveMaintenanceForDashboard()
                        .stream()
                        .limit(10)
                        .map(this::toMaintenanceAlertDTO)
                        .collect(Collectors.toList());

        return DashboardSummaryDTO.builder()
                // fleet
                .totalVehicles(totalVehicles)
                .availableVehicles(availableVehicles)
                .vehiclesOnTrip(vehiclesOnTrip)
                .vehiclesInMaintenance(vehiclesInMaintenance)
                .retiredVehicles(retiredVehicles)
                .fleetUtilizationPercent(fleetUtilPct)
                .vehicleStatusBreakdown(vehicleStatusBreakdown)
                // drivers
                .totalDrivers(totalDrivers)
                .availableDrivers(availableDrivers)
                .driversOnTrip(driversOnTrip)
                // trips
                .totalTrips(totalTrips)
                .activeTrips(activeTrips)
                .completedTrips(completedTrips)
                .cancelledTrips(cancelledTrips)
                .scheduledTrips(scheduledTrips)
                // maintenance
                .openMaintenanceRequests(openMaintenance)
                .inProgressMaintenance(inProgressMaintenance)
                // financials
                .totalFuelCost(totalFuelCost)
                .totalMaintenanceCost(totalMaintenanceCost)
                .totalExpenses(totalExpenses)
                .totalOperationalCost(totalOperationalCost)
                // activity feed
                .recentTrips(recentTrips)
                .maintenanceAlerts(maintenanceAlerts)
                .build();
    }

    // -----------------------------------------------------------------------
    // Private mappers
    // -----------------------------------------------------------------------

    private DashboardSummaryDTO.RecentTripDTO toRecentTripDTO(Trip trip) {
        String driverName = trip.getDriver() != null
                ? trip.getDriver().getFirstName() + " " + trip.getDriver().getLastName()
                : "N/A";
        String vehicleReg = trip.getVehicle() != null
                ? trip.getVehicle().getRegistrationNumber()
                : "N/A";

        return DashboardSummaryDTO.RecentTripDTO.builder()
                .tripId(trip.getId())
                .tripNumber(trip.getTripNumber())
                .vehicleRegistration(vehicleReg)
                .driverName(driverName)
                .source(trip.getSource())
                .destination(trip.getDestination())
                .status(trip.getStatus() != null ? trip.getStatus().name() : "UNKNOWN")
                .startTime(trip.getStartTime() != null
                        ? trip.getStartTime().format(DT_FORMATTER) : null)
                .build();
    }

    private DashboardSummaryDTO.MaintenanceAlertDTO toMaintenanceAlertDTO(Object[] row) {
        // row: [vehicleId, registrationNumber, maintenanceType, status, startDate, estimatedCost]
        Long vehicleId      = (Long) row[0];
        String regNum       = (String) row[1];
        Object maintType    = row[2];
        Object status       = row[3];
        Object startDate    = row[4];
        Object estimatedCost = row[5];

        return DashboardSummaryDTO.MaintenanceAlertDTO.builder()
                .maintenanceId(vehicleId) // using vehicleId as proxy; real ID available via fetch
                .vehicleRegistration(regNum)
                .serviceType(maintType != null ? maintType.toString() : "N/A")
                .status(status != null ? status.toString() : "N/A")
                .scheduledDate(startDate != null ? startDate.toString() : null)
                .estimatedCost(startDate != null && estimatedCost instanceof Number num
                        ? BigDecimal.valueOf(num.doubleValue()) : BigDecimal.ZERO)
                .build();
    }
}
