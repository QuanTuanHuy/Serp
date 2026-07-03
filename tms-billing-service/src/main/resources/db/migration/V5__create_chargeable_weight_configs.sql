-- Author: Nguyen The Anh
-- Description: Part of Serp Project
-- Purpose: Configure chargeable weight calculation parameters per delivery service.

CREATE TABLE chargeable_weight_configs (
    id BIGSERIAL PRIMARY KEY,
    service_code VARCHAR(32) NOT NULL,
    min_dimension_cm BIGINT NOT NULL,
    small_bulky_threshold_cm BIGINT NOT NULL,
    base_weight_gram BIGINT NOT NULL,
    step_weight_gram BIGINT NOT NULL,
    max_weight_gram BIGINT NOT NULL,
    volumetric_divisor DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    tenant_id BIGINT,
    CONSTRAINT uq_chargeable_weight_configs_service UNIQUE (service_code)
);

COMMENT ON TABLE chargeable_weight_configs IS 'Chargeable weight formula parameters by delivery service';

INSERT INTO chargeable_weight_configs (
    service_code,
    min_dimension_cm,
    small_bulky_threshold_cm,
    base_weight_gram,
    step_weight_gram,
    max_weight_gram,
    volumetric_divisor,
    created_at,
    updated_at
)
VALUES
    ('TIEU_CHUAN', 10, 100, 2000, 500, 15000, 5000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (service_code) DO NOTHING;
