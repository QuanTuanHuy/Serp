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
    order_code VARCHAR(255),
    customer_order_code VARCHAR(255),
    last_known_status VARCHAR(255),
    origin_post_office_code VARCHAR(255),
    destination_post_office_code VARCHAR(255),
    total_weight_snapshot DOUBLE PRECISION,
    total_volume_snapshot DOUBLE PRECISION,
    scan_out_time TIMESTAMP WITHOUT TIME ZONE,
    scan_in_time TIMESTAMP WITHOUT TIME ZONE,
    tenant_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uq_handover_manifest_order UNIQUE (manifest_id, order_id),
    CONSTRAINT fk_handover_manifest_orders_manifest
        FOREIGN KEY (manifest_id) REFERENCES handover_manifests(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_handover_manifest_orders_manifest
    ON handover_manifest_orders (manifest_id);
CREATE INDEX IF NOT EXISTS idx_handover_manifest_orders_order
    ON handover_manifest_orders (order_id);
CREATE INDEX IF NOT EXISTS idx_handover_manifest_orders_scan_in
    ON handover_manifest_orders (scan_in_time);
CREATE INDEX IF NOT EXISTS idx_handover_manifest_orders_tenant_order_id
    ON handover_manifest_orders (tenant_id, order_id);
CREATE INDEX IF NOT EXISTS idx_handover_manifest_orders_tenant_order_code_lower
    ON handover_manifest_orders (tenant_id, lower(order_code));
