package com.odoo.backend.exception;

/**
 * ============================================================================
 * Vehicle Not Found Exception
 * ============================================================================
 *
 * Thrown when a requested vehicle does not exist.
 *
 * Examples:
 * • Vehicle ID not found
 * • Registration number not found
 *
 * ============================================================================
 */
public class VehicleNotFoundException extends ResourceNotFoundException {

    public VehicleNotFoundException(Long vehicleId) {
        super("Vehicle not found with ID : " + vehicleId);
    }

    public VehicleNotFoundException(String registrationNumber) {
        super("Vehicle not found with Registration Number : " + registrationNumber);
    }
}