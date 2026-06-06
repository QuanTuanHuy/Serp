-- ============================================================
-- V22 — Create App Config for School Bus Routing Parameters
-- ============================================================

CREATE TABLE IF NOT EXISTS school_bus_app_config
(
    id                 BIGSERIAL PRIMARY KEY,
    tenant_id          BIGINT NOT NULL,

    config_code        VARCHAR(100) NOT NULL,
    config_name        VARCHAR(255) NOT NULL,
    config_description TEXT,
    config_type        VARCHAR(50) NOT NULL,
    config_value       VARCHAR(1000) NOT NULL,

    is_active          BOOLEAN DEFAULT TRUE NOT NULL,
    is_deleted         BOOLEAN DEFAULT FALSE NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by         VARCHAR(100),
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by         VARCHAR(100),

    CONSTRAINT uk_school_bus_app_config_tenant_code UNIQUE (tenant_id, config_code)
);

CREATE INDEX IF NOT EXISTS idx_school_bus_app_config_tenant_code_active
    ON school_bus_app_config (tenant_id, config_code, is_active, is_deleted);

-- Seed default parameters for existing tenants + tenant 1 (default)
-- Parameters to seed:
-- 1. ROUTING_AVERAGE_SPEED_KMPH (DECIMAL, default 25.0)
-- 2. ROUTING_DWELL_TIME_MINUTES (INTEGER, default 2)
-- 3. ROUTING_ROAD_FACTOR (DECIMAL, default 1.3)
-- 4. ROUTING_OSRM_ENABLED (BOOLEAN, default true)

WITH target_tenants AS (
    SELECT DISTINCT tenant_id FROM school_bus_school
    UNION
    SELECT 1::BIGINT AS tenant_id
)
INSERT INTO school_bus_app_config (
    tenant_id, config_code, config_name, config_description, config_type, config_value,
    is_active, is_deleted, created_by, updated_by
)
SELECT 
    t.tenant_id,
    c.config_code,
    c.config_name,
    c.config_description,
    c.config_type,
    c.config_value,
    TRUE,
    FALSE,
    'SYSTEM',
    'SYSTEM'
FROM target_tenants t
CROSS JOIN (
    SELECT 'ROUTING_AVERAGE_SPEED_KMPH' AS config_code, 'Average Speed (km/h)' AS config_name, 'Average vehicle speed in km/h for routing estimation' AS config_description, 'DECIMAL' AS config_type, '25.0' AS config_value
    UNION ALL
    SELECT 'ROUTING_DWELL_TIME_MINUTES', 'Dwell Time (minutes)', 'Default dwell time in minutes at each pickup/drop-off stop', 'INTEGER', '2'
    UNION ALL
    SELECT 'ROUTING_ROAD_FACTOR', 'Road Factor', 'Multiplier to adjust haversine distance to estimate road distance', 'DECIMAL', '1.3'
    UNION ALL
    SELECT 'ROUTING_OSRM_ENABLED', 'OSRM Enabled', 'Flag to enable/disable external OSRM routing', 'BOOLEAN', 'true'
) c
ON CONFLICT (tenant_id, config_code) DO NOTHING;
