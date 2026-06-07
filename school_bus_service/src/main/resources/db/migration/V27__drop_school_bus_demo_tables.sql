-- V27: Drop school bus demo tables and clean simulation_mode column
DROP TABLE IF EXISTS public.school_bus_demo_event_log;
DROP TABLE IF EXISTS public.school_bus_demo_session;

ALTER TABLE public.school_bus_trip_execution
    DROP COLUMN IF EXISTS simulation_mode;
