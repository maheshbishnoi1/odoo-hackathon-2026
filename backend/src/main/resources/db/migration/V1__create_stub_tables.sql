-- ============================================================
-- V1: Create stub tables for Vehicle and Trip entities
-- These tables are owned by teammate modules; fields here are
-- the minimum required by the Maintenance, Fuel, and Expense modules.
-- Extend these tables when the Vehicle and Trip owners deliver their schemas.
-- ============================================================

-- Vehicles table
CREATE TABLE IF NOT EXISTS vehicles
(
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    registration_number VARCHAR(50)     NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',
    make                VARCHAR(100),
    model               VARCHAR(100),
    year                INT,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_vehicles               PRIMARY KEY (id),
    CONSTRAINT uq_vehicles_reg_number    UNIQUE (registration_number),
    CONSTRAINT chk_vehicle_status        CHECK (status IN ('AVAILABLE','ON_TRIP','IN_SHOP','UNDER_REPAIR','RETIRED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Trips table
CREATE TABLE IF NOT EXISTS trips
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    trip_number VARCHAR(50)  NOT NULL,
    vehicle_id  BIGINT       NOT NULL,
    origin      VARCHAR(255),
    destination VARCHAR(255),
    trip_date   DATE,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_trips             PRIMARY KEY (id),
    CONSTRAINT uq_trips_trip_number UNIQUE (trip_number),
    CONSTRAINT fk_trips_vehicle     FOREIGN KEY (vehicle_id) REFERENCES vehicles (id) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_trips_vehicle_id ON trips (vehicle_id);
