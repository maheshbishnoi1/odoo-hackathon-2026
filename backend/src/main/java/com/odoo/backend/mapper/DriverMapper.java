package com.odoo.backend.mapper;

import com.odoo.backend.dto.DriverRequest;
import com.odoo.backend.dto.DriverResponse;
import com.odoo.backend.dto.DriverUpdateRequest;
import com.odoo.backend.entity.Driver;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * Driver Mapper
 * ============================================================================
 *
 * Converts between:
 * • DriverRequest -> Driver
 * • Driver -> DriverResponse
 * • DriverUpdateRequest -> Existing Driver
 *
 * ============================================================================
 */

@Component
public class DriverMapper {

    /**
     * Converts DriverRequest DTO to Driver Entity.
     *
     * @param request Driver request.
     * @return Driver entity.
     */
    public Driver toEntity(DriverRequest request) {

        if (request == null) {
            return null;
        }

        return Driver.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .licenseNumber(request.getLicenseNumber())
                .licenseExpiryDate(request.getLicenseExpiryDate())
                .dateOfBirth(request.getDateOfBirth())
                .joiningDate(request.getJoiningDate())
                .status(request.getStatus())
                .address(request.getAddress())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .build();
    }

    /**
     * Converts Driver Entity to DriverResponse DTO.
     *
     * @param driver Driver entity.
     * @return Driver response DTO.
     */
    public DriverResponse toResponse(Driver driver) {

        if (driver == null) {
            return null;
        }

        return DriverResponse.builder()
                .id(driver.getId())
                .firstName(driver.getFirstName())
                .lastName(driver.getLastName())
                .fullName(driver.getFullName())
                .email(driver.getEmail())
                .phone(driver.getPhone())
                .licenseNumber(driver.getLicenseNumber())
                .licenseExpiryDate(driver.getLicenseExpiryDate())
                .licenseValid(driver.isLicenseValid())
                .dateOfBirth(driver.getDateOfBirth())
                .joiningDate(driver.getJoiningDate())
                .status(driver.getStatus())
                .address(driver.getAddress())
                .emergencyContactName(driver.getEmergencyContactName())
                .emergencyContactPhone(driver.getEmergencyContactPhone())
                .createdAt(driver.getCreatedAt())
                .updatedAt(driver.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing Driver entity using DriverUpdateRequest.
     *
     * @param driver Existing driver.
     * @param request Update request.
     */
    public void updateEntity(Driver driver, DriverUpdateRequest request) {

        if (driver == null || request == null) {
            return;
        }

        driver.setFirstName(request.getFirstName());
        driver.setLastName(request.getLastName());
        driver.setEmail(request.getEmail());
        driver.setPhone(request.getPhone());
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setLicenseExpiryDate(request.getLicenseExpiryDate());
        driver.setDateOfBirth(request.getDateOfBirth());
        driver.setJoiningDate(request.getJoiningDate());
        driver.setStatus(request.getStatus());
        driver.setAddress(request.getAddress());
        driver.setEmergencyContactName(request.getEmergencyContactName());
        driver.setEmergencyContactPhone(request.getEmergencyContactPhone());
    }
}