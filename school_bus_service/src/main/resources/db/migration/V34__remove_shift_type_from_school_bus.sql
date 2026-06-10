-- V34: Remove shift_type column from school_bus_route_plan and school_bus_trip_execution
-- Phase 2.1: shift_type is replaced by routeDirection (OUTBOUND/RETURN) semantics.
-- OUTBOUND = morning trip to school, RETURN = afternoon trip home.
-- No index on shift_type was created in any prior migration.

ALTER TABLE public.school_bus_route_plan
    DROP COLUMN IF EXISTS shift_type;

ALTER TABLE public.school_bus_trip_execution
    DROP COLUMN IF EXISTS shift_type;
