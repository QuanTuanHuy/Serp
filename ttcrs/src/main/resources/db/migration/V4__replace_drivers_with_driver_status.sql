-- Remove all transport plan data that references the old drivers table
DELETE FROM transport_plan_stops;
UPDATE requests SET transport_plan_id = NULL WHERE transport_plan_id IS NOT NULL;
DELETE FROM transport_plans;

-- Drop the foreign key from transport_plans to drivers, then drop drivers table
ALTER TABLE transport_plans DROP CONSTRAINT IF EXISTS transport_plans_driver_id_foreign;
DROP TABLE IF EXISTS drivers;

-- driver_status: stores TTCRS operational status for Account users with TTCRS_DRIVER role.
-- user_id references a user in the Account service (no FK constraint — cross-service).
-- Default status is AVAILABLE; rows are upserted only when status is explicitly set.
CREATE TABLE IF NOT EXISTS driver_status (
    user_id              BIGINT      NOT NULL,
    tenant_id            BIGINT      NOT NULL,
    status               VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE'
        CHECK (status IN ('AVAILABLE', 'IN_USE', 'OFF')),
    last_updated_stamp   TIMESTAMP,
    CONSTRAINT driver_status_pkey PRIMARY KEY (user_id, tenant_id)
);
