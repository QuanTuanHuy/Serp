-- ============================================================================
-- V31: Simplify School Bus Schema
-- ============================================================================
-- Context: After demo review, module deemed overly complex. Simplifying to
-- student-centric flow: register → approve → assign to route → dispatch → trip.
-- Removes: time window, objective score, planning issues, calculation trace,
--          pause periods, audit log, and complex DB constraints.
-- Data: All tables will be truncated before this migration runs.
-- ============================================================================

-- ════════════════════════════════════════════════════════════════════════════════
-- STEP 1: Drop tables no longer used
-- ════════════════════════════════════════════════════════════════════════════════

DROP TABLE IF EXISTS public.school_bus_route_calculation_trace CASCADE;
DROP TABLE IF EXISTS public.school_bus_route_planning_issue CASCADE;
DROP TABLE IF EXISTS public.school_bus_school_pickup_point_window CASCADE;
DROP TABLE IF EXISTS public.school_bus_subscription_pause_period CASCADE;
DROP TABLE IF EXISTS public.school_bus_audit_log CASCADE;

-- ════════════════════════════════════════════════════════════════════════════════
-- STEP 2: Truncate app_config (routing weights, objective configs no longer used)
-- ════════════════════════════════════════════════════════════════════════════════

TRUNCATE TABLE public.school_bus_app_config RESTART IDENTITY CASCADE;

-- ════════════════════════════════════════════════════════════════════════════════
-- STEP 3: Add new columns to route_plan_student (before dropping old ones)
-- ════════════════════════════════════════════════════════════════════════════════

ALTER TABLE public.school_bus_route_plan_student
    ADD COLUMN IF NOT EXISTS pickup_stop_id bigint,
    ADD COLUMN IF NOT EXISTS dropoff_stop_id bigint;

-- ════════════════════════════════════════════════════════════════════════════════
-- STEP 4: Drop old indexes and columns from route_plan_student
-- ════════════════════════════════════════════════════════════════════════════════

DROP INDEX IF EXISTS public.uk_route_plan_student_action;
DROP INDEX IF EXISTS public.idx_route_plan_student_stop;

ALTER TABLE public.school_bus_route_plan_student
    DROP COLUMN IF EXISTS service_action,
    DROP COLUMN IF EXISTS route_stop_id,
    DROP COLUMN IF EXISTS planned_time;

-- ════════════════════════════════════════════════════════════════════════════════
-- STEP 5: Create new indexes for route_plan_student
-- ════════════════════════════════════════════════════════════════════════════════

