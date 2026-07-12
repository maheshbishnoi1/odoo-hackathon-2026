package com.odoo.backend.enums;

/**
 * Category of maintenance performed on a vehicle.
 */
public enum MaintenanceType {

    /** Scheduled routine servicing (e.g., oil change, filter replacement). */
    ROUTINE,

    /** General repair work. */
    REPAIR,

    /** Periodic safety or compliance inspection. */
    INSPECTION,

    /** Tire replacement or rotation. */
    TIRE,

    /** Brake system maintenance. */
    BRAKE,

    /** Engine-related work. */
    ENGINE,

    /** Electrical system work. */
    ELECTRICAL,

    /** Any other maintenance not covered by the above categories. */
    OTHER
}
