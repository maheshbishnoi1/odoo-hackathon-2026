package com.odoo.backend.enums;

/**
 * Payment method used to settle an {@link com.odoo.backend.entity.Expense}.
 */
public enum PaymentMode {

    /** Physical cash payment. */
    CASH,

    /** Debit or credit card. */
    CARD,

    /** Unified Payments Interface (UPI). */
    UPI,

    /** Direct bank transfer / NEFT / IMPS. */
    BANK_TRANSFER,

    /** Any other payment method. */
    OTHER
}
