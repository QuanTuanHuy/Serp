-- ============================================================
-- V26 — Make route_plan_id nullable in school_bus_route_calculation_trace
-- ============================================================

ALTER TABLE school_bus_route_calculation_trace ALTER COLUMN route_plan_id DROP NOT NULL;
