package com.odoo.backend.enums;

/**
 * Lifecycle status of a {@link com.odoo.backend.entity.MaintenanceRecord}.
 */
public enum MaintenanceStatus {

    /** Maintenance has been requested but not yet started. */
    OPEN,

    /** Maintenance work is currently in progress. */
    IN_PROGRESS,

    /** Maintenance has been completed. Vehicle status returns to AVAILABLE. */
    COMPLETED
}
