package com.odoo.backend.exception;

public class TripNotFoundException extends ResourceNotFoundException {

    public TripNotFoundException(Long id) {
        super("Trip", "id", id);
    }

}