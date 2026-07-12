package com.odoo.backend.enums;

/**
 * High-level category of a vehicle {@link com.odoo.backend.entity.Expense}.
 */
public enum ExpenseType {

    /** Fuel purchase (distinct from FuelLog — can be recorded here too for aggregation). */
    FUEL,

    /** Maintenance-related costs. */
    MAINTENANCE,

    /** Highway / toll charges. */
    TOLL,

    /** Repair costs not covered under routine maintenance. */
    REPAIR,

    /** Vehicle insurance premium payments. */
    INSURANCE,

    /** Any other miscellaneous expense. */
    OTHER
}
