ALTER TABLE public.school_bus_route_planning_session
    DROP CONSTRAINT IF EXISTS chk_planning_session_method;

ALTER TABLE public.school_bus_route_planning_session
    DROP COLUMN IF EXISTS planning_method,
    DROP COLUMN IF EXISTS generated_at,
    DROP COLUMN IF EXISTS generated_by;

ALTER TABLE public.school_bus_route_plan
    DROP COLUMN IF EXISTS route_generation_method;
