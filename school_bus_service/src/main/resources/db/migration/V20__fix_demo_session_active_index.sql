-- ============================================================
-- V20: Include READY status in active demo session unique index
-- Policy: only one active (READY/RUNNING/PAUSED) session per trip
-- ============================================================

DROP INDEX IF EXISTS idx_demo_session_active_per_trip;

CREATE UNIQUE INDEX idx_demo_session_active_per_trip
    ON school_bus_demo_session (trip_id, tenant_id)
    WHERE status IN ('READY', 'RUNNING', 'PAUSED') AND is_deleted = FALSE;
