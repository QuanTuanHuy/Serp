ALTER TABLE public.school_bus_route_plan
    DROP COLUMN IF EXISTS assigned_bus_capacity;

ALTER TABLE public.school_bus_trip_stop_log
    DROP COLUMN IF EXISTS stop_order;
