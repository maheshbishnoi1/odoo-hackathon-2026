-- ============================================================
-- V2: Create maintenance_records table
-- ============================================================

CREATE TABLE IF NOT EXISTS maintenance_records
(
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    vehicle_id       BIGINT          NOT NULL,
    maintenance_type VARCHAR(30)     NOT NULL,
    description      TEXT,
    estimated_cost   DECIMAL(12, 2),
    actual_cost      DECIMAL(12, 2),
    start_date       DATE            NOT NULL,
    completion_date  DATE,
    status           VARCHAR(20)     NOT NULL DEFAULT 'OPEN',
    remarks          TEXT,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_maintenance_records   PRIMARY KEY (id),
    CONSTRAINT fk_maintenance_vehicle   FOREIGN KEY (vehicle_id) REFERENCES vehicles (id) ON DELETE RESTRICT,
    CONSTRAINT chk_maintenance_status   CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT chk_maintenance_type     CHECK (maintenance_type IN
                                               ('ROUTINE', 'REPAIR', 'INSPECTION', 'TIRE',
                                                'BRAKE', 'ENGINE', 'ELECTRICAL', 'OTHER')),
    CONSTRAINT chk_estimated_cost       CHECK (estimated_cost IS NULL OR estimated_cost > 0),
    CONSTRAINT chk_actual_cost          CHECK (actual_cost IS NULL OR actual_cost > 0),
    CONSTRAINT chk_completion_date      CHECK (completion_date IS NULL OR completion_date >= start_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_maintenance_vehicle_id ON maintenance_records (vehicle_id);
CREATE INDEX idx_maintenance_status     ON maintenance_records (status);
CREATE INDEX idx_maintenance_start_date ON maintenance_records (start_date);
