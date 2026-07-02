-- Normalize School Bus planning/request/subscription route model.
-- This migration is intentionally defensive because dev databases may already
-- have some manual cleanup applied, while Flyway schema history is preserved.

ALTER TABLE IF EXISTS public.school_bus_transport_request
    DROP COLUMN IF EXISTS school_id;

DROP INDEX IF EXISTS public.idx_subscription_tenant_school_status;

ALTER TABLE IF EXISTS public.school_bus_student_subscription
    DROP COLUMN IF EXISTS school_id;

DROP INDEX IF EXISTS public.idx_route_plan_start_school;
DROP INDEX IF EXISTS public.idx_route_plan_end_school;
DROP INDEX IF EXISTS public.idx_route_plan_start_depot;
DROP INDEX IF EXISTS public.idx_route_plan_end_depot;
DROP INDEX IF EXISTS public.idx_route_plan_conflict;
DROP INDEX IF EXISTS public.idx_route_plan_selected_bus;

ALTER TABLE IF EXISTS public.school_bus_route_plan
    ALTER COLUMN planning_session_id SET NOT NULL,
    DROP COLUMN IF EXISTS school_id,
    DROP COLUMN IF EXISTS service_date,
    DROP COLUMN IF EXISTS route_direction,
    DROP COLUMN IF EXISTS start_location_type,
    DROP COLUMN IF EXISTS start_school_id,
    DROP COLUMN IF EXISTS start_depot_id,
    DROP COLUMN IF EXISTS end_location_type,
    DROP COLUMN IF EXISTS end_school_id,
    DROP COLUMN IF EXISTS end_depot_id,
    DROP COLUMN IF EXISTS selected_bus_id;

ALTER TABLE IF EXISTS public.school_bus_route_stop
    ADD COLUMN IF NOT EXISTS location_id bigint;

UPDATE public.school_bus_route_stop
SET location_id = CASE
    WHEN location_type = 'PICKUP_POINT' THEN pickup_point_id
    WHEN location_type = 'SCHOOL' THEN school_id
    WHEN location_type = 'DEPOT' THEN depot_id
    ELSE location_id
END
WHERE location_id IS NULL
  AND (
      pickup_point_id IS NOT NULL
      OR school_id IS NOT NULL
      OR depot_id IS NOT NULL
  );

ALTER TABLE IF EXISTS public.school_bus_route_stop
    ALTER COLUMN location_id SET NOT NULL,
    DROP COLUMN IF EXISTS pickup_point_id,
    DROP COLUMN IF EXISTS school_id,
    DROP COLUMN IF EXISTS depot_id;

DROP INDEX IF EXISTS public.idx_route_plan_student_student_lookup;
DROP INDEX IF EXISTS public.idx_route_plan_student_unique_lookup;
DROP INDEX IF EXISTS public.uk_route_plan_student_active;

ALTER TABLE IF EXISTS public.school_bus_route_plan_student
    DROP COLUMN IF EXISTS student_id;

CREATE INDEX IF NOT EXISTS idx_route_plan_student_subscription_lookup
    ON public.school_bus_route_plan_student (subscription_id, is_deleted);

CREATE UNIQUE INDEX IF NOT EXISTS uk_route_plan_student_active
    ON public.school_bus_route_plan_student (route_id, subscription_id)
    WHERE is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_route_stop_location_lookup
    ON public.school_bus_route_stop (location_type, location_id)
    WHERE is_deleted = false;
