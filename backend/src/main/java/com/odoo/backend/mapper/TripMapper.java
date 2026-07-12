package com.odoo.backend.mapper;

import com.odoo.backend.dto.TripRequest;
import com.odoo.backend.dto.TripResponse;
import com.odoo.backend.dto.TripUpdateRequest;
import com.odoo.backend.entity.Driver;
import com.odoo.backend.entity.Trip;
import com.odoo.backend.entity.Vehicle;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * Trip Mapper
 * ============================================================================
 *
 * Converts between:
 * • TripRequest -> Trip
 * • Trip -> TripResponse
 * • TripUpdateRequest -> Existing Trip
 *
 * ============================================================================
 */

@Component
public class TripMapper {

    /**
     * Converts TripRequest DTO to Trip entity.
     *
     * Vehicle and Driver are supplied by the service layer.
     *
     * @param request Trip request.
     * @param vehicle Vehicle entity.
     * @param driver Driver entity.
     * @return Trip entity.
     */
    public Trip toEntity(
            TripRequest request,
            Vehicle vehicle,
            Driver driver
    ) {

        if (request == null) {
            return null;
        }

        return Trip.builder()
                .vehicle(vehicle)
                .driver(driver)
                .source(request.getSource())
                .destination(request.getDestination())
                .startTime(request.getStartTime())
                .distance(request.getDistance())
                .estimatedDuration(request.getEstimatedDuration())
                .notes(request.getNotes())
                .build();
    }

    /**
     * Converts Trip entity to TripResponse DTO.
     *
     * @param trip Trip entity.
     * @return Trip response.
     */
    public TripResponse toResponse(Trip trip) {

        if (trip == null) {
            return null;
        }

        return TripResponse.builder()
                .id(trip.getId())
                .tripNumber(trip.getTripNumber())

                // Vehicle
                .vehicleId(trip.getVehicle().getId())
                .vehicleRegistrationNumber(
                        trip.getVehicle().getRegistrationNumber()
                )
                .vehicleModel(
                        trip.getVehicle().getVehicleName()
                )

                // Driver
                .driverId(trip.getDriver().getId())
                .driverName(
                        trip.getDriver().getFullName()
                )
                .driverLicenseNumber(
                        trip.getDriver().getLicenseNumber()
                )

                // Trip Details
                .source(trip.getSource())
                .destination(trip.getDestination())
                .startTime(trip.getStartTime())
                .endTime(trip.getEndTime())
                .distance(trip.getDistance())
                .estimatedDuration(trip.getEstimatedDuration())
                .actualDuration(trip.getActualDuration())
                .status(trip.getStatus())
                .notes(trip.getNotes())

                // Audit
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())

                .build();
    }

    /**
     * Updates an existing Trip entity.
     *
     * Vehicle and Driver are resolved in the service layer.
     *
     * @param trip Existing trip.
     * @param request Update request.
     * @param vehicle Vehicle entity.
     * @param driver Driver entity.
     */
    public void updateEntity(
            Trip trip,
            TripUpdateRequest request,
            Vehicle vehicle,
            Driver driver
    ) {

        if (trip == null || request == null) {
            return;
        }

        trip.setVehicle(vehicle);
        trip.setDriver(driver);
        trip.setSource(request.getSource());
        trip.setDestination(request.getDestination());
        trip.setStartTime(request.getStartTime());
        trip.setDistance(request.getDistance());
        trip.setEstimatedDuration(request.getEstimatedDuration());
        trip.setNotes(request.getNotes());
    }

}