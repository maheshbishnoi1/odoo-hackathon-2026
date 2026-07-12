package com.odoo.backend.enums;

/**
 * ============================================================================
 * Trip Status Enum
 * ============================================================================
 *
 * Represents the lifecycle of a trip.
 *
 * Business Rules:
 * • SCHEDULED   -> Trip has been created but has not started.
 * • IN_PROGRESS -> Trip is currently active.
 * • COMPLETED   -> Trip has successfully finished.
 * • CANCELLED   -> Trip has been cancelled before completion.
 *
 * State Flow:
 *
 * SCHEDULED
 *      |
 *      |-----> IN_PROGRESS -------> COMPLETED
 *      |
 *      +--------------------------> CANCELLED
 *
 * ============================================================================
 */

public enum TripStatus {

    /**
     * Trip is scheduled and waiting to start.
     */
    SCHEDULED,

    /**
     * Trip is currently in progress.
     */
    IN_PROGRESS,

    /**
     * Trip has been completed successfully.
     */
    COMPLETED,

    /**
     * Trip has been cancelled.
     */
    CANCELLED
}