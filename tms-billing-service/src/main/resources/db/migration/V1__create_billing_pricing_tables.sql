-- Author: Nguyen The Anh
-- Description: Part of Serp Project
-- Purpose: TMS billing - pricing tables (tariffs, surcharges, VAS) and seed defaults.
--          Seed amounts are placeholders; ops should tune via admin API or SQL.

-- ---------------------------------------------------------------------------
-- tariffs: base freight per delivery service + route type + effective window
-- ---------------------------------------------------------------------------
CREATE TABLE tariffs (
    id BIGSERIAL PRIMARY KEY,
    service_code VARCHAR(32) NOT NULL,
    route_type_code VARCHAR(64) NOT NULL,
    base_weight DOUBLE PRECISION NOT NULL,
    base_price DOUBLE PRECISION NOT NULL,
    step_weight DOUBLE PRECISION NOT NULL,
    step_price DOUBLE PRECISION NOT NULL,
    effective_date DATE NOT NULL,
    expiration_date DATE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    tenant_id BIGINT,
    CONSTRAINT uq_tariffs_service_route_effective UNIQUE (service_code, route_type_code, effective_date)
);

CREATE INDEX idx_tariffs_lookup
    ON tariffs (service_code, route_type_code, effective_date DESC);

COMMENT ON TABLE tariffs IS 'Base freight ladder: first slab weight/price + per-step weight/price by service and route type';

-- ---------------------------------------------------------------------------
-- surcharge_rules: optional / per-order / per-kg / step-weight surcharges
-- ---------------------------------------------------------------------------
CREATE TABLE surcharge_rules (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    calculation_type VARCHAR(32),
    rate_percent DOUBLE PRECISION,
    fixed_amount DOUBLE PRECISION,
    min_amount DOUBLE PRECISION,
    base_weight DOUBLE PRECISION,
    base_price DOUBLE PRECISION,
    step_weight DOUBLE PRECISION,
    step_price DOUBLE PRECISION,
    effective_date DATE,
    expiration_date DATE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    tenant_id BIGINT,
    CONSTRAINT uq_surcharge_rules_code UNIQUE (code)
);

COMMENT ON TABLE surcharge_rules IS 'Surcharge catalog; code matches SurchargeRuleEnum';

-- ---------------------------------------------------------------------------
-- vas_rules: value-added services (COD, …)
-- ---------------------------------------------------------------------------
CREATE TABLE vas_rules (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    calculation_type VARCHAR(32) NOT NULL,
    rate_percent DOUBLE PRECISION,
    fixed_amount DOUBLE PRECISION,
    min_amount DOUBLE PRECISION,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    tenant_id BIGINT,
    CONSTRAINT uq_vas_rules_code UNIQUE (code)
);

COMMENT ON TABLE vas_rules IS 'VAS catalog; code matches VasRuleCode';

-- ---------------------------------------------------------------------------
-- Seed: surcharges
-- ---------------------------------------------------------------------------
INSERT INTO surcharge_rules (code, name, calculation_type, rate_percent, fixed_amount, min_amount, base_weight, base_price, step_weight, step_price, effective_date, expiration_date, created_at, updated_at)
VALUES
    ('VUNG_XA', 'Phụ phí vùng sâu vùng xa', 'STEP_WEIGHT', NULL, NULL, NULL, 5000, 7000, 500, 500, DATE '2020-01-01', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- Seed: VAS
-- ---------------------------------------------------------------------------
INSERT INTO vas_rules (code, name, calculation_type, rate_percent, fixed_amount, min_amount, created_at, updated_at)
VALUES
    ('COD', 'Phí COD', 'FIXED_PER_ORDER', NULL, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
