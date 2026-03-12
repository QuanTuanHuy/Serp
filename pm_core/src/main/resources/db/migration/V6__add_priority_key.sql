-- Author: QuanTuanHuy
-- Description: Part of Serp Project
-- Add stable priority_key for tenant-scoped dedupe/materialization

ALTER TABLE priorities
    ADD COLUMN IF NOT EXISTS priority_key VARCHAR(100);

UPDATE priorities
SET priority_key = LOWER(REGEXP_REPLACE(TRIM(name), '[^a-zA-Z0-9]+', '_', 'g')) || '_' || id
WHERE priority_key IS NULL
  AND name IS NOT NULL;

UPDATE priorities
SET priority_key = 'priority_' || id
WHERE priority_key IS NULL
   OR priority_key = '';

CREATE UNIQUE INDEX IF NOT EXISTS uidx_priorities_tenant_priority_key
    ON priorities (tenant_id, priority_key)
    WHERE deleted_at IS NULL;
