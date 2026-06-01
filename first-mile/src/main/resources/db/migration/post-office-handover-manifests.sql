/*
Author: NguyenTheAnh
Description: Part of Serp Project - Post office to hub handover manifests
*/

CREATE TABLE IF NOT EXISTS post_office_handover_manifests (
    id BIGSERIAL PRIMARY KEY,
    manifest_code VARCHAR(100) NOT NULL,
    origin_post_office_id BIGINT NOT NULL,
    origin_post_office_code VARCHAR(255) NOT NULL,
    target_hub_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'CREATED',
    dispatched_at TIMESTAMP WITHOUT TIME ZONE,
    inbound_confirmed_at TIMESTAMP WITHOUT TIME ZONE,
    seal_code VARCHAR(100),
    note TEXT,
    tenant_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_po_handover_manifests_tenant_code
    ON post_office_handover_manifests (tenant_id, lower(manifest_code));

CREATE INDEX IF NOT EXISTS idx_po_handover_manifests_origin_po
    ON post_office_handover_manifests (origin_post_office_id);

CREATE INDEX IF NOT EXISTS idx_po_handover_manifests_target_hub
    ON post_office_handover_manifests (target_hub_id);

CREATE INDEX IF NOT EXISTS idx_po_handover_manifests_status
    ON post_office_handover_manifests (status);

CREATE TABLE IF NOT EXISTS post_office_handover_manifest_orders (
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
    CONSTRAINT uq_po_handover_manifest_order UNIQUE (manifest_id, order_id),
    CONSTRAINT fk_po_handover_manifest_orders_manifest
        FOREIGN KEY (manifest_id) REFERENCES post_office_handover_manifests(id) ON DELETE CASCADE,
    CONSTRAINT fk_po_handover_manifest_orders_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_po_handover_manifest_orders_manifest
    ON post_office_handover_manifest_orders (manifest_id);

CREATE INDEX IF NOT EXISTS idx_po_handover_manifest_orders_order
    ON post_office_handover_manifest_orders (order_id);

CREATE INDEX IF NOT EXISTS idx_po_handover_manifest_orders_scan_out
    ON post_office_handover_manifest_orders (scan_out_time);
