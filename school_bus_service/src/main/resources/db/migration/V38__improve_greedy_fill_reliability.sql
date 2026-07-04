ALTER TABLE public.school_bus_route_plan
    ADD COLUMN IF NOT EXISTS geometry_source varchar(30) NOT NULL DEFAULT 'UNKNOWN';

UPDATE public.school_bus_route_plan
SET geometry_source = 'UNKNOWN'
WHERE geometry_source IS NULL;

-- Keep the oldest active assignment when exact duplicates already exist.
WITH ranked_assignments AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY route_id, student_id, subscription_id
               ORDER BY id
           ) AS duplicate_rank
    FROM public.school_bus_route_plan_student
    WHERE is_deleted = false
)
UPDATE public.school_bus_route_plan_student assignment
SET is_deleted = true,
    is_active = false,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'V38_MIGRATION'
FROM ranked_assignments ranked
WHERE assignment.id = ranked.id
  AND ranked.duplicate_rank > 1;

CREATE UNIQUE INDEX IF NOT EXISTS uk_route_plan_student_active
    ON public.school_bus_route_plan_student(route_id, student_id, subscription_id)
    WHERE is_deleted = false;
