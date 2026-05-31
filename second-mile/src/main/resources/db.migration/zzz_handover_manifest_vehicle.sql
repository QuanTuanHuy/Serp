/*
Author: QuanTuanHuy
Description: Part of Serp Project - Link handover manifests to transport vehicle and route
*/

ALTER TABLE handover_manifests
    ADD COLUMN IF NOT EXISTS vehicle_id BIGINT,
    ADD COLUMN IF NOT EXISTS route_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_handover_manifests_vehicle_id
    ON handover_manifests (vehicle_id);

CREATE INDEX IF NOT EXISTS idx_handover_manifests_route_id
    ON handover_manifests (route_id);

COMMENT ON COLUMN handover_manifests.vehicle_id IS 'Vehicle assigned to transport orders from post office to hub';
COMMENT ON COLUMN handover_manifests.route_id IS 'Optional hub-to-post-office route used for this collection run';
