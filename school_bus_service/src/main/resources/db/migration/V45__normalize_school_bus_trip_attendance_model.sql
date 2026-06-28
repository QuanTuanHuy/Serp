-- Normalize School Bus trip execution and attendance model.
-- Attendance now belongs to a trip student row; trip/student/route can be
-- derived from trip_student and route_stop.

DROP INDEX IF EXISTS public.idx_trip_execution_tenant_date_status;

ALTER TABLE IF EXISTS public.school_bus_trip_execution
    DROP COLUMN IF EXISTS service_date,
    DROP COLUMN IF EXISTS route_direction,
    DROP COLUMN IF EXISTS bus_id,
    DROP COLUMN IF EXISTS driver_id,
    DROP COLUMN IF EXISTS attendant_id,
    DROP COLUMN IF EXISTS route_geometry_path,
    DROP COLUMN IF EXISTS start_location_type,
    DROP COLUMN IF EXISTS start_school_id,
    DROP COLUMN IF EXISTS start_depot_id,
    DROP COLUMN IF EXISTS end_location_type,
    DROP COLUMN IF EXISTS end_school_id,
    DROP COLUMN IF EXISTS end_depot_id;

ALTER TABLE IF EXISTS public.school_bus_attendance
    ADD COLUMN IF NOT EXISTS trip_student_id bigint;

UPDATE public.school_bus_attendance attendance
SET trip_student_id = trip_student.id
FROM public.school_bus_trip_student trip_student
LEFT JOIN public.school_bus_student_subscription subscription
       ON subscription.id = trip_student.subscription_id
WHERE attendance.trip_student_id IS NULL
  AND attendance.trip_id = trip_student.trip_id
  AND trip_student.is_deleted = false
  AND (
      trip_student.student_id = attendance.student_id
      OR subscription.student_id = attendance.student_id
  );

DROP INDEX IF EXISTS public.idx_school_bus_trip_student_lookup;

ALTER TABLE IF EXISTS public.school_bus_trip_student
    DROP COLUMN IF EXISTS student_id;

CREATE INDEX IF NOT EXISTS idx_school_bus_trip_student_lookup
    ON public.school_bus_trip_student (trip_id, subscription_id)
    WHERE is_deleted = false;

ALTER TABLE IF EXISTS public.school_bus_attendance
    ALTER COLUMN trip_student_id SET NOT NULL,
    DROP COLUMN IF EXISTS route_id,
    DROP COLUMN IF EXISTS student_id,
    DROP COLUMN IF EXISTS trip_id;

CREATE INDEX IF NOT EXISTS idx_school_bus_attendance_trip_student
    ON public.school_bus_attendance (trip_student_id, recorded_at DESC)
    WHERE is_deleted = false;
