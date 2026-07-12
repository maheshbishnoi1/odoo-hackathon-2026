-- ============================================================
-- V4: Create expenses table
-- ============================================================

CREATE TABLE IF NOT EXISTS expenses
(
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    vehicle_id   BIGINT          NOT NULL,
    trip_id      BIGINT,
    expense_type VARCHAR(20)     NOT NULL,
    title        VARCHAR(255)    NOT NULL,
    description  TEXT,
    amount       DECIMAL(12, 2)  NOT NULL,
    expense_date DATE            NOT NULL,
    payment_mode VARCHAR(20)     NOT NULL,
    remarks      TEXT,
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_expenses          PRIMARY KEY (id),
    CONSTRAINT fk_expense_vehicle   FOREIGN KEY (vehicle_id) REFERENCES vehicles (id) ON DELETE RESTRICT,
    CONSTRAINT fk_expense_trip      FOREIGN KEY (trip_id)    REFERENCES trips (id)    ON DELETE SET NULL,
    CONSTRAINT chk_expense_type     CHECK (expense_type IN
                                           ('FUEL', 'MAINTENANCE', 'TOLL', 'REPAIR', 'INSURANCE', 'OTHER')),
    CONSTRAINT chk_payment_mode     CHECK (payment_mode IN
                                           ('CASH', 'CARD', 'UPI', 'BANK_TRANSFER', 'OTHER')),
    CONSTRAINT chk_expense_amount   CHECK (amount > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_expense_vehicle_id ON expenses (vehicle_id);
CREATE INDEX idx_expense_trip_id    ON expenses (trip_id);
CREATE INDEX idx_expense_type       ON expenses (expense_type);
CREATE INDEX idx_expense_date       ON expenses (expense_date);
