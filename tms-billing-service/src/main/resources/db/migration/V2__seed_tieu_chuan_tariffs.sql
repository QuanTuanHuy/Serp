-- Author: Nguyen The Anh
-- Description: Part of Serp Project
-- Purpose: Seed simplified TIEU_CHUAN tariffs (editable via admin API).

INSERT INTO tariffs (service_code, route_type_code, base_weight, base_price, step_weight, step_price, effective_date, expiration_date, created_at, updated_at)
VALUES
    ('TIEU_CHUAN', 'NOI_TINH_NOI_CUM', 3000, 16500, 500, 2500, DATE '2025-07-10', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TIEU_CHUAN', 'NOI_TINH_LIEN_CUM', 500, 30000, 500, 3000, DATE '2025-07-10', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TIEU_CHUAN', 'NOI_MIEN', 500, 30000, 500, 3000, DATE '2025-07-10', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TIEU_CHUAN', 'LIEN_MIEN', 3000, 57000, 500, 5000, DATE '2025-07-10', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TIEU_CHUAN', 'LIEN_MIEN_DAC_BIET', 3000, 57000, 500, 5000, DATE '2025-07-10', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (service_code, route_type_code, effective_date) DO NOTHING;
