-- Author: Nguyen The Anh
-- Description: Add trip type to separate pickup and delivery routes.

ALTER TABLE trips
    ADD COLUMN IF NOT EXISTS trip_type VARCHAR(20) NOT NULL DEFAULT 'PICKUP';

ALTER TABLE trips
    DROP CONSTRAINT IF EXISTS trips_trip_type_check;

ALTER TABLE trips
    ADD CONSTRAINT trips_trip_type_check
        CHECK (trip_type IN ('PICKUP', 'DELIVERY'));

DROP INDEX IF EXISTS uq_trips_active_courier_shift;
DROP INDEX IF EXISTS uq_trips_active_vehicle_shift;

CREATE UNIQUE INDEX IF NOT EXISTS uq_trips_active_courier_shift
    ON trips (tenant_id, trip_type, trip_date, shift, courier_staff_id)
    WHERE status IN ('PLANNED', 'IN_PROGRESS');

CREATE UNIQUE INDEX IF NOT EXISTS uq_trips_active_vehicle_shift
    ON trips (tenant_id, trip_type, trip_date, shift, vehicle_id)
    WHERE vehicle_id IS NOT NULL
      AND status IN ('PLANNED', 'IN_PROGRESS');

ALTER TABLE trip_order
    ADD COLUMN IF NOT EXISTS scan_out_time TIMESTAMP;
