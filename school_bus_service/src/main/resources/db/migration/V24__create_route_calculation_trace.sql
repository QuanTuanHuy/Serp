-- ============================================================
-- V24 — Create Route Calculation Trace
-- ============================================================

CREATE TABLE IF NOT EXISTS school_bus_route_calculation_trace
(
    id                    BIGSERIAL PRIMARY KEY,

    route_plan_id         BIGINT NOT NULL,
    planning_session_id   BIGINT,
    tenant_id             BIGINT,

    calculation_type      VARCHAR(50) NOT NULL,
    calculation_status    VARCHAR(50) NOT NULL,

    input_json            JSONB,
    matrix_json           JSONB,
    timeline_json         JSONB,
    issues_json           JSONB,
    config_snapshot_json  JSONB,
    source_summary        VARCHAR(255),

    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),

    is_active             BOOLEAN DEFAULT TRUE NOT NULL,
    is_deleted            BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_route_calc_trace_route
    ON school_bus_route_calculation_trace(route_plan_id)
    WHERE is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_route_calc_trace_session
    ON school_bus_route_calculation_trace(planning_session_id)
    WHERE is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_route_calc_trace_created_at
    ON school_bus_route_calculation_trace(created_at);

ALTER TABLE school_bus_route_calculation_trace
    ADD CONSTRAINT fk_route_calc_trace_route_plan
    FOREIGN KEY (route_plan_id)
    REFERENCES school_bus_route_plan(id);
