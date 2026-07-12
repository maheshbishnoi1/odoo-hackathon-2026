package com.odoo.backend.exception;

public class VehicleNotFoundException extends ResourceNotFoundException {

    public VehicleNotFoundException(Long id) {
        super("Vehicle", "id", id);
    }

    public VehicleNotFoundException(String registrationNumber) {
        super("Vehicle", "registrationNumber", registrationNumber);
    }

}