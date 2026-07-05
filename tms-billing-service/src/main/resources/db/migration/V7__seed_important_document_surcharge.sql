-- Author: Nguyen The Anh
-- Description: Part of Serp Project
-- Purpose: Seed important document surcharge at 5,000 VND per shipment.

INSERT INTO surcharge_rules (
    code,
    name,
    calculation_type,
    rate_percent,
    fixed_amount,
    min_amount,
    base_weight,
    base_price,
    step_weight,
    step_price,
    effective_date,
    expiration_date,
    created_at,
    updated_at
)
VALUES (
    'CHUNG_TU_QUAN_TRONG',
    'Phụ phí chứng từ quan trọng',
    'FIXED_PER_ORDER',
    NULL,
    5000,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    DATE '2020-01-01',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    calculation_type = EXCLUDED.calculation_type,
    rate_percent = EXCLUDED.rate_percent,
    fixed_amount = EXCLUDED.fixed_amount,
    min_amount = EXCLUDED.min_amount,
    base_weight = EXCLUDED.base_weight,
    base_price = EXCLUDED.base_price,
    step_weight = EXCLUDED.step_weight,
    step_price = EXCLUDED.step_price,
    effective_date = EXCLUDED.effective_date,
    expiration_date = EXCLUDED.expiration_date,
    updated_at = CURRENT_TIMESTAMP;
