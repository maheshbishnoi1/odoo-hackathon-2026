package com.odoo.backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ============================================================================
 * Vehicle Status
 * ============================================================================
 *
 * Represents the current operational state of a vehicle.
 *
 * Workflow:
 *
 * AVAILABLE
 *      ↓
 * ON_TRIP
 *      ↓
 * AVAILABLE
 *
 * AVAILABLE
 *      ↓
 * IN_SHOP
 *      ↓
 * AVAILABLE
 *
 * AVAILABLE
 *      ↓
 * RETIRED
 *
 * A RETIRED vehicle can never become AVAILABLE again.
 *
 * ============================================================================
 */

@Getter
@RequiredArgsConstructor
public enum VehicleStatus {

    AVAILABLE("Available"),

    ON_TRIP("On Trip"),

    IN_SHOP("In Shop"),

    UNDER_REPAIR("Under Repair"),

    RETIRED("Retired");

    /**
     * Display name shown in UI.
     */
    private final String displayName;

    /**
     * Converts string to enum safely.
     */
    public static VehicleStatus fromString(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(status ->
                        status.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Vehicle can be dispatched only when available.
     */
    public boolean canDispatch() {
        return this == AVAILABLE;
    }

    /**
     * Vehicle can enter maintenance only if
     * it is not currently on a trip.
     */
    public boolean canGoToMaintenance() {
        return this == AVAILABLE || this == UNDER_REPAIR;
    }

    /**
     * Indicates whether vehicle is active
     * within the fleet.
     */
    public boolean isActive() {
        return this != RETIRED;
    }

    /**
     * Vehicle is selectable for trips.
     */
    public boolean isAvailable() {
        return this == AVAILABLE;
    }

}
