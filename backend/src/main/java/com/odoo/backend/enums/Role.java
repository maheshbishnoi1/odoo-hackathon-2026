package com.odoo.backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.odoo.backend.enums.Permission;
import java.util.Arrays;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum Role {

    ADMIN(
            "Admin",
            Set.of(
                    Permission.MANAGE_USERS,
                    Permission.MANAGE_VEHICLES,
                    Permission.MANAGE_DRIVERS,
                    Permission.MANAGE_TRIPS,
                    Permission.MANAGE_MAINTENANCE,
                    Permission.VIEW_REPORTS,
                    Permission.VIEW_DASHBOARD
            )
    ),

    FLEET_MANAGER(
            "Fleet Manager",
            Set.of(
                    Permission.MANAGE_VEHICLES,
                    Permission.MANAGE_MAINTENANCE,
                    Permission.VIEW_REPORTS,
                    Permission.VIEW_DASHBOARD
            )
    ),

    DISPATCHER(
            "Dispatcher",
            Set.of(
                    Permission.MANAGE_TRIPS,
                    Permission.MANAGE_DRIVERS,
                    Permission.VIEW_DASHBOARD
            )
    ),

    SAFETY_OFFICER(
            "Safety Officer",
            Set.of(
                    Permission.MANAGE_DRIVERS,
                    Permission.VIEW_REPORTS,
                    Permission.VIEW_DASHBOARD
            )
    ),

    FINANCE_MANAGER(
            "Finance Manager",
            Set.of(
                    Permission.VIEW_REPORTS,
                    Permission.MANAGE_EXPENSES,
                    Permission.VIEW_DASHBOARD
            )
    );

    private final String displayName;

    private final Set<Permission> permissions;

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public static Role fromString(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(role -> role.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(null);
    }
}