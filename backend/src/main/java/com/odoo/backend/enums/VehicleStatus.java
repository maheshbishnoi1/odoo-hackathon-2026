package com.odoo.backend.enums;

/**
 * Represents the operational status of a {@link com.odoo.backend.entity.Vehicle}.
 */
public enum VehicleStatus {

    /** Vehicle is available for trips. */
    AVAILABLE,

    /** Vehicle is currently on an active trip. */
    ON_TRIP,

    /** Vehicle is in the maintenance shop. Set automatically when a Maintenance record is created. */
    IN_SHOP,

    /** Vehicle is under repair (non-routine). */
    UNDER_REPAIR,

    /** Vehicle has been permanently retired and cannot receive new maintenance or trips. */
    RETIRED
}
