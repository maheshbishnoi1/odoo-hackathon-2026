package com.odoo.backend.exception;

public class DriverNotFoundException extends ResourceNotFoundException {

    public DriverNotFoundException(Long id) {
        super("Driver", "id", id);
    }

    public DriverNotFoundException(String licenseNumber) {
        super("Driver", "licenseNumber", licenseNumber);
    }

}