ALTER TABLE public.school_bus_trip_execution
    DROP COLUMN IF EXISTS planned_distance_km,
    DROP COLUMN IF EXISTS planned_duration_min,
    DROP COLUMN IF EXISTS actual_distance_km,
    DROP COLUMN IF EXISTS actual_duration_min;