CREATE INDEX IF NOT EXISTS idx_route_plan_student_route_lookup
    ON public.school_bus_route_plan_student(route_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_route_plan_student_student_lookup
    ON public.school_bus_route_plan_student(student_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_route_plan_student_subscription_lookup
    ON public.school_bus_route_plan_student(subscription_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_route_plan_student_pickup_stop
    ON public.school_bus_route_plan_student(pickup_stop_id, is_deleted)
    WHERE pickup_stop_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_route_plan_student_dropoff_stop
    ON public.school_bus_route_plan_student(dropoff_stop_id, is_deleted)
    WHERE dropoff_stop_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_route_plan_student_unique_lookup
    ON public.school_bus_route_plan_student(route_id, student_id, subscription_id, is_deleted);

-- ════════════════════════════════════════════════════════════════════════════════
-- STEP 6: Drop columns from various tables
-- ════════════════════════════════════════════════════════════════════════════════

-- 6.1 school_bus_pickup_point: drop zone_code
ALTER TABLE public.school_bus_pickup_point
    DROP COLUMN IF EXISTS zone_code;

-- 6.2 school_bus_student: drop emergency_contact fields
ALTER TABLE public.school_bus_student
    DROP COLUMN IF EXISTS emergency_contact_name,
    DROP COLUMN IF EXISTS emergency_contact_phone;

-- 6.3 school_bus_route_plan: drop scoring/issue columns
ALTER TABLE public.school_bus_route_plan
    DROP COLUMN IF EXISTS quality_score,
    DROP COLUMN IF EXISTS issue_count,
    DROP COLUMN IF EXISTS blocking_issue_count,
    DROP COLUMN IF EXISTS estimated_cost;

-- 6.4 school_bus_route_planning_session: drop constraint_json
ALTER TABLE public.school_bus_route_planning_session
    DROP COLUMN IF EXISTS constraint_json;

-- 6.5 school_bus_route_stop: drop planned arrival/departure times
ALTER TABLE public.school_bus_route_stop
    DROP COLUMN IF EXISTS planned_arrival_time,
    DROP COLUMN IF EXISTS planned_departure_time;

-- ════════════════════════════════════════════════════════════════════════════════
-- STEP 7: Drop all CHECK constraints on school_bus_% tables
-- ════════════════════════════════════════════════════════════════════════════════

DO $$
DECLARE
    r record;
BEGIN
    FOR r IN
        SELECT conrelid::regclass AS table_name, conname
        FROM pg_constraint
        WHERE connamespace = 'public'::regnamespace
          AND conrelid::regclass::text LIKE 'school_bus_%'
          AND contype = 'c'
    LOOP
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT IF EXISTS %I', r.table_name, r.conname);
    END LOOP;
END $$;

-- ════════════════════════════════════════════════════════════════════════════════
-- STEP 8: Drop all FK constraints on school_bus_% tables
-- ════════════════════════════════════════════════════════════════════════════════

DO $$
DECLARE
    r record;
BEGIN
    FOR r IN
        SELECT conrelid::regclass AS table_name, conname
        FROM pg_constraint
        WHERE connamespace = 'public'::regnamespace
          AND conrelid::regclass::text LIKE 'school_bus_%'
          AND contype = 'f'
    LOOP
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT IF EXISTS %I', r.table_name, r.conname);
    END LOOP;
END $$;

-- ════════════════════════════════════════════════════════════════════════════════
-- STEP 9: Drop UNIQUE indexes/constraints on school_bus_% tables
-- ════════════════════════════════════════════════════════════════════════════════

-- 9.1 Drop unique indexes (CREATE UNIQUE INDEX style)
DROP INDEX IF EXISTS public.uk_school_bus_school_tenant_code;
DROP INDEX IF EXISTS public.uk_school_bus_parent_tenant_user;
DROP INDEX IF EXISTS public.uk_school_bus_driver_tenant_user;
DROP INDEX IF EXISTS public.uk_school_bus_driver_tenant_license;
DROP INDEX IF EXISTS public.uk_school_bus_attendant_tenant_user;
DROP INDEX IF EXISTS public.uk_pickup_point_tenant_code;
DROP INDEX IF EXISTS public.uk_school_bus_student_tenant_code;
DROP INDEX IF EXISTS public.uk_school_bus_bus_tenant_plate;
DROP INDEX IF EXISTS public.uk_school_bus_depot_tenant_code;
DROP INDEX IF EXISTS public.uk_request_student_active;
DROP INDEX IF EXISTS public.uk_school_bus_subscription_code;
DROP INDEX IF EXISTS public.uk_school_schedule_code;
DROP INDEX IF EXISTS public.uk_school_schedule_default_active;
DROP INDEX IF EXISTS public.uk_school_pickup_point_active;
DROP INDEX IF EXISTS public.uk_school_schedule_day_active;
DROP INDEX IF EXISTS public.uk_route_assignment_active;
DROP INDEX IF EXISTS public.uk_school_bus_trip_history_route;
DROP INDEX IF EXISTS public.uk_school_bus_trip_code;
DROP INDEX IF EXISTS public.uk_school_bus_trip_stop_log;
DROP INDEX IF EXISTS public.uk_school_bus_trip_student;
DROP INDEX IF EXISTS public.uk_planning_session_context_active;
DROP INDEX IF EXISTS public.uk_route_plan_student_action; -- already dropped above, safe to repeat
DROP INDEX IF EXISTS public.uk_school_bus_app_config_code_active;
DROP INDEX IF EXISTS public.uk_school_bus_route_stop_order;
DROP INDEX IF EXISTS public.uk_school_bus_user_account_user;
DROP INDEX IF EXISTS public.uk_school_bus_user_keycloak;
DROP INDEX IF EXISTS public.uk_school_bus_user_tenant_email;
DROP INDEX IF EXISTS public.uk_school_bus_sync_checkpoint_code;

-- 9.2 Drop table-level unique constraints (ALTER TABLE ... ADD CONSTRAINT style)
ALTER TABLE public.school_bus_code_sequence
    DROP CONSTRAINT IF EXISTS uk_school_bus_code_sequence_tenant_key;

-- ════════════════════════════════════════════════════════════════════════════════
-- STEP 10: Create replacement normal indexes for important lookups
-- ════════════════════════════════════════════════════════════════════════════════

-- School lookup by code
CREATE INDEX IF NOT EXISTS idx_school_bus_school_tenant_code_lookup
    ON public.school_bus_school(tenant_id, code)
    WHERE code IS NOT NULL AND is_deleted = false;

-- Student lookup by code
CREATE INDEX IF NOT EXISTS idx_school_bus_student_tenant_code_lookup
    ON public.school_bus_student(tenant_id, student_code)
    WHERE student_code IS NOT NULL AND is_deleted = false;

-- Bus lookup by plate number
CREATE INDEX IF NOT EXISTS idx_school_bus_bus_tenant_plate_lookup
    ON public.school_bus_bus(tenant_id, plate_number)
    WHERE is_deleted = false;

-- Trip lookup by code
CREATE INDEX IF NOT EXISTS idx_school_bus_trip_code_lookup
    ON public.school_bus_trip_execution(tenant_id, trip_code)
    WHERE is_deleted = false;

-- User shadow table lookups
CREATE INDEX IF NOT EXISTS idx_school_bus_user_account_user_lookup
    ON public.school_bus_user(account_user_id)
    WHERE is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_school_bus_user_keycloak_lookup
    ON public.school_bus_user(keycloak_id)
    WHERE keycloak_id IS NOT NULL AND is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_school_bus_user_tenant_email_lookup
    ON public.school_bus_user(tenant_id, email)
    WHERE is_deleted = false;

-- Subscription code lookup
CREATE INDEX IF NOT EXISTS idx_school_bus_subscription_code_lookup
    ON public.school_bus_student_subscription(tenant_id, subscription_code)
    WHERE is_deleted = false;

-- Depot code lookup
CREATE INDEX IF NOT EXISTS idx_school_bus_depot_tenant_code_lookup
    ON public.school_bus_depot(tenant_id, code)
    WHERE code IS NOT NULL AND is_deleted = false;

-- Parent tenant user lookup
CREATE INDEX IF NOT EXISTS idx_school_bus_parent_tenant_user_lookup
    ON public.school_bus_parent_profile(tenant_id, user_id)
    WHERE is_deleted = false;

-- Driver tenant user lookup
CREATE INDEX IF NOT EXISTS idx_school_bus_driver_tenant_user_lookup
    ON public.school_bus_driver_profile(tenant_id, user_id)
    WHERE is_deleted = false;

-- Attendant tenant user lookup
CREATE INDEX IF NOT EXISTS idx_school_bus_attendant_tenant_user_lookup
    ON public.school_bus_attendant_profile(tenant_id, user_id)
    WHERE is_deleted = false;

-- Pickup point code lookup
CREATE INDEX IF NOT EXISTS idx_school_bus_pickup_point_code_lookup
    ON public.school_bus_pickup_point(tenant_id, code)
    WHERE code IS NOT NULL AND is_deleted = false;

-- Schedule code lookup
CREATE INDEX IF NOT EXISTS idx_school_bus_schedule_code_lookup
    ON public.school_bus_school_schedule(tenant_id, schedule_code)
    WHERE schedule_code IS NOT NULL AND is_deleted = false;

-- Route stop order lookup (was previously unique, now normal)
CREATE INDEX IF NOT EXISTS idx_school_bus_route_stop_order_lookup
    ON public.school_bus_route_stop(route_id, stop_order)
    WHERE is_deleted = false;

-- Route assignment active lookup
CREATE INDEX IF NOT EXISTS idx_school_bus_route_assignment_active_lookup
    ON public.school_bus_route_assignment(route_id, status)
    WHERE is_deleted = false;

-- Planning session context lookup
CREATE INDEX IF NOT EXISTS idx_planning_session_context_lookup
    ON public.school_bus_route_planning_session(tenant_id, school_id, school_schedule_id, service_date, route_direction)
    WHERE is_deleted = false;

-- School pickup point link lookup
CREATE INDEX IF NOT EXISTS idx_school_pickup_point_link_lookup
    ON public.school_bus_school_pickup_point(school_id, pickup_point_id)
    WHERE is_deleted = false;

-- Schedule day lookup
CREATE INDEX IF NOT EXISTS idx_school_schedule_day_lookup
    ON public.school_bus_school_schedule_day(school_schedule_id, day_of_week)
    WHERE is_deleted = false;

-- Trip stop log lookup
CREATE INDEX IF NOT EXISTS idx_school_bus_trip_stop_log_lookup
    ON public.school_bus_trip_stop_log(trip_id, route_stop_id)
    WHERE is_deleted = false;

-- Trip student lookup
CREATE INDEX IF NOT EXISTS idx_school_bus_trip_student_lookup
    ON public.school_bus_trip_student(trip_id, student_id)
    WHERE is_deleted = false;

-- Code sequence lookup
CREATE INDEX IF NOT EXISTS idx_school_bus_code_sequence_lookup
    ON public.school_bus_code_sequence(tenant_id, sequence_key);

-- Sync checkpoint code lookup
CREATE INDEX IF NOT EXISTS idx_school_bus_sync_checkpoint_code_lookup
    ON public.school_bus_sync_checkpoint(sync_code)
    WHERE is_deleted = false;

-- App config code lookup
CREATE INDEX IF NOT EXISTS idx_school_bus_app_config_code_lookup
    ON public.school_bus_app_config(config_code)
    WHERE is_deleted = false;

-- ════════════════════════════════════════════════════════════════════════════════
-- DONE
-- ════════════════════════════════════════════════════════════════════════════════
