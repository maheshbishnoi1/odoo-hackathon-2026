package com.odoo.backend.exception;

public class MaintenanceNotFoundException extends ResourceNotFoundException {

    public MaintenanceNotFoundException(Long id) {
        super("Maintenance", "id", id);
    }

}