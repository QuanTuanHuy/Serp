ALTER TABLE post_office_handover_manifests
    ADD COLUMN IF NOT EXISTS vehicle_id BIGINT,
    ADD COLUMN IF NOT EXISTS route_id BIGINT,
    ADD COLUMN IF NOT EXISTS planned_departure_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS planned_arrival_at TIMESTAMP WITHOUT TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_po_handover_manifests_vehicle_id
    ON post_office_handover_manifests (vehicle_id);

CREATE INDEX IF NOT EXISTS idx_po_handover_manifests_route_id
    ON post_office_handover_manifests (route_id);
