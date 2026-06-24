WITH ranked AS (
    SELECT
        id,
        row_number() OVER (
            PARTITION BY sync_code
            ORDER BY
                COALESCE(last_success_sync_at, last_attempt_sync_at, updated_at, created_at) DESC NULLS LAST,
                id DESC
        ) AS rn
    FROM public.school_bus_sync_checkpoint
    WHERE is_deleted = false
)
UPDATE public.school_bus_sync_checkpoint checkpoint
SET is_deleted = true,
    is_active = false,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
FROM ranked
WHERE checkpoint.id = ranked.id
  AND ranked.rn > 1;

DROP INDEX IF EXISTS public.idx_school_bus_sync_checkpoint_code_lookup;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_sync_checkpoint_code_active
    ON public.school_bus_sync_checkpoint(sync_code)
    WHERE is_deleted = false;
