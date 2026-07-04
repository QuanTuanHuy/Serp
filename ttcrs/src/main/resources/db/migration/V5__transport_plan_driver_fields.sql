-- Transport plan: fields for driver execution workflow
ALTER TABLE transport_plans
    ADD COLUMN cancel_reason      TEXT,
    ADD COLUMN current_stop       INTEGER,
    ADD COLUMN actual_start_time  TIMESTAMP,
    ADD COLUMN actual_end_time    TIMESTAMP,
    ADD COLUMN total_distance     DOUBLE PRECISION,
    ADD COLUMN total_time         INTEGER;

-- Transport plan stops: track whether driver has completed evidence submission
ALTER TABLE transport_plan_stops
    ADD COLUMN is_completed BOOLEAN NOT NULL DEFAULT FALSE;
