-- Author: QuanTuanHuy
-- Description: Part of Serp Project
-- Purpose: Backfill scope_id for roles with MODULE scope from module_id

UPDATE roles
SET scope_id = module_id,
    updated_at = CURRENT_TIMESTAMP
WHERE scope = 'MODULE'
  AND module_id IS NOT NULL
  AND scope_id IS DISTINCT FROM module_id;
