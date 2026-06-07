-- ============================================================
-- V23 — Refactor School Bus App Config to Remove tenant_id
-- ============================================================

-- 1. Deduplicate existing configs: Retain only one record per config_code
DELETE FROM school_bus_app_config
WHERE id NOT IN (
    SELECT MIN(id)
    FROM school_bus_app_config
    GROUP BY config_code
);

-- 2. Drop the old tenant-based unique index and constraint
DROP INDEX IF EXISTS idx_school_bus_app_config_tenant_code_active;

ALTER TABLE school_bus_app_config
    DROP CONSTRAINT IF EXISTS uk_school_bus_app_config_tenant_code;

-- 3. Drop tenant_id column
ALTER TABLE school_bus_app_config
    DROP COLUMN IF EXISTS tenant_id;

-- 4. Create new unique index on config_code where is_deleted = false
CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_app_config_code_active
    ON school_bus_app_config (config_code)
    WHERE is_deleted = false;
