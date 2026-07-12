package com.odoo.backend.enums;

/**
 * ============================================================================
 * Driver Status Enum
 * ============================================================================
 *
 * Represents the current availability and working status of a driver.
 *
 * Business Rules:
 * • AVAILABLE  -> Driver is available for trip assignment.
 * • ON_TRIP    -> Driver is currently assigned to an active trip.
 * • OFF_DUTY   -> Driver is not working for the current shift.
 * • ON_LEAVE   -> Driver is on approved leave.
 * • SUSPENDED  -> Driver cannot be assigned to any trip.
 *
 * ============================================================================
 */

public enum DriverStatus {

    /**
     * Driver is available for new trip assignments.
     */
    AVAILABLE,

    /**
     * Driver is currently driving an active trip.
     */
    ON_TRIP,

    /**
     * Driver has completed the shift or is off duty.
     */
    OFF_DUTY,

    /**
     * Driver is on approved leave.
     */
    ON_LEAVE,

    /**
     * Driver is suspended and cannot operate any vehicle.
     */
    SUSPENDED
}