/*
Author: Nguyen The Anh
Description: Part of Serp Project - Extend bag capacity and sealing metadata
*/

ALTER TABLE bags
    ADD COLUMN IF NOT EXISTS max_weight DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS max_volume DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS max_orders INT,
    ADD COLUMN IF NOT EXISTS current_weight DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS current_volume DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS current_orders INT,
    ADD COLUMN IF NOT EXISTS sealed_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE bags
SET max_weight = COALESCE(max_weight, 50.0),
    max_volume = COALESCE(max_volume, 0.5),
    max_orders = COALESCE(max_orders, 30),
    current_weight = COALESCE(current_weight, 0),
    current_volume = COALESCE(current_volume, 0),
    current_orders = COALESCE(current_orders, 0)
WHERE max_weight IS NULL
   OR max_volume IS NULL
   OR max_orders IS NULL
   OR current_weight IS NULL
   OR current_volume IS NULL
   OR current_orders IS NULL;

CREATE INDEX IF NOT EXISTS idx_bags_sealed_at ON bags(sealed_at);
