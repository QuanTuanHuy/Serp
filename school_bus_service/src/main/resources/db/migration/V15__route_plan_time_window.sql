-- ============================================================
-- V15 — Route Plan time window for assignment conflict check
-- Adds planned_start_time / planned_end_time to route_plan.
-- ============================================================

alter table school_bus_route_plan
    add column if not exists planned_start_time time,
    add column if not exists planned_end_time   time;

-- Index for time-window conflict queries
create index if not exists idx_route_plan_conflict
    on school_bus_route_plan (tenant_id, service_date, is_deleted)
    where is_deleted = false;
