-- ============================================================
-- V19: Enhance demo simulation tables for playback engine
-- ============================================================

-- Add new columns to school_bus_demo_session
ALTER TABLE school_bus_demo_session
    ADD COLUMN IF NOT EXISTS duration_seconds INTEGER,
    ADD COLUMN IF NOT EXISTS auto_advance_stops BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS auto_attendance BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS last_tick_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_event_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS error_message TEXT;

-- Update CHECK constraint to include ERROR status
ALTER TABLE school_bus_demo_session DROP CONSTRAINT IF EXISTS chk_demo_session_status;
ALTER TABLE school_bus_demo_session
    ADD CONSTRAINT chk_demo_session_status
        CHECK (status IN ('READY', 'RUNNING', 'PAUSED', 'COMPLETED', 'STOPPED', 'ERROR'));

-- Partial unique index: only one RUNNING/PAUSED session per trip at a time
CREATE UNIQUE INDEX IF NOT EXISTS idx_demo_session_active_per_trip
    ON school_bus_demo_session (trip_id, tenant_id)
    WHERE status IN ('RUNNING', 'PAUSED') AND is_deleted = FALSE;

-- Index on event log for fast event retrieval by session
CREATE INDEX IF NOT EXISTS idx_demo_event_log_session_time
    ON school_bus_demo_event_log (demo_session_id, event_time DESC)
    WHERE is_deleted = FALSE;
