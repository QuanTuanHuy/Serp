-- ============================================================================
-- V33: Remove School Schedule From School Bus Schema
-- ============================================================================

-- 1. Drop indexes that depend on school_schedule_id
DROP INDEX IF EXISTS public.idx_request_student_schedule;
DROP INDEX IF EXISTS public.idx_planning_session_context;
DROP INDEX IF EXISTS public.idx_planning_session_context_lookup;
DROP INDEX IF EXISTS public.idx_school_schedule_day_lookup;
DROP INDEX IF EXISTS public.idx_school_schedule_tenant_deleted;
DROP INDEX IF EXISTS public.idx_school_schedule_school;
DROP INDEX IF EXISTS public.idx_school_bus_schedule_code_lookup;

-- 2. Drop schedule-related columns
ALTER TABLE public.school_bus_request_student
    DROP COLUMN IF EXISTS school_schedule_id;

ALTER TABLE public.school_bus_student_subscription
    DROP COLUMN IF EXISTS school_schedule_id;

ALTER TABLE public.school_bus_student_subscription_history
    DROP COLUMN IF EXISTS old_school_schedule_id,
    DROP COLUMN IF EXISTS new_school_schedule_id;

ALTER TABLE public.school_bus_route_plan
    DROP COLUMN IF EXISTS school_schedule_id;

ALTER TABLE public.school_bus_route_planning_session
    DROP COLUMN IF EXISTS school_schedule_id;

-- 3. Drop schedule tables
DROP TABLE IF EXISTS public.school_bus_school_schedule_day CASCADE;
DROP TABLE IF EXISTS public.school_bus_school_schedule CASCADE;

-- 4. Recreate planning session context indexes without schedule
CREATE INDEX IF NOT EXISTS idx_planning_session_context
    ON public.school_bus_route_planning_session
        (tenant_id, school_id, service_date, route_direction, is_deleted);

CREATE INDEX IF NOT EXISTS idx_planning_session_context_lookup
    ON public.school_bus_route_planning_session
        (tenant_id, school_id, service_date, route_direction)
    WHERE is_deleted = false;
