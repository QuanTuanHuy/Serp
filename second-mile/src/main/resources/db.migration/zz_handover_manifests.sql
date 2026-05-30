/*
Author: Nguyen The Anh
Description: Part of Serp Project - Handover manifests for post office to hub checkpointing
*/

CREATE TABLE IF NOT EXISTS handover_manifests (
    id BIGSERIAL PRIMARY KEY,
    manifest_code VARCHAR(100) NOT NULL,
    origin_post_office_code VARCHAR(255) NOT NULL,
    target_hub_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'CREATED',
    tenant_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_handover_manifests_tenant_code
    ON handover_manifests (tenant_id, lower(manifest_code));

CREATE INDEX IF NOT EXISTS idx_handover_manifests_target_hub
    ON handover_manifests (target_hub_id);

CREATE TABLE IF NOT EXISTS handover_manifest_orders (
    id BIGSERIAL PRIMARY KEY,
    manifest_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    scan_out_time TIMESTAMP WITHOUT TIME ZONE,
    scan_in_time TIMESTAMP WITHOUT TIME ZONE,
    tenant_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uq_handover_manifest_order UNIQUE (manifest_id, order_id),
    CONSTRAINT fk_handover_manifest_orders_manifest
        FOREIGN KEY (manifest_id) REFERENCES handover_manifests(id) ON DELETE CASCADE,
    CONSTRAINT fk_handover_manifest_orders_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_handover_manifest_orders_manifest
    ON handover_manifest_orders (manifest_id);
CREATE INDEX IF NOT EXISTS idx_handover_manifest_orders_order
    ON handover_manifest_orders (order_id);
CREATE INDEX IF NOT EXISTS idx_handover_manifest_orders_scan_in
    ON handover_manifest_orders (scan_in_time);
