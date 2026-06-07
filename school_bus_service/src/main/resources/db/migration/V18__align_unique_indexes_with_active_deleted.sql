-- V18: Align unique indexes to only cover active + non-deleted records.
--
-- Two indexes currently use WHERE is_deleted = false but do NOT filter on
-- is_active. Because we use soft-deactivation (is_active = false) alongside
-- soft-delete (is_deleted = true) for route stops and plan-student entries,
-- adding is_active = true ensures that deactivated records free their unique
-- slots immediately, preventing false constraint violations.
--
-- Index analysis:
--
-- 1. uk_school_bus_route_stop_order (school_bus_route_stop)
--    Constraint: per-route stop_order is unique among non-deleted stops.
--    Problem: if a stop is deactivated (is_active = false) without being
--    deleted, the old index still holds its order slot and blocks the new
--    active stop at the same position.
--    Fix: add is_active = true to the predicate.
--
-- 2. uk_route_plan_student_action (school_bus_route_plan_student)
--    Constraint: one (route, student, subscription, action) per route among
--    non-deleted entries.
--    Problem: same as above — deactivated-but-not-deleted entries would
--    prevent re-assigning the student after a soft-deactivation.
--    Fix: add is_active = true to the predicate.
--
-- Indexes kept unchanged (reason):
--   uk_school_bus_school_tenant_code   — identity master data; deleted codes stay reserved
--   uk_school_bus_bus_tenant_plate     — identity master data
--   uk_school_bus_parent_tenant_user   — identity; user link is permanent
--   uk_school_bus_driver_tenant_user   — identity
--   uk_school_bus_driver_tenant_license — identity
--   uk_school_bus_attendant_tenant_user — identity
--   uk_school_bus_student_tenant_code  — identity
--   uk_school_bus_subscription_code    — identity code; codes are not reused
--   uk_school_bus_trip_code            — identity
--   uk_school_bus_trip_student         — trip-level uniqueness; trip is immutable after creation
--   uk_school_bus_trip_stop_log        — trip-level; same reasoning
--   uk_school_bus_depot_tenant_code    — identity
--   uk_school_schedule_code            — identity code
--   uk_pickup_point_tenant_code        — identity code
--   uk_school_pickup_point_active      — junction; is_deleted = false already sufficient
--                                        (always deleted when deactivated)
--   uk_school_schedule_day_active      — schedule days always deleted when deactivated
--   uk_school_schedule_default_active  — already has is_active = true + is_deleted = false
--   uk_planning_session_context_active — status-based predicate already sufficient
--   uk_route_assignment_active         — status-based predicate already sufficient
--   uk_request_student_active          — check V11 reasoning; deleted on deactivation

-- ── 1. school_bus_route_stop: stop order ─────────────────────────────────────

DROP INDEX IF EXISTS uk_school_bus_route_stop_order;

CREATE UNIQUE INDEX uk_school_bus_route_stop_order
    ON school_bus_route_stop (route_id, stop_order)
    WHERE is_deleted = false AND is_active = true;

-- ── 2. school_bus_route_plan_student: one action per student per route ────────

DROP INDEX IF EXISTS uk_route_plan_student_action;

CREATE UNIQUE INDEX uk_route_plan_student_action
    ON school_bus_route_plan_student (route_id, student_id, subscription_id, service_action)
    WHERE is_deleted = false AND is_active = true;
