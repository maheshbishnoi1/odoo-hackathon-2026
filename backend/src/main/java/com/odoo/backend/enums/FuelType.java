package com.odoo.backend.enums;

/**
 * Type of fuel used in a {@link com.odoo.backend.entity.FuelLog}.
 */
public enum FuelType {

    /** Regular petrol / gasoline. */
    PETROL,

    /** Diesel fuel. */
    DIESEL,

    /** Compressed Natural Gas. */
    CNG,

    /** Electric vehicle charging. */
    ELECTRIC,

    /** Any other fuel type. */
    OTHER
}
