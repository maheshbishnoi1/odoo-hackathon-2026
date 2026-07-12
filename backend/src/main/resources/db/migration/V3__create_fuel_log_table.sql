-- ============================================================
-- V3: Create fuel_logs table
-- ============================================================

CREATE TABLE IF NOT EXISTS fuel_logs
(
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    vehicle_id       BIGINT          NOT NULL,
    trip_id          BIGINT,
    fuel_date        DATE            NOT NULL,
    fuel_type        VARCHAR(20)     NOT NULL,
    quantity         DECIMAL(10, 2)  NOT NULL,
    cost             DECIMAL(12, 2)  NOT NULL,
    vendor           VARCHAR(200),
    odometer_reading BIGINT          NOT NULL,
    cost_per_liter   DECIMAL(10, 4),
    remarks          TEXT,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_fuel_logs         PRIMARY KEY (id),
    CONSTRAINT fk_fuel_vehicle      FOREIGN KEY (vehicle_id) REFERENCES vehicles (id) ON DELETE RESTRICT,
    CONSTRAINT fk_fuel_trip         FOREIGN KEY (trip_id)    REFERENCES trips (id)    ON DELETE SET NULL,
    CONSTRAINT chk_fuel_type        CHECK (fuel_type IN ('PETROL', 'DIESEL', 'CNG', 'ELECTRIC', 'OTHER')),
    CONSTRAINT chk_fuel_quantity    CHECK (quantity > 0),
    CONSTRAINT chk_fuel_cost        CHECK (cost > 0),
    CONSTRAINT chk_fuel_odometer    CHECK (odometer_reading >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_fuel_log_vehicle_id ON fuel_logs (vehicle_id);
CREATE INDEX idx_fuel_log_trip_id    ON fuel_logs (trip_id);
CREATE INDEX idx_fuel_log_fuel_date  ON fuel_logs (fuel_date);
