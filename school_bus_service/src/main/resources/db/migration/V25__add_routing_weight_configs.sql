-- ============================================================
-- V25 — Seed Routing Weight Parameters
-- ============================================================

INSERT INTO school_bus_app_config (
    config_code, config_name, config_description, config_type, config_value,
    is_active, is_deleted, created_by, updated_by
)
SELECT config_code, config_name, config_description, config_type, config_value, TRUE, FALSE, 'SYSTEM', 'SYSTEM'
FROM (
    SELECT 'ROUTING_WEIGHT_DISTANCE' AS config_code, 'Weight Distance' AS config_name, 'Weight for total distance in objective function' AS config_description, 'DECIMAL' AS config_type, '1.0' AS config_value UNION ALL
    SELECT 'ROUTING_WEIGHT_DURATION', 'Weight Duration', 'Weight for total duration in objective function', 'DECIMAL', '1.0' UNION ALL
    SELECT 'ROUTING_WEIGHT_ROUTE_COUNT', 'Weight Route Count', 'Weight for the number of routes in objective function', 'DECIMAL', '10.0' UNION ALL
    SELECT 'ROUTING_WEIGHT_UNASSIGNED', 'Weight Unassigned Students', 'Weight for unassigned students in objective function', 'DECIMAL', '1000.0' UNION ALL
    SELECT 'ROUTING_WEIGHT_WAIT_TIME', 'Weight Wait Time', 'Weight for total student wait time in objective function', 'DECIMAL', '0.5' UNION ALL
    SELECT 'ROUTING_WEIGHT_BLOCKING_ISSUE', 'Weight Blocking Issues', 'Weight for blocking issue occurrences in objective function', 'DECIMAL', '10000.0' UNION ALL
    SELECT 'ROUTING_WEIGHT_WARNING_ISSUE', 'Weight Warning Issues', 'Weight for warning issue occurrences in objective function', 'DECIMAL', '50.0' UNION ALL
    SELECT 'ROUTING_WEIGHT_CAPACITY_EXCESS', 'Weight Capacity Excess', 'Weight for excess student capacity on a route in objective function', 'DECIMAL', '10000.0' UNION ALL
    SELECT 'ROUTING_WEIGHT_LOAD_BALANCE', 'Weight Load Balance', 'Weight for load imbalance penalty in objective function', 'DECIMAL', '2.0'
) c
WHERE NOT EXISTS (
    SELECT 1 FROM school_bus_app_config WHERE config_code = c.config_code AND is_deleted = false
);
