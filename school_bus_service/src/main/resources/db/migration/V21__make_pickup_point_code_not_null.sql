-- ============================================================
-- V21: Backfill pickup point codes and apply NOT NULL constraint
-- ============================================================

-- 1. Backfill code for any existing records using format PKP000001
UPDATE school_bus_pickup_point
SET code = 'PKP' || lpad(id::text, 6, '0')
WHERE code IS NULL;

-- 2. Enforce NOT NULL on the code column
ALTER TABLE school_bus_pickup_point
    ALTER COLUMN code SET NOT NULL;
