package com.odoo.backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ============================================================================
 * Vehicle Type
 * ============================================================================
 *
 * Represents the category of a vehicle in the fleet.
 *
 * Used for:
 * • Fleet Management
 * • Trip Assignment
 * • Dashboard Analytics
 * • Filtering & Reports
 *
 * ============================================================================
 */

@Getter
@RequiredArgsConstructor
public enum VehicleType {

    TRUCK("Truck"),

    VAN("Van"),

    PICKUP("Pickup"),

    BUS("Bus"),

    CAR("Car"),

    BIKE("Bike"),

    TRAILER("Trailer"),

    OTHER("Other");

    /**
     * Display name used in UI.
     */
    private final String displayName;

    /**
     * Converts string safely to enum.
     */
    public static VehicleType fromString(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(type ->
                        type.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns true if this vehicle type
     * is considered a heavy vehicle.
     */
    public boolean isHeavyVehicle() {
        return this == TRUCK
                || this == BUS
                || this == TRAILER;
    }

    /**
     * Returns true if suitable for
     * cargo transportation.
     */
    public boolean isCargoVehicle() {
        return this == TRUCK
                || this == VAN
                || this == PICKUP
                || this == TRAILER;
    }

    /**
     * Returns true if generally used
     * for passenger transport.
     */
    public boolean isPassengerVehicle() {
        return this == BUS
                || this == CAR;
    }

}